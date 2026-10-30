package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * 0.4.168: the kitchen table's objects through the production renderer: the tin's two halves with one reader's crayons (one
 * taken), the drawing blank and coloured, the picture up on the fridge, the radio on and off, the kitchen clock and an office
 * clock with their moving hands (0.4.185); then the drawing in each of the
 * twelve skin crayons with the crayons themselves as they are held. Runs after the bathroom's hair in the same client, before the wardrobe.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HouseReadKitchenProof extends Screen {
    private static final String[] NAMES={"kitchen_objects","kitchen_tones","kitchen_drawn"};
    private int page,frames;
    public HouseReadKitchenProof(){super(Component.literal("The kitchen table"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        g.fill(0,0,width,height,0xff343630);g.drawString(font,"Kitchen table / "+NAMES[page].replace('_',' ')+" / shipped native models and the fittings' own colours",10,9,0xffe5dcc8,false);
        if(page==0)objects(g);else if(page==1)tones(g);else drawn(g);
    }
    private static BlockState part(HouseReadPropBlock.Part part){return HouseReadRegistry.FITTING.get().defaultBlockState().setValue(HouseReadPropBlock.PART,part).setValue(HorizontalDirectionalBlock.FACING,Direction.SOUTH);}
    private static CompoundTag afterFour(){var t=new CompoundTag();t.putString("Clock",HouseReadKitchen.CLOCK_AFTER_FOUR);return t;}
    private static CompoundTag drawing(int face,int hands,int hair,int clothes){var t=new CompoundTag();t.putInt("Face",face);t.putInt("Hands",hands);t.putInt("Hair",hair);t.putInt("Clothes",clothes);return t;}
    private void objects(GuiGraphics g){
        var reader=new UUID(168,7);
        var left=new CompoundTag();left.putIntArray("Colours",HouseReadKitchen.colours(reader,false));left.putInt("Taken",2);
        var right=new CompoundTag();right.putIntArray("Colours",HouseReadKitchen.colours(reader,true));right.putInt("Taken",-1);
        var coloured=drawing(4,4,12,18);
        record Shown(BlockState state,CompoundTag read,String label){}
        var shown=new Shown[]{new Shown(part(HouseReadPropBlock.Part.TIN_LEFT).setValue(HouseReadPropBlock.WRITING,true),left,"tin, left half (one taken)"),new Shown(part(HouseReadPropBlock.Part.TIN_RIGHT),right,"tin, right half"),
            new Shown(part(HouseReadPropBlock.Part.SHEET),drawing(-1,-1,-1,-1),"the drawing, in pencil"),new Shown(part(HouseReadPropBlock.Part.SHEET),coloured,"the drawing, coloured"),
            new Shown(part(HouseReadPropBlock.Part.FRIDGE),new CompoundTag(),"fridge, old drawings"),new Shown(part(HouseReadPropBlock.Part.FRIDGE_TOP).setValue(HouseReadPropBlock.TURNED,true),coloured,"fridge door, picture up"),
            new Shown(part(HouseReadPropBlock.Part.RADIO),new CompoundTag(),"radio, low"),new Shown(part(HouseReadPropBlock.Part.RADIO).setValue(HouseReadPropBlock.TURNED,true),new CompoundTag(),"radio, off"),
            new Shown(part(HouseReadPropBlock.Part.WALL_CLOCK),afterFour(),"kitchen clock"),new Shown(part(HouseReadPropBlock.Part.WALL_CLOCK),new CompoundTag(),"office clock")};
        int columns=5;float cw=width/(float)columns,ch=(height-36)/2f;
        for(int i=0;i<shown.length;i++){float cx=(i%columns+.5f)*cw,cy=32+(i/columns+.5f)*ch;block(g,shown[i].state(),shown[i].read(),cx,cy,Math.min(cw,ch)*.5f,shown[i].state().getValue(HouseReadPropBlock.PART).name().startsWith("FRIDGE")?12:shown[i].state().getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.WALL_CLOCK?0:62);
            g.drawCenteredString(font,shown[i].label(),(int)cx,(int)(32+(i/columns+1)*ch-10),0xffe5dcc8);}
    }
    /** Each of the twelve skin crayons on the drawing's face and hands, with the crayon beside it, and the eight others below. */
    private void tones(GuiGraphics g){
        float cw=width/6f,ch=(height-60)/2.4f;
        for(int i=0;i<12;i++){float cx=(i%6+.5f)*cw,cy=32+(i/6+.45f)*ch;block(g,part(HouseReadPropBlock.Part.SHEET),drawing(i,i,12+i%6,14+i%5),cx-cw*.12f,cy,Math.min(cw,ch)*.55f,80);
            crayon(g,i,cx+cw*.3f,cy,Math.min(cw,ch)*.35f);g.drawCenteredString(font,HouseReadKitchen.hex(i),(int)cx,(int)(32+(i/6+1)*ch-12),0xffa59d8c);}
        for(int i=0;i<8;i++){float cx=(i+.5f)*width/8f,cy=height-30;crayon(g,12+i,cx,cy,Math.min(width/8f,30));}
    }
    /**
     * 0.4.196: the pencil's choices on the drawing (no face and nothing below the shirt; each of the five faces; trousers and a
     * dress, the pencil in the margin and taken), the picture on the fridge, the pencil in the hand; then a wide and a slim
     * default skin as drawn: toned, the arms refitted to the other model and the drawn face put on.
     */
    private void drawn(GuiGraphics g){
        var sheets=new java.util.ArrayList<CompoundTag>();
        var blank=drawing(-1,-1,-1,-1);blank.putInt("Body",0);blank.putInt("Mood",0);sheets.add(blank);
        for(int mood=1;mood<=HouseReadKitchen.FACES.size();mood++){var t=drawing(mood*2,mood*2,12+mood,14+mood%4);t.putInt("Mood",mood);t.putInt("Body",1+mood%2);t.putBoolean("PencilTaken",mood%2==0);sheets.add(t);}
        float cw=width/7f,ch=(height-40)/2.2f;
        for(int i=0;i<sheets.size();i++){float cx=(i+.5f)*cw,cy=32+ch*.5f;block(g,part(HouseReadPropBlock.Part.SHEET),sheets.get(i),cx,cy,Math.min(cw,ch)*.55f,80);
            g.drawCenteredString(font,i==0?"no face yet":HouseReadKitchen.FACES.get(i-1)+(sheets.get(i).getInt("Body")==2?", dress":", trousers"),(int)cx,(int)(32+ch-10),0xffe5dcc8);}
        var hung=sheets.get(2).copy();block(g,part(HouseReadPropBlock.Part.FRIDGE_TOP).setValue(HouseReadPropBlock.TURNED,true),hung,6.5f*cw,32+ch*.5f,Math.min(cw,ch)*.5f,12);
        var pencil=new ItemStack(HouseReadRegistry.CRAYON.get());pencil.set(DataComponents.CUSTOM_MODEL_DATA,new net.minecraft.world.item.component.CustomModelData(1));
        var p=g.pose();float scale=Math.min(cw,ch)*.3f/16f;p.pushPose();p.translate(6.5f*cw-8*scale,32+ch-30-8*scale,0);p.scale(scale,scale,1);g.renderItem(pencil,0,0);p.popPose();
        float top=36+ch,size=Math.min((height-top-24)/1f,width/7.5f);int column=0;
        for(var shown:skins()){float x=8+column*(size+10);g.blit(shown.texture(),(int)x,(int)top,(int)size,(int)size,0,0,64,64,64,64);g.drawString(font,shown.label(),(int)x,(int)(top+size+2),0xffe5dcc8,false);column++;}
    }
    private record Skin(net.minecraft.resources.ResourceLocation texture,String label){}
    private java.util.List<Skin> skins;
    /** The default skins drawn as a reader drew them, registered once for the page. */
    private java.util.List<Skin> skins(){
        if(skins!=null)return skins;skins=new java.util.ArrayList<>();var mc=Minecraft.getInstance();
        for(var model:net.minecraft.client.resources.PlayerSkin.Model.values()){
            net.minecraft.client.resources.PlayerSkin skin=null;for(long i=0;i<64&&(skin==null||skin.model()!=model);i++)skin=net.minecraft.client.resources.DefaultPlayerSkin.get(new UUID(196,i));
            if(skin==null||skin.model()!=model)continue;
            try(var in=mc.getResourceManager().open(skin.texture());var image=com.mojang.blaze3d.platform.NativeImage.read(in)){
                var other=model==net.minecraft.client.resources.PlayerSkin.Model.SLIM?net.minecraft.client.resources.PlayerSkin.Model.WIDE:net.minecraft.client.resources.PlayerSkin.Model.SLIM;
                int[][] cases={{-1,0},{HouseReadKitchen.rgb(1),2},{HouseReadKitchen.rgb(10),1},{-1,3},{-1,4}};
                for(int k=0;k<cases.length;k++){
                    var drawn=k==0?copy(image):HouseBodyClient.drawn(image,cases[k][0],model,k==1?other:model,cases[k][1]);
                    var at=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"proof/kitchen_skin_"+model.id()+"_"+k);
                    mc.getTextureManager().register(at,new net.minecraft.client.renderer.texture.DynamicTexture(drawn));
                    skins.add(new Skin(at,model.id()+(k==0?" own":k==1?" to "+other.id()+", toned, "+HouseReadKitchen.FACES.get(cases[k][1]-1):", "+(cases[k][0]>=0?"toned, ":"")+HouseReadKitchen.FACES.get(cases[k][1]-1))));
                }
            }catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}
        }
        return skins;
    }
    private static com.mojang.blaze3d.platform.NativeImage copy(com.mojang.blaze3d.platform.NativeImage image){var out=new com.mojang.blaze3d.platform.NativeImage(image.getWidth(),image.getHeight(),true);out.copyFrom(image);return out;}
    private void crayon(GuiGraphics g,int colour,float x,float y,float size){
        var stack=new ItemStack(HouseReadRegistry.CRAYON.get());stack.set(DataComponents.DYED_COLOR,new DyedItemColor(HouseReadKitchen.rgb(colour),false));
        var p=g.pose();float scale=size/16f;p.pushPose();p.translate(x-8*scale,y-8*scale,0);p.scale(scale,scale,1);g.renderItem(stack,0,0);p.popPose();
    }
    /** A fitting as it stands, with its block entity's own drawing over the model. */
    private void block(GuiGraphics g,BlockState state,CompoundTag read,float x,float y,float scale,float pitch){
        var mc=Minecraft.getInstance();var p=g.pose();g.flush();var out=mc.renderBuffers().bufferSource();
        p.pushPose();p.translate(x,y,180);p.scale(scale,-scale,scale);p.mulPose(Axis.XP.rotationDegrees(pitch));p.mulPose(Axis.YP.rotationDegrees(state.getValue(HouseReadPropBlock.PART).name().startsWith("FRIDGE")?-20:0));p.translate(-.5,-.5,-.5);
        mc.getBlockRenderer().renderSingleBlock(state,p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
        var fitting=new HouseReadPropBlock.Fitting(BlockPos.ZERO,state);fitting.setRead(read);
        if(HouseReadKitchenClient.draws(state.getValue(HouseReadPropBlock.PART)))HouseReadKitchenClient.render(fitting,p,out,LightTexture.FULL_BRIGHT);
        // 0.4.185: the clocks' moving hands, through the same renderer as in the room.
        if(state.getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.WALL_CLOCK)ClockHands.wall(fitting,p,out,LightTexture.FULL_BRIGHT);
        out.endBatch();p.popPose();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof HouseReadKitchenProof s)||++s.frames<12)return;
        var folder=Path.of("../build/house-read-kitchen");Files.createDirectories(folder);
        try(var image=net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(folder.resolve(NAMES[s.page]+".png"));}
        if(++s.page<NAMES.length){s.frames=0;return;}
        Files.writeString(folder.resolve("passed.txt"),"The kitchen table's tin, drawing, fridge, radio and the clocks' moving hands through the production model and fitting renderer, one reader's crayons with one taken, the picture on the fridge door, the drawing in each of the twelve skin crayons with the held crayons, and (0.4.196) the pencil's five faces, trousers and a dress, the pencil in the hand, and default skins toned, refitted between arm widths and given the drawn faces.\n");
        mc.setScreen(new HouseReadWardrobeProof());
    }
}
