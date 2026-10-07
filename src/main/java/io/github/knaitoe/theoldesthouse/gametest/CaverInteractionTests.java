package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.gametest.*;
import java.util.function.Consumer;

@GameTestHolder(TheOldestHouse.MOD_ID+"_caver")
@PrefixGameTestTemplate(false)
public final class CaverInteractionTests {
    private static CaverTests.Fixture mining,canceled,torches;
    private static Consumer<BlockEvent.BreakEvent> refuse;
    @AfterBatch(batch="caver_native_mining") public static void cleanMining(ServerLevel l){if(mining!=null){mining.close();mining=null;}}
    @AfterBatch(batch="caver_canceled_mining") public static void cleanCanceled(ServerLevel l){if(refuse!=null){NeoForge.EVENT_BUS.unregister(refuse);refuse=null;}if(canceled!=null){canceled.close();canceled=null;}}
    @AfterBatch(batch="caver_native_torches") public static void cleanTorches(ServerLevel l){if(torches!=null){torches.close();torches=null;}}

    @GameTest(template="empty",batch="caver_native_mining",timeoutTicks=100)
    public static void nativePickaxeMiningOpensOnlyTheRealCrackAndKeepsPeerProgressPersonal(GameTestHelper h){
        mining=new CaverTests.Fixture(h,32500);var f=mining;var p=f.player();var peer=f.player();var at=f.b.offset(CaverCave.APERTURE);
        h.runAfterDelay(8,()->{
            f.at(p,.5,-3,-21.5);f.at(peer,.5,-3,-20.5);
            h.assertTrue(!p.gameMode.destroyBlock(at)&&f.l.getBlockState(at).is(Blocks.CRACKED_DEEPSLATE_BRICKS),"bare hands cannot bypass the crack's pickaxe requirement");
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_PICKAXE));
            var start=new PlayerInteractEvent.LeftClickBlock(p,at,Direction.SOUTH,PlayerInteractEvent.LeftClickBlock.Action.START);NeoForge.EVENT_BUS.post(start);
            h.assertTrue(!start.isCanceled()&&f.data().state(CaverVignette.ID).getInt("Work")==0,"holding native mining can begin without a canceled action or invented completed work");
            var wall=at.west();h.assertTrue(!p.gameMode.destroyBlock(wall)&&!f.l.getBlockState(wall).isAir(),"the rock beside the authored crack stays protected");
            h.assertTrue(p.gameMode.destroyBlock(at)&&f.l.getBlockState(at).isAir(),"actual survival mining removes the real cracked block");
            CaverVignette.onServerTick(new ServerTickEvent.Post(()->true,p.server));
            h.assertTrue(f.data().state(CaverVignette.ID).getInt("Work")==CaverVignette.STROKES&&CaverVignette.personal(f.data(),p.getUUID()).getBoolean("Worked"),"confirmed native removal opens the saved crawl gate and records the real miner");
            h.assertTrue(!CaverVignette.personal(f.data(),peer.getUUID()).getBoolean("Worked")&&WitnessAccount.count(f.data(),p.getUUID())==0&&WitnessAccount.count(f.data(),peer.getUUID())==0,"shared excavation transfers no journal work or ending credit to the peer");
            f.reload();h.assertTrue(f.data().state(CaverVignette.ID).getInt("Work")==CaverVignette.STROKES,"native excavation survives SavedData reload");
            var cache=f.l.getBlockEntity(f.b.offset(CaverCave.CACHE));h.assertTrue(cache instanceof net.minecraft.world.level.block.entity.BarrelBlockEntity barrel&&barrel.getItem(0).is(Items.IRON_PICKAXE)&&barrel.getItem(2).getCount()==6,"mining leaves the finite physical cache intact");h.succeed();
        });
    }

    @GameTest(template="empty",batch="caver_canceled_mining",timeoutTicks=100)
    public static void canceledNativeMiningAndSpectatorsCannotOpenTheSavedCrack(GameTestHelper h){
        canceled=new CaverTests.Fixture(h,32800);var f=canceled;var p=f.player();var at=f.b.offset(CaverCave.APERTURE);
        h.runAfterDelay(8,()->{
            f.at(p,.5,-3,-21.5);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_PICKAXE));
            refuse=e->{if(e.getPlayer()==p&&e.getPos().equals(at))e.setCanceled(true);};
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,false,BlockEvent.BreakEvent.class,refuse);
            h.assertTrue(!p.gameMode.destroyBlock(at),"a later native cancellation really prevents removal");
            CaverVignette.onServerTick(new ServerTickEvent.Post(()->true,p.server));
            h.assertTrue(!f.l.getBlockState(at).isAir()&&f.data().state(CaverVignette.ID).getInt("Work")==0&&!CaverVignette.personal(f.data(),p.getUUID()).getBoolean("Worked"),"even a cancellation after Ted's listener confers no saved excavation or personal work");
            NeoForge.EVENT_BUS.unregister(refuse);refuse=null;p.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);
            h.assertTrue(!CaverVignette.mayBreak(p,at)&&!CaverVignette.allowsPlacing(p,f.b.offset(0,-3,-20),Blocks.TORCH.defaultBlockState()),"native observers have no cave-building authority");h.succeed();
        });
    }

    private static InteractionResult place(ServerPlayer p,BlockPos support,Direction face){
        var hit=new BlockHitResult(support.getCenter().add(face.getStepX()*.5,face.getStepY()*.5,face.getStepZ()*.5),face,support,false);
        return p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,hit);
    }
    @GameTest(template="empty",batch="caver_native_torches",timeoutTicks=100)
    public static void twoSurvivalPlayersCanPlaceAndRecoverNativeFloorAndWallTorchesOnlyInsideTheCave(GameTestHelper h){
        torches=new CaverTests.Fixture(h,33100);var f=torches;var p=f.player();var peer=f.player();
        h.runAfterDelay(8,()->{
            f.at(p,-.5,0,-3.5);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.TORCH,6));
            var floor=f.b.offset(0,0,-5);h.assertTrue(place(p,floor.below(),Direction.UP).consumesAction()&&f.l.getBlockState(floor).is(Blocks.TORCH)&&p.getMainHandItem().getCount()==5,"the native placement event accepts a spent floor torch");
            f.at(peer,-3.5,0,-3.5);peer.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.SOUL_TORCH,4));
            var wall=f.b.offset(-5,1,-3);h.assertTrue(place(peer,wall.west(),Direction.EAST).consumesAction()&&f.l.getBlockState(wall).is(Blocks.SOUL_WALL_TORCH)&&peer.getMainHandItem().getCount()==3,"a second native player spends a real wall torch without a shared-placement conflict");
            h.assertTrue(p.gameMode.destroyBlock(floor)&&peer.gameMode.destroyBlock(wall)&&f.l.getBlockState(floor).isAir()&&f.l.getBlockState(wall).isAir(),"the players can mine and recover spent torches normally");
            h.assertTrue(!p.gameMode.destroyBlock(f.b.offset(CaverCave.JOURNAL))&&!p.gameMode.destroyBlock(f.b.offset(CaverCave.CACHE)),"original notebook and finite supplies stay protected");
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.COBBLESTONE,2));
            h.assertTrue(!place(p,floor.below(),Direction.UP).consumesAction()&&f.l.getBlockState(floor).isAir()&&p.getMainHandItem().getCount()==2,"arbitrary building is refused with the actual block and item restored");
            h.assertTrue(!CaverVignette.allowsPlacing(p,f.b.offset(0,0,1),Blocks.TORCH.defaultBlockState())&&!CaverVignette.allowsPlacing(p,f.b.offset(9,0,-4),Blocks.TORCH.defaultBlockState()),"the entrance door and sealed outer shell gain no torch exception");
            f.reload();h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0&&WitnessAccount.count(f.data(),peer.getUUID())==0&&f.data().state(CaverVignette.ID).getInt("Work")==0,"torch work never opens the squeeze or lends personal ending credit");h.succeed();
        });
    }
}
