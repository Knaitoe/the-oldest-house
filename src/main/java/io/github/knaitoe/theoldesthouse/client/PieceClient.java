package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.StoryPieces;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * 0.4.758 (docs/BUILDING_OUTSIDE_0_4_758.md): a story piece in the hand and the inventory is drawn as the block it places, in the kind
 * it carries (its item model is the shared item/story_piece, a block's display without a model of its own). The local reader's recipe
 * book decides what a stonecutter shows them.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class PieceClient {
    private PieceClient(){}
    private static BlockEntityWithoutLevelRenderer renderer;
    @SubscribeEvent public static void setup(FMLClientSetupEvent e){
        StoryPieces.clientKnows=(p,id)->p instanceof LocalPlayer local&&local.getRecipeBook().contains(id);
    }
    @SubscribeEvent public static void extensions(RegisterClientExtensionsEvent e){
        var items=StoryPieces.items();if(items.isEmpty())return;
        e.registerItem(new IClientItemExtensions(){
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){
                if(renderer==null){
                    var mc=Minecraft.getInstance();
                    renderer=new BlockEntityWithoutLevelRenderer(mc.getBlockEntityRenderDispatcher(),mc.getEntityModels()){
                        @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
                            drawModel(StoryPieces.state(stack),pose,buffers,light,overlay);
                        }
                    };
                }
                return renderer;
            }
        },items.toArray(Item[]::new));
    }
    /**
     * The block's baked model only. renderSingleBlock would hand a block drawn by its block entity back to the item renderer of
     * block.asItem(), which is this piece, and recurse until the stack overflows (run 1792, the Backseat clients). Such a block draws
     * whatever its baked model holds here.
     */
    static void drawModel(BlockState state,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(state.getRenderShape()==RenderShape.INVISIBLE)return;
        var mc=Minecraft.getInstance();var blocks=mc.getBlockRenderer();
        BakedModel model=blocks.getBlockModel(state);
        int color=mc.getBlockColors().getColor(state,null,null,0);
        float r=(color>>16&255)/255F,g=(color>>8&255)/255F,b=(color&255)/255F;
        for(RenderType type:model.getRenderTypes(state,RandomSource.create(42L),ModelData.EMPTY))
            blocks.getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderTypeHelper.getEntityRenderType(type,false)),state,model,r,g,b,light,overlay,ModelData.EMPTY,type);
    }
}
