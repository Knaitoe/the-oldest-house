package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.google.gson.*;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Native renderer review, explicitly staged: five skins, connected joints, fitted materials and rotating rollers. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BorrascaVisualProof extends Screen {
    private int page,frames;private BorrascaNativeArt.CastModel cast;private static boolean launched;
    private static final String[] STYLE_NAMES={"trailer_child_0","uncanny_waitress","literary_paw_sergeant","nightclub_dancer_vest","hotel_porter"};
    private java.util.List<JsonObject> styleRows;
    public BorrascaVisualProof(){super(Component.literal("Drisking / native materials and joints"));}
    @SubscribeEvent public static void launch(ClientTickEvent.Post ignored){var mc=Minecraft.getInstance();if(Boolean.getBoolean("the_oldest_house.borrascaSmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();NativeModelTextures.verify();mc.setScreen(new BorrascaVisualProof());}}
    @Override protected void init(){var mc=Minecraft.getInstance();cast=new BorrascaNativeArt.CastModel(mc.getEntityModels().bakeLayer(BorrascaNativeArt.CAST));for(int person=0;person<5;person++)if(mc.getResourceManager().getResource(BorrascaNativeArt.skin(person,person==4)).isEmpty())throw new IllegalStateException("Missing Drisking skin "+BorrascaNativeArt.skin(person,person==4));var books=new java.util.ArrayList<net.minecraft.world.item.ItemStack>();books.add(BorrascaTexts.source());books.add(BorrascaTexts.contentNote());books.add(BorrascaTexts.document("Evidence",true));for(String key:java.util.List.of("Bedroom","Register","Map","Service","Evidence","Testimony"))books.add(BorrascaTexts.document(key));for(int visit:new int[]{1,3,4})books.add(BorrascaTexts.board(visit));books.add(BorrascaTexts.notes(false));books.add(BorrascaTexts.notes(true));for(boolean stopped:new boolean[]{false,true}){var own=new net.minecraft.nbt.CompoundTag();own.putBoolean("PowerOff",stopped);books.add(BorrascaTexts.accountBook("Native reader",own));}for(var book:books)for(var page:book.get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).pages())if(mc.font.split(page.raw(),114).size()>14)throw new IllegalStateException("A Drisking document exceeds its native lectern page");}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff252925);g.drawString(font,"BORRASCA / "+(page<4?"four people and the plant's coveralls / "+new String[]{"front","side","back","dim"}[page]:page<8?"native materials / sheet "+(page-3)+" of 4":page==8?"Friend and Sheriff / native gestures":page==9?"original keepsakes / five bark states":"shared cast style / production meshes"),10,10,0xffe8ddc9,false);g.flush();var buffers=mc.renderBuffers().bufferSource();int light=page==3?LightTexture.pack(7,0):LightTexture.FULL_BRIGHT;
        if(page==10){styleComparison(g);return;}if(page==8){gestures(g,light);return;}if(page==9){keepsakes(g,light);return;}
        if(page<4){for(int person=0;person<5;person++)for(int stride=0;stride<3;stride++){var pose=g.pose();pose.pushPose();float row=(height-60)/3F,scale=Math.min(width/5*.35F,row*.42F);pose.translate((person+.5F)*width/5,40+stride*row+scale*.5F,200);pose.scale(scale,scale,scale);pose.mulPose(Axis.YP.rotationDegrees(new float[]{180,90,0,150}[page]));cast.preview(person,stride*7+frames,.7F);cast.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(BorrascaNativeArt.skin(person,person==4))),light,OverlayTexture.NO_OVERLAY,0xffffffff);buffers.endBatch();pose.popPose();}}
        else{int i=0;var kinds=BorrascaFittingBlock.Kind.values();for(int k=(page-4)*7;k<Math.min(kinds.length,(page-3)*7);k++)for(var facing:Direction.Plane.HORIZONTAL){var state=BorrascaFittingBlock.of(kinds[k],facing);if(mc.getBlockRenderer().getBlockModel(state)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing Borrasca fitting "+kinds[k]+" "+facing);var pose=g.pose();pose.pushPose();pose.translate(60+(i%4+.5F)*(width-100)/4,35+(i/4+.7F)*(height-90)/7,200);pose.scale(21,-21,21);pose.mulPose(Axis.XP.rotationDegrees(20));pose.mulPose(Axis.YP.rotationDegrees(30));mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,light,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();i++;}var pose=g.pose();pose.pushPose();pose.translate(width*.5F,height-35,210);pose.scale(35,-35,35);pose.mulPose(Axis.YP.rotationDegrees(35));BorrascaNativeArt.drawMachine(pose,buffers,light,OverlayTexture.NO_OVERLAY,frames*.12F+page);buffers.endBatch();pose.popPose();}
    }
    private void gestures(GuiGraphics g,int light){var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();int[][] used={{0,2,3,4,5},{0,1,2,4}};for(int row=0;row<2;row++)for(int col=0;col<used[row].length;col++){int gesture=used[row][col];g.drawString(font,(row==0?"Kimber":"Sheriff")+" / "+gesture,(int)((col+.15F)*width/5),38+row*(height-40)/2,0xffc8c5af,false);g.flush();var pose=g.pose();pose.pushPose();float scale=Math.min(width/5*.34F,(height-70)/2*.4F);pose.translate((col+.5F)*width/5,70+row*(height-40)/2+scale*.5F,200);pose.scale(scale,scale,scale);pose.mulPose(Axis.YP.rotationDegrees(160));cast.previewPose(row+1,gesture,frames);cast.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(BorrascaNativeArt.skin(row+1,false))),light,OverlayTexture.NO_OVERLAY,0xffffffff);buffers.endBatch();pose.popPose();}}
    private void keepsakes(GuiGraphics g,int light){var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();var items=new net.minecraft.world.item.Item[]{BorrascaRegistry.COMPASS.get(),BorrascaRegistry.PATCH.get(),BorrascaRegistry.FLASHLIGHT.get()};for(int i=0;i<items.length;i++){var pose=g.pose();pose.pushPose();pose.translate((i+.5F)*width/3-48,55,0);pose.scale(6,6,1);g.renderItem(new net.minecraft.world.item.ItemStack(items[i]),0,0);pose.popPose();}g.flush();for(int stage=0;stage<5;stage++){var pose=g.pose();pose.pushPose();pose.translate((stage+.5F)*width/5-28,height*.73F,200);pose.scale(56,-56,56);pose.mulPose(Axis.YP.rotationDegrees(-12));var state=BorrascaRegistry.CARVING.get().defaultBlockState().setValue(BorrascaCarvingBlock.STAGE,stage);mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,light,OverlayTexture.NO_OVERLAY);BorrascaCarvingRenderer.draw(font,BorrascaStory.FRIEND,"NativeReader_16",stage,pose,buffers,light);buffers.endBatch();pose.popPose();g.drawString(font,"Bark / "+stage,(int)((stage+.15F)*width/5),height-35,0xffc8c5af,false);}}
    private void styleComparison(GuiGraphics g){
        var mc=Minecraft.getInstance();
        if(styleRows==null){
            var rows=new java.util.HashMap<String,JsonObject>();
            try(var reader=mc.getResourceManager().openAsReader(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"character_art.json"))){
                for(var value:JsonParser.parseReader(reader).getAsJsonArray()){var row=value.getAsJsonObject();rows.put(row.get("name").getAsString(),row);}
            }catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}
            styleRows=new java.util.ArrayList<>();
            for(String name:STYLE_NAMES){var row=rows.get(name);if(row==null)throw new IllegalStateException("Missing established-cast comparison: "+name);styleRows.add(row);}
        }
        float cell=width/5F,rowHeight=(height-65)/2F;
        String[] names={"Kyle","Kimber","Sheriff","Prescott","Plant worker"};
        String[] references={"Camp cousin","Waitress","Sergeant","Club vest","Hotel porter"};
        for(int i=0;i<5;i++){
            float x=(i+.5F)*cell;
            g.drawCenteredString(font,names[i],(int)x,30,0xffe4d8bf);g.drawCenteredString(font,references[i],(int)x,(int)(44+rowHeight),0xffe4d8bf);g.flush();
            cast.preview(i,0,0);
            CharacterArtVisualProof.draw(g,cast,BorrascaNativeArt.skin(i,i==4),x,46+rowHeight*.47F,cell*.76F,rowHeight*.78F,165,0);
            var row=styleRows.get(i);var texture=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/"+row.get("name").getAsString()+".png");
            CharacterArtVisualProof.draw(g,CharacterArtVisualProof.model(row),texture,x,60+rowHeight*1.47F,cell*.76F,rowHeight*.78F,165,0);
        }
    }
    private static void namesVisible(NativeImage image){int cell=image.getWidth()/5;for(int[] pair:new int[][]{{0,1},{1,2},{3,4}}){int changed=0;for(int y=(int)(image.getHeight()*.60);y<(int)(image.getHeight()*.71);y++)for(int x=0;x<cell;x++)if(image.getPixelRGBA(x+pair[0]*cell,y)!=image.getPixelRGBA(x+pair[1]*cell,y))changed++;if(changed<20)throw new IllegalStateException("The native bark lettering is absent between states "+pair[0]+" and "+pair[1]+": "+changed+" changed pixels");}}
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{var mc=Minecraft.getInstance();boolean direct=Boolean.getBoolean("the_oldest_house.borrascaSmoke");if(!(direct||Boolean.getBoolean("the_oldest_house.fontSmoke"))||!(mc.screen instanceof BorrascaVisualProof s)||++s.frames<24)return;var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);String name=s.page<4?new String[]{"front","side","back","dim"}[s.page]:s.page<8?"fixtures-"+(s.page-4):s.page==8?"gestures":s.page==9?"keepsakes":"style";try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-borrasca-"+name+".png"));if(s.page==9)namesVisible(image);}if(++s.page<11){s.frames=0;return;}Files.writeString(dir.resolve("borrasca-assets-passed.txt"),"Eleven native GPU views: the four people and the plant worker on their own skins at three connected gait samples, front/side/back/dim; every fitted material in four facings on four bounded sheets; original machine roller at four continuous angles; all used Friend/Sheriff gestures; the three item models and five bark/name states through the production native font renderer, with visible lettering differences required on the actual frame. Direct comparison with five established cast members on their production layers. Staged assets, not a complete ordinary playthrough.\n");if(direct)mc.stop();else mc.setScreen(new HallChangeVisualProof());}
}
