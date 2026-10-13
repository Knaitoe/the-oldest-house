package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Ordinary handling after the scene's own puzzles/readers; none of these actions awards progress or supplies. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class VignetteInteractivity0492 {
    private VignetteInteractivity0492(){}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void use(PlayerInteractEvent.RightClickBlock e){
        if(e.isCanceled()||e.getHand()!=InteractionHand.MAIN_HAND||!(e.getEntity() instanceof ServerPlayer p)||!LiteraryVignettes.participant(p)||!p.getMainHandItem().isEmpty()||p.distanceToSqr(e.getPos().getCenter())>25)return;
        var o=HouseSavedData.get(p.server).houseOrigin();if(o==null)return;var place=LabyrinthPlaces.placeAt(o,p.blockPosition());if(place==null||!p.level().dimension().equals(NovelRooms.dimension(place)))return;
        var l=p.serverLevel();var at=e.getPos();if(!l.hasChunkAt(at))return;var s=l.getBlockState(at);
        var ray=e.getHitVec().getLocation().subtract(p.getEyePosition());var hit=l.clip(new ClipContext(p.getEyePosition(),e.getHitVec().getLocation().add(ray.normalize().scale(.02)),ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,p));if(hit.getType()!=HitResult.Type.BLOCK||!hit.getBlockPos().equals(at))return;
        boolean handled=false;
        if(s.is(HouseBlocks.SCENE_DETAIL.get())){
            var kind=s.getValue(SceneDetailBlock.KIND);
            if(kind==SceneDetailBlock.Kind.TABLE_LAMP){l.setBlock(at,s.cycle(SceneDetailBlock.LIT),18);l.playSound(null,at,SoundEvents.STONE_BUTTON_CLICK_ON,SoundSource.BLOCKS,.35F,s.getValue(SceneDetailBlock.LIT)?.8F:1.1F);handled=true;}
            else if(!kind.wall()&&kind!=SceneDetailBlock.Kind.CRATE&&kind!=SceneDetailBlock.Kind.FEED_SACK){
                var next=s.setValue(SceneDetailBlock.FACING,s.getValue(SceneDetailBlock.FACING).getClockWise());if(SceneDetailBlock.supported(l,at,next))l.setBlock(at,next,18);
                var sound=switch(kind){case BOOKS,FILE_TRAY,HYMNALS,INK_PAPERS->SoundEvents.BOOK_PAGE_TURN;case TEA_SET,BOTTLES,CROCK->SoundEvents.BOTTLE_EMPTY;default->SoundEvents.BUNDLE_INSERT;};l.playSound(null,at,sound,SoundSource.BLOCKS,.35F,1F);handled=true;
                String words=switch(kind){case BOOKS,HYMNALS->"You ease the book open, then leave it where it was.";case FILE_TRAY,INK_PAPERS->"You turn the loose sheets over. The ink has carried through.";case TEA_SET->"The cup is cold. You turn it by its handle.";case SATCHEL->"You test the clasp, then set the bag down again.";case MEDICAL_TRAY->"Metal clicks against enamel.";case TOOLS->"You lift a handle and put it back in its worn place.";default->"You turn it in your hands and set it back.";};p.displayClientMessage(Component.literal(words),true);
            }
        }else if(place==LabyrinthPlace.THREE_FINGERS&&CottageBlock.is(s,CottageBlock.Kind.CHESSBOARD)){
            l.setBlock(at,s.cycle(CottageBlock.STAGE),18);l.playSound(null,at,SoundEvents.WOOD_PLACE,SoundSource.BLOCKS,.4F,1.3F);p.displayClientMessage(Component.literal(s.getValue(CottageBlock.STAGE)==0?"You move the pawn. The open file points toward the black king.":"You put the pawn back. Nobody claims the next move."),true);handled=true;
        }
        if(handled){p.swing(InteractionHand.MAIN_HAND);e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}
    }
}
