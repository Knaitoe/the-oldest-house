package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FinaleTests {
    @GameTest(template="empty",batch="finale") public static void actualCreatureRejectsCopiesAndOneOriginalWoundStartsCollapse(GameTestHelper helper){
        var player=helper.makeMockServerPlayerInLevel();var server=player.server;var data=LabyrinthData.get(server);
        var previous=data.state(FinaleProgress.STATE);MinotaurEntity creature=FinaleRegistry.MINOTAUR.get().create(helper.getLevel());
        try{
            ItemStack sword=new ItemStack(Items.IRON_SWORD);UUID original=WeaponHistory.stamp(sword);CompoundTag record=new CompoundTag();
            record.putUUID("Weapon",original);record.putString("Phase",FinaleProgress.Phase.FIGHT.name());FinaleProgress.save(server,player.getUUID(),record);
            creature.owner(player.getUUID());creature.moveTo(player.position().add(0,0,4));
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,sword);
            helper.assertTrue(!creature.hurt(player.damageSources().playerAttack(player),8),"even the original cannot wound outside the shield opening");
            creature.stagger();ItemStack copy=sword.copy();WeaponHistory.markCopy(copy);player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,copy);
            helper.assertTrue(!creature.hurt(player.damageSources().playerAttack(player),8)&&creature.motion()==MinotaurEntity.STUNNED,"a House copy passes through a genuinely stunned creature");
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,sword);
            helper.assertTrue(creature.hurt(player.damageSources().playerAttack(player),8)&&creature.motion()==MinotaurEntity.WOUNDED,"one original hit starts the wounded crawl");
            helper.assertTrue(creature.getHealth()==creature.getMaxHealth()&&FinaleProgress.phase(server,player.getUUID())==FinaleProgress.Phase.COLLAPSE,"the encounter changes saved phase rather than draining conventional health");
            for(var display:helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.Display.TextDisplay.class,creature.getBoundingBox().inflate(12))){
                if(!display.getTags().contains("HouseFinaleWords"))continue;
                CompoundTag text=new CompoundTag();display.saveWithoutId(text);
                helper.assertTrue(text.getString("text").contains("edge passes through")||text.getString("text").contains("walls begin"),"the native dialogue display retains authored text");display.discard();
            }
        }finally{data.setState(FinaleProgress.STATE,previous);if(creature!=null)creature.discard();if(server.getPlayerList().getPlayers().contains(player))server.getPlayerList().remove(player);}
        helper.succeed();
    }
    @GameTest(template="empty") public static void loadingAChargeNeverResumesAnUnwarnedDash(GameTestHelper helper){
        var creature=FinaleRegistry.MINOTAUR.get().create(helper.getLevel());UUID owner=UUID.randomUUID();creature.owner(owner);
        CompoundTag saved=new CompoundTag();creature.saveWithoutId(saved);saved.putInt("FinaleMotion",MinotaurEntity.CHARGING);saved.putInt("FinaleRemaining",12);
        var restored=FinaleRegistry.MINOTAUR.get().create(helper.getLevel());restored.load(saved);
        helper.assertTrue(owner.equals(restored.owner())&&restored.motion()==MinotaurEntity.WATCHING,"reconnect preserves the encounter owner and restores the charge's warning");creature.discard();restored.discard();helper.succeed();
    }
    @GameTest(template="empty") public static void weaponIdentitySurvivesArchiveAndCopiesCannotWound(GameTestHelper helper){
        var registries=helper.getLevel().registryAccess();ItemStack sword=new ItemStack(Items.IRON_SWORD);sword.setDamageValue(73);
        UUID original=WeaponHistory.stamp(sword);MotherCollection mother=new MotherCollection();UUID owner=UUID.randomUUID();
        var entry=mother.keepItem(sword,registries,owner,0);var loaded=MotherCollection.load(mother.save(new CompoundTag(),registries),registries);
        ItemStack recovered=loaded.claimItem(owner,entry.id,registries);
        helper.assertTrue(WeaponHistory.wounds(recovered,original)&&recovered.getDamageValue()==73,"the real shelf object retains its identity and wear");
        ItemStack duplicate=recovered.copy();WeaponHistory.markCopy(duplicate);
        helper.assertTrue(!WeaponHistory.wounds(duplicate,original)&&WeaponHistory.stamp(duplicate)==null,"a copied edge cannot become original by being used");
        ItemStack other=new ItemStack(Items.IRON_SWORD);WeaponHistory.stamp(other);
        helper.assertTrue(!WeaponHistory.wounds(other,original),"an identical ordinary sword is still another physical object");helper.succeed();
    }
    @GameTest(template="empty") public static void defeatSealsEvenOrdinaryResourcesAcrossRestart(GameTestHelper helper){
        var registries=helper.getLevel().registryAccess();MotherCollection mother=new MotherCollection();UUID player=UUID.randomUUID();
        mother.keepFinaleItem(new ItemStack(Items.COBBLESTONE,48),registries,player,0);
        mother.keepFinaleItem(new ItemStack(Items.DIAMOND_SWORD),registries,player,0);
        MotherCollection loaded=MotherCollection.load(mother.save(new CompoundTag(),registries),registries);
        helper.assertTrue(loaded.all().size()==2,"failure retains resources as well as keepsakes, without world drops");
        for(var entry:loaded.all())helper.assertTrue(entry.sealed&&loaded.claimItem(player,entry.id,registries).isEmpty(),"terminal losses are permanently kept");
        helper.assertTrue(loaded.all().stream().anyMatch(e->e.item(registries).is(Items.COBBLESTONE)&&e.item(registries).getCount()==48),"ordinary inventory counts survive");helper.succeed();
    }
    @GameTest(template="empty") public static void guidanceRemembersTakingShelvesAndAcceptsKindness(GameTestHelper helper){
        var registries=helper.getLevel().registryAccess();MotherCollection mother=new MotherCollection();UUID player=UUID.randomUUID();
        helper.assertTrue(mother.canGuide(player),"leaving the shelves alone qualifies for the rescue");
        var entry=mother.keepItem(new ItemStack(Items.IRON_SWORD),registries,player,0);mother.claimItem(player,entry.id,registries);
        MotherCollection loaded=MotherCollection.load(mother.save(new CompoundTag(),registries),registries);
        helper.assertTrue(!loaded.canGuide(player),"a restart cannot erase taking from her shelves");
        ItemStack gift=new ItemStack(Items.COMPASS);gift.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("My way home"));loaded.offer(player,gift,registries,0);
        helper.assertTrue(loaded.canGuide(player),"a wanted, loved offering restores the possibility of help");helper.succeed();
    }
    @GameTest(template="empty") public static void optionalDiscoveryRequiresDepthAndPersonalExploration(GameTestHelper helper){
        LabyrinthData data=new LabyrinthData();UUID player=UUID.randomUUID(),other=UUID.randomUUID();
        for(int i=0;i<12;i++)data.pushReturn(player,new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,Vec3.ZERO,0));
        helper.assertTrue(!FinaleProgress.eligible(data,player),"depth alone never forces the climax");
        data.visit(player,LabyrinthPlace.HARRIGAN);data.visit(player,LabyrinthPlace.HIDE_AND_CLAP);
        helper.assertTrue(FinaleProgress.eligible(data,player)&&!FinaleProgress.eligible(data,other),"discovery belongs to each explorer");helper.succeed();
    }
    @GameTest(template="empty") public static void endingSurvivesWorldSaveAndDoesNotFinishVignettes(GameTestHelper helper){
        LabyrinthData data=new LabyrinthData();UUID player=UUID.randomUUID();CompoundTag record=new CompoundTag(),world=new CompoundTag();
        record.putString("Phase",FinaleProgress.Phase.LOCKED_OUT.name());world.put(player.toString(),record);data.setState(FinaleProgress.STATE,world);
        var registry=helper.getLevel().registryAccess();LabyrinthData loaded=LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(),registry),registry);
        helper.assertTrue(FinaleProgress.terminal(FinaleProgress.phase(loaded.state(FinaleProgress.STATE).getCompound(player.toString()))),"player cloning cannot erase a world-owned ending");
        helper.assertTrue(loaded.completed().isEmpty(),"defeat does not complete or erase unfinished vignettes");helper.succeed();
    }
    @GameTest(template="empty") public static void collapseDisablesOriginButPreservesItsActualSite(GameTestHelper helper){
        var registry=helper.getLevel().registryAccess();HouseSavedData house=new HouseSavedData();BlockPos site=new BlockPos(40,70,-30);house.markSpawned(site);house.collapse();
        HouseSavedData loaded=HouseSavedData.load(house.save(new CompoundTag(),registry),registry);
        helper.assertTrue(loaded.isCollapsed()&&loaded.houseOrigin()==null&&loaded.housePosition().orElseThrow().equals(site),"all House controllers stand down while demolition can resume at the real manor");helper.succeed();
    }
    @GameTest(template="empty") public static void greatStaircaseHasPhysicalDepthAndNoRoomSlotOverlap(GameTestHelper helper){
        BlockPos origin=new BlockPos(100,70,100);var plan=FinaleArchitecture.plan(origin);Map<BlockPos,net.minecraft.world.level.block.state.BlockState> blocks=new HashMap<>();
        for(var p:plan){blocks.put(p.pos(),p.block());helper.assertTrue(p.pos().getY()>=-2032&&p.pos().getY()<2032,"geometry stays inside the dimension's real height");
            helper.assertTrue(!LabyrinthPlaces.isInStack(origin,p.pos()),"the staircase does not overwrite saved vignette slots");}
        var route=FinaleArchitecture.staircaseRoute(origin);helper.assertTrue(route.get(0).getY()-route.get(route.size()-1).getY()>=1280,"this is an actual long descent");
        for(int i=0;i<route.size();i++){
            helper.assertTrue(blocks.containsKey(route.get(i).below())&&!blocks.get(route.get(i).below()).isAir(),"every tread has a real block at "+i);
            var feet=blocks.get(route.get(i));var head=blocks.get(route.get(i).above());
            helper.assertTrue((feet==null||feet.isAir())&&(head==null||head.isAir()),"a landing cannot obstruct the route at tread "+i);
        }
        var continuation=FinaleArchitecture.continuationRoute(origin);helper.assertTrue(continuation.getFirst().getY()-continuation.getLast().getY()>=1280,"the staircase physically continues equally far below the cell");
        for(var at:continuation){
            var floor=blocks.get(at.below());var feet=blocks.get(at);var head=blocks.get(at.above());
            helper.assertTrue(floor!=null&&!floor.isAir()&&(feet==null||feet.isAir())&&(head==null||head.isAir()),"the lower continuation retains a connected unobstructed tread at "+at);
        }
        for(int z=26;z<=32;z++)for(int y=FinaleArchitecture.ARENA;y<FinaleArchitecture.ARENA+3;y++){
            var opening=blocks.get(FinaleArchitecture.base(origin).offset(0,y,z));
            helper.assertTrue(opening==null||opening.isAir(),"the passage through the shaft wall really reaches the cell chamber");
        }
        for(int n:new int[]{12,48,100,180}){
            var at=route.get(n);var next=route.get(n+1);boolean alongX=next.getX()!=at.getX();
            for(int lateral=-4;lateral<=4;lateral++){
                var walking=at.offset(alongX?0:lateral,0,alongX?lateral:0);
                helper.assertTrue(!blocks.get(walking.below()).isAir()&&(blocks.get(walking)==null||blocks.get(walking).isAir())&&(blocks.get(walking.above())==null||blocks.get(walking.above()).isAir()),"nine unobstructed blocks of actual walking width at tread "+n);
            }
        }
        for(int x=-FinaleArchitecture.SHAFT_RADIUS;x<=FinaleArchitecture.SHAFT_RADIUS;x++)for(int z=-FinaleArchitecture.SHAFT_RADIUS;z<=FinaleArchitecture.SHAFT_RADIUS;z++)
            helper.assertTrue(!blocks.get(FinaleArchitecture.base(origin).offset(x,FinaleArchitecture.LOOP_BOTTOM-1,z)).isAir(),"a complete shaft floor encloses the view below the great staircase");
        helper.assertTrue(blocks.get(FinaleArchitecture.cell(origin)).is(Blocks.IRON_BARS),"the cell is physically closed before commitment");helper.succeed();
    }
    @GameTest(template="empty") public static void escapeHasConnectedFloorAndRealWrongTurns(GameTestHelper helper){
        BlockPos origin=BlockPos.ZERO;Map<BlockPos,net.minecraft.world.level.block.state.BlockState> blocks=new HashMap<>();for(var p:FinaleArchitecture.plan(origin))blocks.put(p.pos(),p.block());
        var route=FinaleArchitecture.escapeRoute(origin);
        helper.assertTrue(route.get(0).equals(FinaleArchitecture.bottomStart(origin))&&route.get(route.size()-1).equals(FinaleArchitecture.exit(origin)),"the real route joins the arrival and exit");
        for(int i=0;i<route.size();i++){helper.assertTrue(blocks.containsKey(route.get(i).below())&&!blocks.get(route.get(i).below()).isAir(),"each route point has floor");
            if(i>0)helper.assertTrue(route.get(i).distManhattan(route.get(i-1))==1,"no teleport or gap replaces a walkable turn");}
        helper.assertTrue(blocks.containsKey(FinaleArchitecture.base(origin).offset(26,3,95)),"a wrong branch has real geometry");
        helper.assertTrue(blocks.get(FinaleArchitecture.base(origin).offset(0,3,76)).isAir(),"the bottom has real cleared dropoffs rather than generated stone");
        helper.assertTrue(blocks.get(FinaleArchitecture.cell(origin).south(4).above(2)).isAir(),"the cell's interior is carved out of terrain");
        helper.assertTrue(blocks.get(FinaleArchitecture.base(origin).offset(0,150,0)).isAir(),"the shaft has an actual open central void");helper.succeed();
    }
}
