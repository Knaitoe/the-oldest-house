package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SequenceRepairTests {
    @GameTest(template="empty") public static void waterTrialCostsRealBreathAndItsChimneysRestoreIt(GameTestHelper h){
        var p=h.makeMockServerPlayerInLevel();p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);var l=h.getLevel();var base=h.absolutePos(BlockPos.ZERO).offset(720,10,0);LabyrinthHazards.buildFloodedPassage(l,base);var route=LabyrinthHazards.floodRoute(base);var pockets=LabyrinthHazards.floodAir(base);int lowest=300;
        try{
            p.setForcedPose(net.minecraft.world.entity.Pose.SWIMMING);p.setPose(net.minecraft.world.entity.Pose.SWIMMING);p.setSwimming(true);p.setAirSupply(300);p.moveTo(Vec3.atBottomCenterOf(route.getFirst()).add(0,.05,0));
            for(var node:route){
                Vec3 target=Vec3.atBottomCenterOf(node).add(0,.05,0);
                for(int n=0;n<8&&p.position().distanceToSqr(target)>.0001;n++){
                    Vec3 delta=target.subtract(p.position());p.move(net.minecraft.world.entity.MoverType.SELF,delta.normalize().scale(Math.min(.16,delta.length())));p.baseTick();
                }
                h.assertTrue(p.position().distanceToSqr(target)<.001&&l.noCollision(p,p.getBoundingBox()),"the native swimming body really clears each authored underwater turn");
                lowest=Math.min(lowest,p.getAirSupply());h.assertTrue(p.getAirSupply()>0&&p.isAlive(),"the physical distances between refuges are survivable with ordinary swimming strides");
                BlockPos pocket=pockets.stream().filter(q->q.getX()==node.getX()&&q.getZ()==node.getZ()).findFirst().orElse(null);
                if(pocket!=null){
                    for(int n=0;n<22;n++){p.move(net.minecraft.world.entity.MoverType.SELF,new Vec3(0,.16,0));p.baseTick();}
                    h.assertTrue(p.getY()>base.getY()+2&&l.noCollision(p,p.getBoundingBox()),"a real vertical air pocket can be reached without clipping through a ceiling");
                    for(int n=0;n<80;n++)p.baseTick();h.assertTrue(p.getAirSupply()==300,"native air recovery replenishes the swimmer at a physical refuge");
                    for(int n=0;n<22;n++){p.move(net.minecraft.world.entity.MoverType.SELF,new Vec3(0,-.16,0));p.baseTick();}
                }
            }
            h.assertTrue(lowest<180,"the route consumes a material amount of actual air instead of posing a decorative water trial");h.succeed();
        }finally{p.setForcedPose(null);p.setSwimming(false);safelyRemove(p);}
    }
    private static void safelyRemove(net.minecraft.server.level.ServerPlayer p){p.server.getPlayerList().remove(p);}
    @GameTest(template="empty") public static void everyObserverProtectsThePrisonersUnseenPhysicalDeparture(GameTestHelper h){
        var s=h.getLevel().getServer();var l=h.getLevel();var oldHouse=io.github.knaitoe.theoldesthouse.house.HouseSavedData.get(s);var oldData=LabyrinthData.get(s);var origin=new BlockPos(96700,70,96700);var house=new io.github.knaitoe.theoldesthouse.house.HouseSavedData();house.markSpawned(origin);s.overworld().getDataStorage().set("the_oldest_house",house);s.overworld().getDataStorage().set("the_oldest_house_labyrinth",new LabyrinthData());
        var owner=h.makeMockServerPlayerInLevel();var peer=h.makeMockServerPlayerInLevel();var cell=FinaleArchitecture.cell(origin);var boy=FinaleRegistry.MINOTAUR.get().create(l);
        try{
            for(int x=-7;x<=7;x++)for(int z=-5;z<=10;z++){
                l.setBlock(cell.offset(x,-1,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
                for(int y=0;y<=5;y++)l.setBlock(cell.offset(x,y,z),z==0&&Math.abs(x)>1?net.minecraft.world.level.block.Blocks.STONE.defaultBlockState():net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
            }
            owner.moveTo(cell.getX()+.5,cell.getY(),cell.getZ()-3.5,0,0);peer.moveTo(cell.getX()+4.3,cell.getY(),cell.getZ()+6.5,180,0);owner.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);peer.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
            var record=new CompoundTag();record.putString("Phase",FinaleProgress.Phase.FIGHT.name());FinaleProgress.save(s,owner.getUUID(),record);
            boy.caged();boy.owner(owner.getUUID());boy.moveTo(cell.getX()+.5,cell.getY(),cell.getZ()+4.5);l.addFreshEntity(boy);UUID original=boy.getUUID();
            for(int n=0;n<50;n++){boy.tick();h.assertTrue(boy.childAppearance()&&boy.motion()==MinotaurEntity.CAGED,"an actual peer looking into the cell prevents any visible transformation");}
            h.assertTrue(boy.getX()>cell.getX()+3.5,"the child physically walks behind the side wall instead of being replaced or teleported");
            peer.setYRot(0);peer.setYHeadRot(0);peer.setXRot(0);
            h.assertTrue(!boy.observed(),"masonry conceals the child from the owner after the peer turns away");boy.tick();
            h.assertTrue(boy.getUUID().equals(original)&&!boy.childAppearance()&&boy.motion()==MinotaurEntity.WATCHING&&boy.getBbHeight()>3,"the same native actor changes only after every actual observer loses sight");h.succeed();
        }finally{if(boy!=null)boy.discard();s.getPlayerList().remove(owner);s.getPlayerList().remove(peer);s.overworld().getDataStorage().set("the_oldest_house",oldHouse);s.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);}
    }
    @GameTest(template="empty") public static void customEssayUsesReadableNativeBookWithoutDuplicatingItsOriginal(GameTestHelper h){
        var p=h.makeMockServerPlayerInLevel();var essay=new ItemStack(DrownedTownRegistry.DRIED_ESSAY_ONE.get());var identity=new CompoundTag();identity.putUUID("OriginalEssay",UUID.randomUUID());essay.set(DataComponents.CUSTOM_DATA,CustomData.of(identity));p.setItemInHand(InteractionHand.MAIN_HAND,essay);
        try{
            essay.getItem().use(p.level(),p,InteractionHand.MAIN_HAND);
            h.assertTrue(p.containerMenu instanceof LecternMenu,"using the custom dried item opens an actual native lectern reader");
            var menu=(LecternMenu)p.containerMenu;var display=menu.getSlot(0).getItem();
            h.assertTrue(display.is(Items.WRITTEN_BOOK)&&display.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().get(1).raw().getString().contains("water's surface"),"the native reader receives a vanilla written book with the actual refuge guidance");
            h.assertTrue(menu.clickMenuButton(p,102)&&menu.getPage()==2&&!menu.clickMenuButton(p,999),"native page controls work and reject forged pages");
            h.assertTrue(!menu.clickMenuButton(p,3)&&p.getInventory().countItem(Items.WRITTEN_BOOK)==0&&p.getItemInHand(InteractionHand.MAIN_HAND)==essay&&essay.get(DataComponents.CUSTOM_DATA).copyTag().equals(identity),"the reader cannot take a second book or change original custody and identity");h.succeed();
        }finally{p.closeContainer();p.server.getPlayerList().remove(p);}
    }
    @GameTest(template="empty") public static void visitLockAndDormantSourceRemainPersonalAcrossReload(GameTestHelper h){
        var p=h.makeMockServerPlayerInLevel();p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);var s=p.server;var previous=LabyrinthData.get(s);var d=new LabyrinthData();s.overworld().getDataStorage().set("the_oldest_house_labyrinth",d);
        try{
            var at=h.absolutePos(new BlockPos(0,0,0));var source=new LabyrinthData.Door("repair.source",p.level().dimension(),at,Direction.SOUTH,LabyrinthData.DEALT,false);d.putDoor(source);
            LabyrinthBuilder.registerDoors(d,LabyrinthPlace.DROWNED_TOWN,at.offset(500,0,0));var exit=d.door(LabyrinthPlace.DROWNED_TOWN.entryDoorId());var town=new CompoundTag();town.putInt("Visit",1);d.setState(DrownedTown.ID,town);
            VignetteGate.begin(p,LabyrinthPlace.DROWNED_TOWN);h.assertTrue(!VignetteGate.exitLocked(p,exit),"the vestibule remains reversible before physically entering");VignetteGate.stepped(p,LabyrinthPlace.DROWNED_TOWN);
            h.assertTrue(!VignetteGate.exitLocked(p,exit),"an unfinished lake visit can be voluntarily abandoned without false completion");
            h.assertTrue(!WitnessAccount.has(d,p.getUUID(),WitnessAccount.Story.DROWNED_TOWN)&&d.state(DrownedTown.ID).getInt("DryMask")==0,"retreat neither awards evidence nor erases the unfinished task");
            VignetteGate.departed(p,new LabyrinthData.Waypoint(p.level().dimension(),Vec3.atBottomCenterOf(at),0,true),exit);UUID peer=UUID.randomUUID();
            h.assertTrue(VignetteGate.dormant(d,p.getUUID(),source)&&!VignetteGate.dormant(d,peer,source),"leaving makes only this explorer's source dormant");
            var loaded=LabyrinthData.FACTORY.deserializer().apply(d.save(new CompoundTag(),p.registryAccess()),p.registryAccess());s.overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);
            h.assertTrue(VignetteGate.dormant(loaded,p.getUUID(),loaded.door(source.id)),"dormancy survives native world-data reload");loaded.deal(p.getUUID(),loaded.door(source.id),LabyrinthPlace.DROWNED_TOWN.id(),false);
            h.assertTrue(!VignetteGate.dormant(loaded,p.getUUID(),loaded.door(source.id)),"an actual personal rediscovery unlocks the source again");h.succeed();
        }finally{s.overworld().getDataStorage().set("the_oldest_house_labyrinth",previous);s.getPlayerList().remove(p);}
    }
    @GameTest(template="empty") public static void cagedBoyReloadsAndAwakensAsTheSameLivingCreature(GameTestHelper h){
        var boy=FinaleRegistry.MINOTAUR.get().create(h.getLevel());boy.caged();var save=new CompoundTag();boy.saveWithoutId(save);var restored=FinaleRegistry.MINOTAUR.get().create(h.getLevel());restored.load(save);
        try{
            h.assertTrue(restored.motion()==MinotaurEntity.CAGED&&restored.getBbWidth()<.6&&restored.getBbHeight()<1.4&&restored.getUUID().equals(boy.getUUID()),"native save retains the visible child's pose, size and entity identity");UUID owner=UUID.randomUUID();restored.awaken(owner);
            h.assertTrue(owner.equals(restored.owner())&&restored.motion()==MinotaurEntity.WATCHING&&restored.getBbHeight()>3&&restored.getUUID().equals(boy.getUUID())&&restored.isAlive(),"opening the cell changes the same living actor into the encounter");h.succeed();
        }finally{boy.discard();restored.discard();}
    }
}
