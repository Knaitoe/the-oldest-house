package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.758 (docs/BUILDING_OUTSIDE_0_4_758.md): a story's piece as an item ({@link StoryPieces}). It places the kind it carries, turned
 * to face whoever sets it down (a thing against a wall faces away from the wall), and never inside the House.
 */
public final class PieceItem extends BlockItem {
    public PieceItem(Block block,Properties p){super(block,p);}
    @Override public Component getName(ItemStack s){return StoryPieces.name(getBlock(),StoryPieces.kind(s));}
    @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){
        var story=StoryPieces.story(getBlock());
        if(story!=null)lines.add(Component.literal(story.title).withStyle(ChatFormatting.DARK_GRAY));
    }
    @Override public InteractionResult place(BlockPlaceContext c){
        var level=c.getLevel();
        if(HouseDimensions.isHouseDimension(level.dimension())){
            if(c.getPlayer()!=null&&!level.isClientSide())c.getPlayer().displayClientMessage(Component.literal("It does not belong in here."),true);
            return InteractionResult.FAIL;
        }
        var at=c.getClickedPos();var done=super.place(c);
        if(done.consumesAction()&&level instanceof ServerLevel l&&StoryPieces.isPiece(l.getBlockState(at).getBlock()))PlacedPieces.add(l,at);
        return done;
    }
    @Override protected @Nullable BlockState getPlacementState(BlockPlaceContext c){
        var s=getBlock().getStateForPlacement(c);if(s==null)return null;
        var props=c.getItemInHand().get(DataComponents.BLOCK_STATE);if(props!=null)s=props.apply(s);
        // Every piece with a facing turns to whoever sets it down, whatever block class carries the property and whatever facing the
        // kind was recorded with in its story.
        var toward=c.getHorizontalDirection().getOpposite();
        if(s.hasProperty(BlockStateProperties.HORIZONTAL_FACING))s=s.setValue(BlockStateProperties.HORIZONTAL_FACING,toward);
        else if(s.hasProperty(BlockStateProperties.FACING)&&BlockStateProperties.FACING.getPossibleValues().contains(toward))s=s.setValue(BlockStateProperties.FACING,toward);
        return canPlace(c,s)?s:null;
    }
}
