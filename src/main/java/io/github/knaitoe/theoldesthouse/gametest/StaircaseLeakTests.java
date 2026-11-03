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
    /** 0.4.214: the second 160-block stretch of the descent, where the household's rhythm first allows a leak or a door. */
    private static BlockPos paper(StaircaseAccessTests.Fixture f){return f.base.offset(3,FinaleArchitecture.TOP-200,24);}
    private static void read(StaircaseAccessTests.Fixture f,ServerPlayer p,int index){
        var at=paper(f);StaircaseHousehold.forget(p);
        for(int x=-3;x<=3;x++)for(int z=-2;z<=3;z++)for(int y=-1;y<=2;y++)f.put(f.level,at.offset(x,y,z),(y==-1?Blocks.STONE:Blocks.AIR).defaultBlockState());
        f.put(f.level,at,NoteSurfaceBlock.state(HouseMarginalia.Thread.HOUSEKEEPING,Direction.NORTH));
        p.teleportTo(f.level,at.getX()+.5+Math.max(0,f.players.indexOf(p))*1.2,at.getY(),at.getZ()+1.5,37,11);p.hasChangedDimension();p.connection.resetPosition();p.setDeltaMovement(Vec3.ZERO);
        FinaleProgress.phase(p.server,p.getUUID(),FinaleProgress.Phase.STAIRCASE);
        var own=f.data().stateEntry(StaircaseWriting.ID,p.getUUID().toString());var books=own.getCompound("Books");var indices=own.getCompound("Indices");var key=Long.toString(at.asLong());
        books.put(key,StaircaseNotes.specimen(index).save(p.registryAccess()));indices.putInt(key,index);own.put("Books",books);own.put("Indices",indices);f.data().setStateEntry(StaircaseWriting.ID,p.getUUID().toString(),own);
        StaircaseWriting.open(p,at);var book=p.containerMenu.getSlot(0).getItem();int last=book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()-1;
        if(last>0)p.containerMenu.clickMenuButton(p,100+last);
    }
    private static void run(GameTestHelper h,int coordinate,java.util.function.BiConsumer<StaircaseAccessTests.Fixture,long[]> step){run(h,coordinate,1799,step);}
    private static void run(GameTestHelper h,int coordinate,int limit,java.util.function.BiConsumer<StaircaseAccessTests.Fixture,long[]> step){
        var f=new StaircaseAccessTests.Fixture(h,coordinate);current=f;
        for(int slot=0;slot<8;slot++)f.chunks.hold(f.level,StaircaseLeakRooms.bounds(StaircaseLeakRooms.base(f.origin,slot)));
        f.chunks.hold(f.level,new AABB(paper(f)).inflate(35,6,35));long[] state={0,0};
        Runnable cleanup=()->{var ids=f.players.stream().map(ServerPlayer::getUUID).collect(java.util.stream.Collectors.toSet());var remove=new ArrayList<Entity>();for(var entity:f.level.getAllEntities())if(entity instanceof net.minecraft.world.entity.TamableAnimal pet&&ids.contains(pet.getOwnerUUID())||entity instanceof StaircaseReaderEcho echo&&echo.owner().filter(ids::contains).isPresent())remove.add(entity);remove.forEach(Entity::discard);StaircaseLeaks.clearForServer(f.level.getServer());for(int slot=0;slot<8;slot++){var base=StaircaseLeakRooms.base(f.origin,slot);for(int i=0;i<StaircaseLeakRooms.CELLS;i++)f.level.setBlock(StaircaseLeakRooms.position(base,i),Blocks.AIR.defaultBlockState(),18);}f.close();if(current==f)current=null;};
        h.runAfterDelay(limit,()->{if(!f.finished){String detail="Staircase scene stage "+state[0]+": "+f.players.stream().map(p->p.getGameProfile().getName()+" pos="+p.position()+" active="+StaircaseLeaks.active(p)+" offered="+StaircaseLeaks.offered(p)+" hurt="+p.hurtTime).toList();f.finished=true;cleanup.run();h.fail(detail);}});
        h.onEachTick(()->{if(f.finished||!f.chunks.ready())return;try{if(!f.started){f.start();StaircaseLeaks.clearForServer(f.level.getServer());}for(var player:new ArrayList<>(f.players))if(!player.isRemoved())player.doTick();step.accept(f,state);if(state[1]!=state[0]){TheOldestHouse.LOGGER.info("STAIRCASE LEAK native fixture {} stage {}",f.origin,state[0]);state[1]=state[0];}if(state[0]==99){f.finished=true;cleanup.run();h.succeed();}}catch(RuntimeException|Error ex){f.finished=true;cleanup.run();throw ex;}});
    }
    @GameTest(template="empty",batch="test_stair_note",timeoutTicks=1800)
    public static void anOperatorNoteRunsTheRealPrivateSceneWithoutSpendingTheDescent(GameTestHelper h){
        run(h,521000,(f,state)->{
            var source=f.source.south(2);
            var at=source.south(2);
            if(state[0]==0){
                var reader=f.player(h,"test_note_reader");
                var peer=f.player(h,"test_note_peer");
                f.put(f.level,at,Blocks.AIR.defaultBlockState());
                reader.teleportTo(f.level,source.getX()+.5,source.getY(),source.getZ()+.5,0,0);
                peer.teleportTo(f.level,source.getX()+1.5,source.getY(),source.getZ()+.5,0,0);
                reader.hasChangedDimension();peer.hasChangedDimension();
                try{
                    var dispatcher=reader.server.getCommands().getDispatcher();
                    h.assertTrue(dispatcher.execute("ghosthouse note breakfast",reader.createCommandSourceStack().withPermission(2))==1,
                            "the registered command places the selected household sheet");
                }catch(com.mojang.brigadier.exceptions.CommandSyntaxException e){throw new IllegalStateException(e);}
                h.assertTrue(StaircaseWriting.open(peer,at)&&peer.containerMenu==peer.inventoryMenu,
                        "the other reader cannot read the test sheet");
                h.assertTrue(StaircaseWriting.open(reader,at),"the owner gets the normal native reader");
                var menu=reader.containerMenu;
                var content=menu.getSlot(0).getItem().get(DataComponents.WRITTEN_BOOK_CONTENT);
                int last=content.pages().size()-1;
                if(last>0)menu.clickMenuButton(reader,100+last);
                state[0]=1;h.runAfterDelay(24,reader::closeContainer);
                return;
            }
            var reader=f.players.getFirst();
            if(state[0]==1&&StaircaseLeaks.active(reader)){
                h.assertTrue(StaircaseLeaks.activeKind(reader)==StaircaseLeakRooms.Kind.DAWN,
                        "the full reading enters the real dawn room");
                StaircaseLeaks.returnAfterBeat(reader,1);state[0]=2;
            }else if(state[0]==2&&!StaircaseLeaks.active(reader)){
                h.assertTrue(reader.blockPosition().equals(source)
                        &&!StaircaseLeaks.done(reader,3)&&StaircaseHousehold.own(reader).isEmpty(),
                        "return restores the test position without scoring or spending the real sheet");
                state[0]=99;
            }
        });
    }
    @AfterBatch(batch="test_stair_note") public static void cleanTestStairNote(ServerLevel level){cleanup();}

    @AfterBatch(batch="leak_ownership") public static void cleanOwnership(ServerLevel l){cleanup();}
    @AfterBatch(batch="leak_chores") public static void cleanChores(ServerLevel l){cleanup();}
    @AfterBatch(batch="leak_recovery") public static void cleanRecovery(ServerLevel l){cleanup();}
    @AfterBatch(batch="household_rhythm") public static void cleanRhythm(ServerLevel l){cleanup();}
    @AfterBatch(batch="household_door") public static void cleanDoor(ServerLevel l){cleanup();}
    @AfterBatch(batch="household_rooms") public static void cleanRooms(ServerLevel l){cleanup();}
    @AfterBatch(batch="household_landing") public static void cleanLanding(ServerLevel l){cleanup();}
    private static void cleanup(){if(current!=null){StaircaseLeaks.clearForServer(current.level.getServer());current.close();current=null;}}

    @GameTest(template="empty",batch="leak_ownership",timeoutTicks=1800)
    public static void nativeOriginalReadingWaitsCancelsAndKeepsPeersPetsAndExactReturns(GameTestHelper h){
        ServerPlayer[] p=new ServerPlayer[3];Wolf[] pet={null};Vec3[] source=new Vec3[2];ItemStack[] kept={null};CompoundTag[] snapshot={null};long[] born={0};int[] fires={-1};
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
            if(s[0]==1&&now-born[0]>=22){fires[0]=StaircaseFire.flames(FinaleProgress.player(p[0].server,p[0].getUUID()));p[0].closeContainer();p[1].closeContainer();h.assertTrue(StaircaseLeaks.offered(p[0])&&StaircaseLeaks.offered(p[1]),"both own full original readings offer a personal scene");p[0].moveTo(p[0].position().add(.3,0,0));s[0]=2;born[0]=now;return;}
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
            if(s[0]==6&&now-born[0]>=22){p[1].closeContainer();h.assertTrue(!StaircaseLeaks.offered(p[1])&&!StaircaseLeaks.active(p[1]),"reconnect and replay cannot repeat a consumed personal scene");h.assertTrue(FinaleProgress.phase(p[0].server,p[0].getUUID())==FinaleProgress.Phase.STAIRCASE&&StaircaseFire.flames(FinaleProgress.player(p[0].server,p[0].getUUID()))==fires[0],"scenery never advances phase or burns a leaf");pet[0].discard();s[0]=99;}
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
    // ------------------------------------------------------------------------------------------------ 0.4.214, the household
    @GameTest(template="empty",batch="household_rhythm",timeoutTicks=600)
    public static void theDescentIsDealtInOrderAndItsRhythmAllowsOneThingAStretch(GameTestHelper h){
        ServerPlayer[] p={null};
        run(h,658000,(f,s)->{
            if(s[0]!=0)return;
            for(int i=0;i<StaircaseNotes.DESCENT.length;i++)h.assertTrue(StaircaseNotes.dealt(i)==StaircaseNotes.DESCENT[i]&&StaircaseNotes.place(StaircaseNotes.DESCENT[i])==i,"the descent's sheets are dealt in their chosen order");
            var all=new HashSet<Integer>();for(int i=0;i<StaircaseNotes.TEXTS.size();i++)all.add(StaircaseNotes.dealt(i));h.assertTrue(all.size()==StaircaseNotes.TEXTS.size(),"every sheet is dealt exactly once before any repeats");
            var papers=StaircaseWriting.positions(f.origin);
            for(int i=0;i<StaircaseNotes.DESCENT.length;i++)h.assertTrue(StaircaseHousehold.stretch(papers.get(i).getY())==i/3,"three loose sheets lie in each 160-block stretch: sheet "+i+" at "+papers.get(i).getY());
            for(int j=0;j<3;j++)h.assertTrue(StaircaseLeakRooms.kind(StaircaseNotes.DESCENT[j])==null&&StaircaseHousehold.room(StaircaseNotes.DESCENT[j])==null,"the first stretch opens nothing");
            for(int st=1;st<=5;st++){boolean leak=false;for(int j=0;j<3;j++)leak|=StaircaseLeakRooms.kind(StaircaseNotes.DESCENT[st*3+j])!=null;h.assertTrue(leak,"stretch "+(st+1)+" holds one leak's sheet");}
            h.assertTrue(StaircaseHousehold.room(11)==StaircaseLeakRooms.Kind.DINING&&StaircaseHousehold.room(17)==StaircaseLeakRooms.Kind.DINING,"The good cups and The table open Sunday lunch");
            p[0]=f.player(h,"rhythm");FinaleProgress.phase(p[0].server,p[0].getUUID(),FinaleProgress.Phase.STAIRCASE);
            java.util.function.IntFunction<Boolean> at=y->{p[0].teleportTo(f.level,f.base.getX()+3.5,y,f.base.getZ()+24.5,0,0);return StaircaseHousehold.allows(p[0]);};
            h.assertTrue(!at.apply(FinaleArchitecture.TOP),"nothing in the first stretch: the shaft's own dark comes first");
            h.assertTrue(at.apply(FinaleArchitecture.TOP-200),"the second stretch allows a leak or a door");
            StaircaseHousehold.mark(p[0],"leak:0");h.assertTrue(!StaircaseHousehold.allows(p[0]),"one thing a stretch");
            h.assertTrue(!at.apply(FinaleArchitecture.TOP-360),"nothing straight after a stretch that had one");
            h.assertTrue(at.apply(FinaleArchitecture.TOP-520),"the stretch after that allows again");
            h.assertTrue(!at.apply(FinaleArchitecture.TOP-1250),"the bottom stretch holds only the last landing");
            at.apply(FinaleArchitecture.TOP-520);FinaleProgress.phase(p[0].server,p[0].getUUID(),FinaleProgress.Phase.HOMEWARD);h.assertTrue(!StaircaseHousehold.allows(p[0]),"nothing on the homeward climb");
            s[0]=99;
        });
    }
    @GameTest(template="empty",batch="household_door",timeoutTicks=3000)
    public static void aHouseholdDoorIsLeftItsLunchIsSatThroughOrLeftAndTheWallIsPutBack(GameTestHelper h){
        ServerPlayer[] p=new ServerPlayer[2];BlockPos[] door={null};net.minecraft.world.level.block.state.BlockState[] wall={null,null};long[] born={0};
        run(h,660000,2900,(f,s)->{
            long now=f.level.getGameTime();var paper=paper(f);
            if(s[0]==0){door[0]=StaircaseHousehold.doorAt(f.origin,paper);f.chunks.hold(f.level,new AABB(door[0]).inflate(8));
                wall[0]=f.level.getBlockState(door[0]);wall[1]=f.level.getBlockState(door[0].west(4));
                p[0]=f.player(h,"house_owner");p[1]=f.player(h,"house_peer");read(f,p[0],11);born[0]=now;s[0]=1;return;}
            if(s[0]==1&&now-born[0]>=22){p[0].closeContainer();
                h.assertTrue(f.level.getBlockState(door[0]).is(StaircaseLeakRegistry.HOUSEHOLD_DOOR.get())&&f.level.getBlockState(door[0].above()).is(StaircaseLeakRegistry.HOUSEHOLD_DOOR.get()),"reading The good cups to its last line leaves a door in the shaft wall: "+f.level.getBlockState(door[0]));
                h.assertTrue(f.level.getBlockState(door[0].west(3).below()).is(Blocks.OAK_PLANKS)&&f.level.getBlockState(door[0].west(4)).isAir(),"a doorstep of boards runs out to it, through a gap in the landing's rail");
                h.assertTrue(!StaircaseHousehold.allows(p[0]),"the door spends the stretch");
                p[1].teleportTo(f.level,door[0].getX()-.5,door[0].getY(),door[0].getZ()+.5,90,0);StaircaseHousehold.useDoor(p[1],door[0]);h.assertTrue(!StaircaseLeaks.offered(p[1])&&!StaircaseLeaks.active(p[1]),"it isn't the peer's door");
                p[0].teleportTo(f.level,door[0].getX()-1.5,door[0].getY(),door[0].getZ()+.5,-90,0);h.assertTrue(StaircaseHousehold.useDoor(p[0],door[0])&&StaircaseLeaks.offered(p[0]),"its reader goes through it");s[0]=2;return;}
            if(s[0]==2&&StaircaseLeaks.active(p[0])){
                h.assertTrue(StaircaseLeaks.activeKind(p[0])==StaircaseLeakRooms.Kind.DINING,"behind the door is Sunday lunch");
                var b=StaircaseLeaks.activeBase(p[0]);p[0].teleportTo(f.level,b.getX()+.5,b.getY(),b.getZ()-2.5,0,0);
                StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.SIDEBOARD));
                p[0].setYRot(180);for(var place:StaircaseHouseholdRooms.PLACES)StaircaseLeakChores.interact(p[0],b.offset(place));
                h.assertTrue(StaircaseLeaks.progress(p[0]).getInt("Laid")==6&&!StaircaseLeaks.progress(p[0]).getBoolean("Changed"),"six places laid, two with forks that don't match, and nobody changes them while they are watched");
                p[0].setYRot(0);p[0].setXRot(0);born[0]=now;s[0]=3;return;}
            if(s[0]==3&&StaircaseLeaks.progress(p[0]).getBoolean("Changed")){var b=StaircaseLeaks.activeBase(p[0]);
                for(var place:StaircaseHouseholdRooms.PLACES)h.assertTrue(f.level.getBlockState(b.offset(place)).getValue(StaircaseLeakProps.KIND)==StaircaseLeakProps.Kind.PLACE_LAID,"the forks were changed while the reader looked away");
                StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.PLACES[0]));h.assertTrue(StaircaseLeaks.progress(p[0]).getInt("CupsLaid")==0,"no cups before the cupboard");
                StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.CUPBOARD));StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.PLACES[0]));h.assertTrue(StaircaseLeaks.progress(p[0]).getInt("CupsLaid")==0,"the cracked cup goes to the flowers first");
                StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.VASE));for(var place:StaircaseHouseholdRooms.PLACES)StaircaseLeakChores.interact(p[0],b.offset(place));
                StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.VISITOR));h.assertTrue(!StaircaseLeaks.progress(p[0]).getBoolean("Visitor"),"no visitor's place without the seventh chair");
                StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.SEVENTH));StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.VISITOR));StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.VISITOR));
                var own=StaircaseLeaks.progress(p[0]);h.assertTrue(own.getInt("CupsLaid")==6&&own.getBoolean("Visitor")&&f.level.getBlockState(b.offset(StaircaseHouseholdRooms.SEVENTH_UP)).getBlock() instanceof HouseholdFurnitureBlock,"six good cups, the visitor's place and the seventh chair pulled up");
                born[0]=now;s[0]=4;return;}
            if(s[0]==4&&StaircaseLeaks.progress(p[0]).getBoolean("Hatch")){var b=StaircaseLeaks.activeBase(p[0]);
                p[0].teleportTo(f.level,b.getX()+.5,b.getY(),b.getZ()-7.5,180,0);StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.HATCH[1]));h.assertTrue(StaircaseLeaks.progress(p[0]).getBoolean("Tray"),"the bowls are taken from the hatch");
                p[0].teleportTo(f.level,b.getX()+.5,b.getY(),b.getZ()-2.5,180,0);for(var place:StaircaseHouseholdRooms.PLACES)StaircaseLeakChores.interact(p[0],b.offset(place));
                StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.VISITOR));h.assertTrue(StaircaseLeaks.progress(p[0]).getInt("Bowls")==6,"six bowls from the hatch, none for the visitor");
                var chair=b.offset(StaircaseHouseholdRooms.CHAIRS[0]);p[0].teleportTo(f.level,chair.getX()+.5,chair.getY(),chair.getZ()+1.5,180,0);StaircaseLeakChores.interact(p[0],chair);
                h.assertTrue(p[0].isPassenger()&&StaircaseLeaks.progress(p[0]).getLong("Meal")>0,"the reader sits down to the meal");
                var own=StaircaseLeaks.progress(p[0]);own.putInt("Line",StaircaseHouseholdTexts.LUNCH.size()-2);StaircaseLeaks.progress(p[0],own);born[0]=now;s[0]=5;return;}
            if(s[0]==5&&!StaircaseLeaks.active(p[0])){
                var rooms=StaircaseHousehold.own(p[0]).getCompound("Rooms").getCompound("DINING");h.assertTrue(rooms.getBoolean("Success")&&rooms.getString("Last").equals("success"),"sitting through to the chairs pushing back is the lunch's success");
                var sheet=StaircaseHousehold.withAccount(p[0],paper,StaircaseNotes.specimen(11));h.assertTrue(sheet.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()==2,"the household's own sheet about the reader follows The good cups");
                h.assertTrue(StaircaseHousehold.rooms(p[0])==1,"the lunch counts among the household's rooms");
                // The peer reads the same sheet and gets up from the table.
                read(f,p[1],11);born[0]=now;s[0]=6;return;}
            if(s[0]==6&&now-born[0]>=22){p[1].closeContainer();p[1].teleportTo(f.level,door[0].getX()-1.5,door[0].getY(),door[0].getZ()+.5,-90,0);h.assertTrue(StaircaseHousehold.useDoor(p[1],door[0])&&StaircaseLeaks.offered(p[1]),"the peer's own reading makes the door theirs too");s[0]=7;return;}
            if(s[0]==7&&StaircaseLeaks.active(p[1])){var b=StaircaseLeaks.activeBase(p[1]);
                var own=StaircaseLeaks.progress(p[1]);own.putInt("Laid",6);own.putInt("Bowls",6);StaircaseLeaks.progress(p[1],own);
                var chair=b.offset(StaircaseHouseholdRooms.CHAIRS[3]);p[1].teleportTo(f.level,chair.getX()+.5,chair.getY(),chair.getZ()-.5,0,0);StaircaseLeakChores.interact(p[1],chair);
                h.assertTrue(p[1].isPassenger(),"the peer sits down");born[0]=now;s[0]=8;return;}
            if(s[0]==8&&now-born[0]>=30&&p[1].isPassenger()){p[1].stopRiding();born[0]=now;s[0]=9;return;}
            if(s[0]==9&&!StaircaseLeaks.active(p[1])){
                h.assertTrue(StaircaseHousehold.own(p[1]).getCompound("Rooms").getCompound("DINING").getString("Last").equals("failure"),"getting up while the others eat is the lunch's failure");
                read(f,p[1],17);born[0]=now;s[0]=10;return;}
            if(s[0]==10&&now-born[0]>=22){p[1].closeContainer();p[1].teleportTo(f.level,door[0].getX()-1.5,door[0].getY(),door[0].getZ()+.5,-90,0);StaircaseHousehold.useDoor(p[1],door[0]);s[0]=11;return;}
            if(s[0]==11&&StaircaseLeaks.active(p[1])){var b=StaircaseLeaks.activeBase(p[1]);
                h.assertTrue(StaircaseLeaks.progress(p[1]).getBoolean("Unlaid")&&f.level.getBlockState(b.offset(StaircaseHouseholdRooms.CHAIRS[5])).isAir(),"next Sunday no place is laid for the one who got up");
                StaircaseLeaks.returnNow(p[1]);h.assertTrue(!StaircaseLeaks.done(p[1],17),"a forced return spends nothing");
                for(var q:p)q.teleportTo(f.level,paper.getX()+.5,paper.getY(),paper.getZ()+1.5,0,0);
                var o=StaircaseHousehold.own(p[1]);o.putInt("Descent",o.getInt("Descent")+1);f.data().setStateEntry(StaircaseHousehold.STATE,p[1].getUUID().toString(),o);born[0]=now;s[0]=12;return;}
            if(s[0]==12&&f.data().stateKeys(StaircaseHousehold.DOORS).isEmpty()){
                h.assertTrue(f.level.getBlockState(door[0]).equals(wall[0])&&f.level.getBlockState(door[0].west(4)).equals(wall[1]),"once used and left behind, the wall and the landing's rail are put back as they were");s[0]=99;}
        });
    }
    /** Reads a household source and goes through the door it leaves. */
    private static void throughDoor(StaircaseAccessTests.Fixture f,ServerPlayer p,BlockPos door){p.closeContainer();p.teleportTo(f.level,door.getX()-1.5,door.getY(),door.getZ()+.5,-90,0);StaircaseHousehold.useDoor(p,door);}
    private static StaircaseLeakProps.Kind prop(StaircaseAccessTests.Fixture f,BlockPos at){var st=f.level.getBlockState(at);return st.is(StaircaseLeakRegistry.PROP.get())?st.getValue(StaircaseLeakProps.KIND):null;}
    @GameTest(template="empty",batch="household_rooms",timeoutTicks=3600)
    public static void freshSheetsMendingDayAndFirstLightArePlayedThroughTheirDoors(GameTestHelper h){
        ServerPlayer[] p={null};BlockPos[] door={null};long[] born={0};
        run(h,662000,3500,(f,s)->{
            long now=f.level.getGameTime();var paper=paper(f);
            if(s[0]==0){door[0]=StaircaseHousehold.doorAt(f.origin,paper);f.chunks.hold(f.level,new AABB(door[0]).inflate(8));p[0]=f.player(h,"house_rooms");read(f,p[0],15);born[0]=now;s[0]=1;return;}
            // Fresh sheets: two sheets folded with the other end, the older sheets on the bed, the sock home, the striped pillowcase.
            if(s[0]==1&&now-born[0]>=22){throughDoor(f,p[0],door[0]);s[0]=2;return;}
            if(s[0]==2&&StaircaseLeaks.active(p[0])){var b=StaircaseLeaks.activeBase(p[0]);h.assertTrue(StaircaseLeaks.activeKind(p[0])==StaircaseLeakRooms.Kind.BEDROOM,"The airing cupboard opens Fresh sheets");
                p[0].teleportTo(f.level,b.getX()+.5,b.getY(),b.getZ()-3.5,180,0);StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.BASKET));StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.FOLD));
                h.assertTrue(prop(f,b.offset(StaircaseHouseholdRooms.FOLD))==StaircaseLeakProps.Kind.SHEET_ONE_END,"one end of the sheet in the reader's hands");born[0]=now;s[0]=3;return;}
            if((s[0]==3||s[0]==4)&&prop(f,StaircaseLeaks.activeBase(p[0]).offset(StaircaseHouseholdRooms.FOLD))==StaircaseLeakProps.Kind.SHEET_FOLDED){var b=StaircaseLeaks.activeBase(p[0]);
                StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.FOLD));StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.AIRING_HIGH_AT));
                if(s[0]==3){StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.BASKET));StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.FOLD));s[0]=4;return;}
                h.assertTrue(StaircaseLeaks.progress(p[0]).getInt("Folded")==2&&!StaircaseLeaks.progress(p[0]).getBoolean("Creased"),"two sheets folded between the reader and the other end, no crease");
                StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.AIRING_LOW_AT));StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.BED_FOOT));
                h.assertTrue(prop(f,b.offset(StaircaseHouseholdRooms.SOCK))==StaircaseLeakProps.Kind.FLOOR_SOCK,"a sock falls out of the fitted sheet");
                StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.SOCK));StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.BASKET));StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.AIRING_HIGH_AT));
                h.assertTrue(StaircaseLeaks.progress(p[0]).getBoolean("SockHome")&&StaircaseLeaks.progress(p[0]).getBoolean("Plain"),"the sock on the basket, and only plain pillowcases in the cupboard");
                p[0].setYRot(180);born[0]=now;s[0]=5;return;}
            if(s[0]==5&&StaircaseLeaks.progress(p[0]).getBoolean("StripedShown")){var b=StaircaseLeaks.activeBase(p[0]);
                StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.BASKET));StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.BED_HEAD));s[0]=6;return;}
            if(s[0]==6&&!StaircaseLeaks.active(p[0])){
                h.assertTrue(StaircaseHousehold.own(p[0]).getCompound("Rooms").getCompound("BEDROOM").getBoolean("Success"),"the bed made with the striped pillowcase, the sock home and no crease");
                p[0].teleportTo(f.level,paper.getX()+.5,paper.getY(),paper.getZ()+1.5,0,0);read(f,p[0],4);born[0]=now;s[0]=7;return;}
            // Mending day: the sewn-shut sleeve, unpicked once, then turned, threaded on the third try, a button from the tin.
            if(s[0]==7&&now-born[0]>=22){throughDoor(f,p[0],door[0]);s[0]=8;return;}
            if(s[0]==8&&StaircaseLeaks.active(p[0])){var b=StaircaseLeaks.activeBase(p[0]);h.assertTrue(StaircaseLeaks.activeKind(p[0])==StaircaseLeakRooms.Kind.MENDING,"Thursday opens Mending day");
                p[0].teleportTo(f.level,b.getX()+.5,b.getY(),b.getZ()-3.5,180,0);
                StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.SEWING));h.assertTrue(!StaircaseLeaks.progress(p[0]).getBoolean("Threaded")&&StaircaseLeaks.progress(p[0]).getInt("Tries")==0,"the lamp's shadow is over the needle");
                StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.LAMP_CLOSE));for(int i=0;i<3;i++)StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.SEWING));
                h.assertTrue(StaircaseLeaks.progress(p[0]).getBoolean("Threaded"),"threaded on the third try, the lamp moved aside");
                StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.COAT));h.assertTrue(prop(f,b.offset(StaircaseHouseholdRooms.COAT))==StaircaseLeakProps.Kind.COAT_SHUT,"stitched without turning, the sleeve is shut");s[0]=9;return;}
            if(s[0]==9&&prop(f,StaircaseLeaks.activeBase(p[0]).offset(StaircaseHouseholdRooms.COAT))==StaircaseLeakProps.Kind.COAT_SLEEVE){var b=StaircaseLeaks.activeBase(p[0]);
                // Unpicked once: a crouching hand turns the sleeve, threaded as it is, and then the needle goes in right side in.
                p[0].setShiftKeyDown(true);StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.COAT));p[0].setShiftKeyDown(false);StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.COAT));
                h.assertTrue(prop(f,b.offset(StaircaseHouseholdRooms.COAT))==StaircaseLeakProps.Kind.COAT_SEWN,"turned, then stitched right side in");
                StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.TIN));StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.TIN));StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.COAT));s[0]=10;return;}
            if(s[0]==10&&!StaircaseLeaks.active(p[0])){
                h.assertTrue(StaircaseHousehold.own(p[0]).getCompound("Rooms").getCompound("MENDING").getBoolean("Success"),"the sleeve sewn right side in and the button on");
                h.assertTrue(StaircaseHousehold.withAccount(p[0],paper,StaircaseNotes.specimen(4)).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().getLast().raw().getString().contains("from the tin"),"the household's sheet names the button chosen");
                p[0].teleportTo(f.level,paper.getX()+.5,paper.getY(),paper.getZ()+1.5,0,0);read(f,p[0],3);born[0]=now;s[0]=11;return;}
            // First light: the kettle off before it sings, twice; the second cup by the sleeper; the extra potato and Bell's plate.
            if(s[0]==11&&now-born[0]>=22){throughDoor(f,p[0],door[0]);s[0]=12;return;}
            if(s[0]==12&&StaircaseLeaks.active(p[0])){var b=StaircaseLeaks.activeBase(p[0]);h.assertTrue(StaircaseLeaks.activeKind(p[0])==StaircaseLeakRooms.Kind.DAWN,"Breakfast opens First light");
                p[0].teleportTo(f.level,b.getX()+.5,b.getY(),b.getZ()-5.5,180,0);StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.KETTLE));s[0]=13;return;}
            if((s[0]==13||s[0]==14)&&prop(f,StaircaseLeaks.activeBase(p[0]).offset(StaircaseHouseholdRooms.KETTLE))==StaircaseLeakProps.Kind.KETTLE_BOILED){var b=StaircaseLeaks.activeBase(p[0]);
                StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.KETTLE));
                if(s[0]==13){StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.FIRST_CUP));StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.FIRST_CUP));StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.KETTLE));s[0]=14;return;}
                StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.SECOND_CUP));StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.SECOND_CUP));
                h.assertTrue(StaircaseLeaks.progress(p[0]).getFloat("Noise")<30&&!StaircaseLeaks.progress(p[0]).getBoolean("Whistle"),"taken off before it sings, the sleeper undisturbed");
                StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.SIDE));StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.POTATO));
                StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.SUPPER));StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.SUPPER));s[0]=15;return;}
            if(s[0]==15&&!StaircaseLeaks.active(p[0])){
                h.assertTrue(StaircaseHousehold.own(p[0]).getCompound("Rooms").getCompound("DAWN").getBoolean("Success"),"the cup by the sleeper and the plate wrapped, without waking anyone");
                h.assertTrue(StaircaseHousehold.rooms(p[0])==3,"three of the household's rooms");s[0]=99;}
        });
    }
    /** Reads a sheet at any height in the shaft, as read() does at the second stretch. */
    private static void readAt(StaircaseAccessTests.Fixture f,ServerPlayer p,int index,BlockPos at){
        for(int x=-3;x<=3;x++)for(int z=-2;z<=3;z++)for(int y=-1;y<=2;y++)f.put(f.level,at.offset(x,y,z),(y==-1?Blocks.STONE:Blocks.AIR).defaultBlockState());
        f.put(f.level,at,NoteSurfaceBlock.state(HouseMarginalia.Thread.HOUSEKEEPING,Direction.NORTH));
        p.teleportTo(f.level,at.getX()+.5,at.getY(),at.getZ()+1.5,37,11);p.connection.resetPosition();p.setDeltaMovement(Vec3.ZERO);FinaleProgress.phase(p.server,p.getUUID(),FinaleProgress.Phase.STAIRCASE);
        var own=f.data().stateEntry(StaircaseWriting.ID,p.getUUID().toString());var books=own.getCompound("Books");var indices=own.getCompound("Indices");var key=Long.toString(at.asLong());
        books.put(key,StaircaseNotes.specimen(index).save(p.registryAccess()));indices.putInt(key,index);own.put("Books",books);own.put("Indices",indices);f.data().setStateEntry(StaircaseWriting.ID,p.getUUID().toString(),own);
        StaircaseWriting.open(p,at);var book=p.containerMenu.getSlot(0).getItem();int last=book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()-1;if(last>0)p.containerMenu.clickMenuButton(p,100+last);
    }
    private static void aim(ServerPlayer p,Vec3 at){var d=at.subtract(p.getEyePosition());float yaw=(float)(Math.toDegrees(Math.atan2(-d.x,d.z)));float pitch=(float)(-Math.toDegrees(Math.atan2(d.y,Math.sqrt(d.x*d.x+d.z*d.z))));p.setYRot(yaw);p.setYHeadRot(yaw);p.setXRot(pitch);}
    @GameTest(template="empty",batch="household_landing",timeoutTicks=7200)
    public static void theLongWayHomeWashingDayAndTheLayByOpenTheLastLanding(GameTestHelper h){
        ServerPlayer[] p={null};BlockPos[] door={null,null};BlockPos[] low={null};long[] born={0};
        run(h,664000,7100,(f,s)->{
            long now=f.level.getGameTime();var paper=paper(f);
            if(s[0]==0){door[0]=StaircaseHousehold.doorAt(f.origin,paper);f.chunks.hold(f.level,new AABB(door[0]).inflate(8));
                low[0]=f.base.offset(3,FinaleArchitecture.TOP-1200,24);door[1]=StaircaseHousehold.doorAt(f.origin,low[0]);f.chunks.hold(f.level,new AABB(low[0]).inflate(35,6,35));
                p[0]=f.player(h,"house_landing");read(f,p[0],2);born[0]=now;s[0]=1;return;}
            // The long way home: the radio down for the pear tree, a clear patch at the lights, leaning into the bends, the map, the handbrake twice.
            if(s[0]==1&&now-born[0]>=22){throughDoor(f,p[0],door[0]);s[0]=2;return;}
            if(s[0]==2&&StaircaseLeaks.active(p[0])){h.assertTrue(StaircaseLeaks.activeKind(p[0])==StaircaseLeakRooms.Kind.DRIVE&&p[0].isPassenger(),"On the way home seats the reader in the passenger seat");s[0]=3;return;}
            var drive=StaircaseLeaks.active(p[0])&&StaircaseLeaks.activeKind(p[0])==StaircaseLeakRooms.Kind.DRIVE?StaircaseLeaks.progress(p[0]):null;
            if(s[0]==3&&drive!=null&&drive.getInt("Stage")==1){var b=StaircaseLeaks.activeBase(p[0]);StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.RADIO_AT));s[0]=4;return;}
            if(s[0]==4&&drive!=null&&drive.getInt("Stage")==2){var b=StaircaseLeaks.activeBase(p[0]);StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.WINDSCREEN));h.assertTrue(StaircaseLeaks.progress(p[0]).getBoolean("Wiped"),"a clear patch at the lights");s[0]=5;return;}
            if(s[0]==5&&drive!=null&&drive.getInt("Stage")>=3){p[0].setYRot(90);p[0].setYHeadRot(90);if(drive.getInt("Stage")==5){var b=StaircaseLeaks.activeBase(p[0]);StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.MAP));
                h.assertTrue(StaircaseLeaks.progress(p[0]).getBoolean("TallyLean")&&StaircaseLeaks.progress(p[0]).getInt("Stage")==7,"leaned into both bends, and checked the map at the junction");born[0]=now;s[0]=6;}return;}
            if(s[0]==6&&now-born[0]>=25){var b=StaircaseLeaks.activeBase(p[0]);StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.HANDBRAKE));StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.HANDBRAKE));s[0]=7;return;}
            if(s[0]==7&&!StaircaseLeaks.active(p[0])){h.assertTrue(StaircaseHousehold.own(p[0]).getCompound("Rooms").getCompound("DRIVE").getBoolean("Success"),"home, and the handbrake up twice");
                p[0].teleportTo(f.level,paper.getX()+.5,paper.getY(),paper.getZ()+1.5,0,0);read(f,p[0],5);born[0]=now;s[0]=8;return;}
            // Washing day: four on the line, the blanket shaken with someone, everything in before the rain, the damp sleeve on the horse.
            if(s[0]==8&&now-born[0]>=22){throughDoor(f,p[0],door[0]);s[0]=9;return;}
            if(s[0]==9&&StaircaseLeaks.active(p[0])){var b=StaircaseLeaks.activeBase(p[0]);h.assertTrue(StaircaseLeaks.activeKind(p[0])==StaircaseLeakRooms.Kind.YARD,"After the rain opens Washing day");
                p[0].teleportTo(f.level,b.getX()+.5,b.getY(),b.getZ()-3.5,180,0);
                for(int i=0;i<4;i++){StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.WASHING));StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.LINE[i]));}
                h.assertTrue(StaircaseLeaks.progress(p[0]).getInt("Hung")==4,"four on the line");StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.BLANKET));s[0]=10;return;}
            if(s[0]==10&&StaircaseLeaks.progress(p[0]).contains("Rain")){var b=StaircaseLeaks.activeBase(p[0]);
                for(int i=0;i<4;i++)StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.LINE[i]));StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.HORSE));s[0]=11;return;}
            if(s[0]==11&&!StaircaseLeaks.active(p[0])){h.assertTrue(StaircaseHousehold.own(p[0]).getCompound("Rooms").getCompound("YARD").getBoolean("Success"),"everything in, dry, and the damp sleeve on the clothes horse");
                p[0].teleportTo(f.level,paper.getX()+.5,paper.getY(),paper.getZ()+1.5,0,0);read(f,p[0],19);born[0]=now;s[0]=12;return;}
            // The lay-by: the sandwiches, the wrapper folded small, three tractors counted, back in the car without waking Bell.
            if(s[0]==12&&now-born[0]>=22){throughDoor(f,p[0],door[0]);s[0]=13;return;}
            if(s[0]==13&&StaircaseLeaks.active(p[0])){var b=StaircaseLeaks.activeBase(p[0]);h.assertTrue(StaircaseLeaks.activeKind(p[0])==StaircaseLeakRooms.Kind.LAYBY,"A longer way opens the lay-by");
                p[0].teleportTo(f.level,b.getX()+3.5,b.getY(),b.getZ()-8.5,90,0);StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.SANDWICH_AT));StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.SANDWICH_AT));
                h.assertTrue(StaircaseLeaks.progress(p[0]).getBoolean("Folded"),"the wrapping folded into a small square");s[0]=14;return;}
            if(s[0]==14&&StaircaseLeaks.active(p[0])){var own=StaircaseLeaks.progress(p[0]);
                if(own.hasUUID("Tractor")){var t=f.level.getEntity(own.getUUID("Tractor"));if(t!=null)aim(p[0],t.position().add(1.2,.6,.6));}
                if(own.getInt("Tractors")>=3){var b=StaircaseLeaks.activeBase(p[0]);p[0].teleportTo(f.level,b.getX()+3.5,b.getY(),b.getZ()-4.5,90,0);StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.SEAT_AT));s[0]=15;}return;}
            if(s[0]==15&&!StaircaseLeaks.active(p[0])){h.assertTrue(StaircaseHousehold.own(p[0]).getCompound("Rooms").getCompound("LAYBY").getBoolean("Success"),"three tractors counted and back in the car without the horn");
                h.assertTrue(StaircaseHousehold.rooms(p[0])==3,"three of the household's rooms");
                readAt(f,p[0],48,low[0]);born[0]=now;s[0]=16;return;}
            // The last landing: everything put back in its order, the list read with the reader's own name last; the household moves on.
            if(s[0]==16&&now-born[0]>=22){p[0].closeContainer();h.assertTrue(f.level.getBlockState(door[1]).is(StaircaseLeakRegistry.HOUSEHOLD_DOOR.get()),"three rooms in, The landing leaves a door at the bottom");
                throughDoor(f,p[0],door[1]);s[0]=17;return;}
            if(s[0]==17&&StaircaseLeaks.active(p[0])){var b=StaircaseLeaks.activeBase(p[0]);h.assertTrue(StaircaseLeaks.activeKind(p[0])==StaircaseLeakRooms.Kind.LANDING,"behind it, the household's last landing");
                p[0].teleportTo(f.level,b.getX()+.5,b.getY(),b.getZ()-3.5,180,0);
                for(var at:List.of(StaircaseHouseholdRooms.CUPS,StaircaseHouseholdRooms.SPOONS,StaircaseHouseholdRooms.CAMP_LAMP_AT,StaircaseHouseholdRooms.CANDLE_AT))StaircaseLeakChores.interact(p[0],b.offset(at));
                h.assertTrue(!StaircaseLeaks.progress(p[0]).contains("Result"),"not done until the morning list is read");
                StaircaseLeakChores.interact(p[0],b.offset(StaircaseHouseholdRooms.LIST));var list=p[0].containerMenu.getSlot(0).getItem().get(DataComponents.WRITTEN_BOOK_CONTENT);
                h.assertTrue(list!=null&&list.pages().getLast().raw().getString().contains(p[0].getGameProfile().getName()),"the reader's own name last on the morning list");p[0].closeContainer();s[0]=18;return;}
            if(s[0]==18&&!StaircaseLeaks.active(p[0])){
                h.assertTrue(StaircaseHousehold.own(p[0]).getBoolean("MovedOn")&&WitnessAccount.has(f.data(),p[0].getUUID(),WitnessAccount.Story.HOUSEHOLD),"the household moves on, and its story resolves");
                h.assertTrue(!StaircaseHousehold.landingAllowed(p[0]),"its landing does not open again");s[0]=99;}
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
