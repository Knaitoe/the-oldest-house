package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * 0.4.758 (docs/BUILDING_OUTSIDE_0_4_758.md): the stories' pieces. Finishing a story teaches its reader the set and gives the keepsake
 * once; readers each get their own and spectators nothing; a stonecutter shows only known pieces; a piece set down outside the House
 * faces its placer, does nothing when used, and breaks back into itself.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_pieces") @PrefixGameTestTemplate(false)
public final class StoryPiecesTests {
    private StoryPiecesTests(){}
    private static final WitnessAccount.Story COLD=WitnessAccount.Story.COLD_ROOM,WAX=WitnessAccount.Story.WAXWORKS;
    private static int keepsakes(ServerPlayer p,StoryPieces.Piece keep){
        int n=0;var inv=p.getInventory();
        for(int i=0;i<inv.getContainerSize();i++){var s=inv.getItem(i);if(s.is(keep.block().asItem())&&java.util.Objects.equals(StoryPieces.kind(s),keep.kind())&&s.has(DataComponents.LORE))n+=s.getCount();}
        return n;
    }
    private static boolean knowsAll(ServerPlayer p,WitnessAccount.Story story){
        for(var piece:StoryPieces.pieces(story))if(!p.getRecipeBook().contains(piece.recipe()))return false;
        return true;
    }

    @GameTest(template="empty",batch="pieces")
    public static void everySetHasItemsRecipesAndAKeepsake(GameTestHelper h){
        var recipes=h.getLevel().getServer().getRecipeManager();
        for(var story:new WitnessAccount.Story[]{COLD,WAX}){
            var pieces=StoryPieces.pieces(story);
            h.assertTrue(pieces.size()>10,story.id+" has its pieces: "+pieces.size());
            for(var piece:pieces){
                h.assertTrue(piece.block().asItem() instanceof PieceItem,piece.recipe()+" has its piece item");
                var r=recipes.byKey(piece.recipe());
                h.assertTrue(r.isPresent(),piece.recipe()+" is cut at a stonecutter");
                var out=r.get().value().getResultItem(h.getLevel().registryAccess());
                h.assertTrue(out.is(piece.block().asItem())&&java.util.Objects.equals(StoryPieces.kind(out),piece.kind()),piece.recipe()+" cuts the piece it names");
                h.assertTrue(StoryPieces.state(out).is(piece.block()),piece.recipe()+" places its own block");
            }
            var keep=StoryPieces.keepsake(story);
            h.assertTrue(keep!=null,story.id+" names a keepsake among its pieces");
        }
        h.assertTrue("crucifix".equals(StoryPieces.keepsake(COLD).kind()),"the back bedroom's keepsake is the crucifix");
        h.assertTrue(StoryPieces.name(ColdRegistry.FITTING.get(),"kitchen_table").getString().equals("Kitchen table"),"a kind is named in words");
        h.succeed();
    }

    @GameTest(template="empty",batch="pieces")
    public static void finishingTeachesTheSetAndGivesTheKeepsakeOnce(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"piece_reader");
        try{
            var keep=StoryPieces.keepsake(COLD);
            h.assertTrue(!knowsAll(p,COLD)&&keepsakes(p,keep)==0,"nothing is known before the story ends");
            WitnessAccount.resolve(p,COLD,"read_the_rite");
            h.assertTrue(knowsAll(p,COLD),"every piece of the back bedroom is known");
            h.assertTrue(!p.getRecipeBook().contains(StoryPieces.pieces(WAX).get(0).recipe()),"the waxworks' pieces are not");
            h.assertTrue(keepsakes(p,keep)==1,"one keepsake is given");
            var lore=new ArrayList<String>();for(var s:p.getInventory().items)if(s.has(DataComponents.LORE))for(var line:s.get(DataComponents.LORE).lines())lore.add(line.getString());
            h.assertTrue(lore.stream().anyMatch(l->l.contains("nail")),"the keepsake carries its line: "+lore);
            WitnessAccount.resolve(p,COLD,"read_the_rite");StoryPieces.ended(p,COLD);
            h.assertTrue(keepsakes(p,keep)==1,"finishing again gives no second keepsake");
            var told=NativeTestPlayers.messages(p);
            h.assertTrue(told.stream().filter(m->m.contains("stonecutter")).count()==1,"the set is announced once: "+told);
        }finally{NativeTestPlayers.remove(p);}
        h.succeed();
    }

    @GameTest(template="empty",batch="pieces")
    public static void eachReaderGetsTheirOwnAndSpectatorsNothing(GameTestHelper h){
        var a=NativeTestPlayers.survival(h,"piece_a");var b=NativeTestPlayers.survival(h,"piece_b");var watcher=NativeTestPlayers.survival(h,"piece_watch");
        try{
            watcher.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);
            var keep=StoryPieces.keepsake(WAX);
            WitnessAccount.resolve(a,WAX,"kept_the_shift");
            h.assertTrue(knowsAll(a,WAX)&&keepsakes(a,keep)==1,"the first reader has the set and a keepsake");
            h.assertTrue(!knowsAll(b,WAX)&&keepsakes(b,keep)==0,"the second has nothing until their own story ends");
            WitnessAccount.resolveAs(b,WAX,"kept_the_shift",VignetteRole.WITNESS,a.getUUID().toString());
            h.assertTrue(knowsAll(b,WAX)&&keepsakes(b,keep)==1,"a witness to the ending gets their own");
            h.assertTrue(keepsakes(a,keep)==1,"and the first reader still has one");
            WitnessAccount.resolve(watcher,WAX,"kept_the_shift");StoryPieces.ended(watcher,WAX);
            h.assertTrue(!knowsAll(watcher,WAX)&&keepsakes(watcher,keep)==0,"a spectator gets nothing");
        }finally{NativeTestPlayers.remove(a);NativeTestPlayers.remove(b);NativeTestPlayers.remove(watcher);}
        h.succeed();
    }

    @GameTest(template="empty",batch="pieces")
    public static void aStonecutterShowsOnlyKnownPieces(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"piece_cutter");
        try{
            var menu=new StonecutterMenu(0,p.getInventory(),ContainerLevelAccess.NULL);
            menu.getSlot(0).set(new ItemStack(Items.SPRUCE_PLANKS));
            h.assertTrue(menu.getRecipes().stream().noneMatch(r->StoryPieces.isPieceRecipe(r.id())),"no piece is shown before its story ends");
            int ordinary=menu.getRecipes().size();
            StoryPieces.ended(p,COLD);
            menu.getSlot(0).set(new ItemStack(Items.OAK_PLANKS));
            var shown=menu.getRecipes();
            h.assertTrue(shown.stream().anyMatch(r->r.id().getPath().equals("piece/cold_room_fitting/crucifix")),"the crucifix is shown once known");
            h.assertTrue(shown.stream().filter(r->StoryPieces.isPieceRecipe(r.id())).allMatch(r->r.id().getPath().contains("cold_room")),"only the back bedroom's pieces are shown");
            h.assertTrue(shown.size()>ordinary,"the known pieces are added to the ordinary list");
            p.closeContainer();
        }finally{NativeTestPlayers.remove(p);}
        h.succeed();
    }

    @GameTest(template="empty",batch="pieces",timeoutTicks=200)
    public static void aPieceSetDownOutsideIsFurniture(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"piece_builder");var l=h.getLevel();
        var floor=h.absolutePos(new BlockPos(2,1,2));var at=floor.above();
        try{
            l.setBlockAndUpdate(floor,Blocks.STONE.defaultBlockState());l.setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());
            var stack=StoryPieces.keepsake(COLD).stack();p.setItemInHand(InteractionHand.MAIN_HAND,stack);
            p.moveTo(at.getX()+.5,at.getY(),at.getZ()+2.5,180,20);
            var hit=new BlockHitResult(Vec3.atCenterOf(floor).add(0,.5,0),Direction.UP,floor,false);
            var result=stack.useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,hit));
            h.assertTrue(result.consumesAction(),"the piece is set down: "+result);
            var s=l.getBlockState(at);
            h.assertTrue(ColdBlock.is(s,ColdBlock.Kind.CRUCIFIX),"it places the kind it carries: "+s);
            h.assertTrue(s.getValue(HorizontalDirectionalBlock.FACING)==Direction.SOUTH,"it faces whoever set it down: "+s);
            h.assertTrue(s.getDestroySpeed(l,at)>0,"it breaks like an ordinary block");
            var other=at.east(2);l.setBlockAndUpdate(other,ColdBlock.of(ColdBlock.Kind.CRUCIFIX,Direction.NORTH));
            h.assertTrue(l.getBlockState(other).getDestroySpeed(l,other)<0,"one the House set stays as hard as the House");
            var use=new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(Vec3.atCenterOf(at),Direction.NORTH,at,false));
            NeoForge.EVENT_BUS.post(use);
            h.assertTrue(use.getUseBlock()==TriState.FALSE,"using it does nothing");
            h.assertTrue(p.gameMode.destroyBlock(at),"it is broken");
            h.assertTrue(l.getBlockState(at).isAir(),"and gone");
            h.runAfterDelay(2,()->{
                var drops=l.getEntitiesOfClass(ItemEntity.class,new AABB(at).inflate(1.5));
                h.assertTrue(drops.stream().anyMatch(d->d.getItem().is(ColdRegistry.FITTING.get().asItem())&&"crucifix".equals(StoryPieces.kind(d.getItem()))),"it drops itself: "+drops);
                drops.forEach(d->d.discard());l.setBlockAndUpdate(other,Blocks.AIR.defaultBlockState());
                NativeTestPlayers.remove(p);h.succeed();
            });
        }catch(RuntimeException ex){NativeTestPlayers.remove(p);throw ex;}
    }
}
