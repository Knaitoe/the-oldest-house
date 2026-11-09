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
    private static final String[] STYLE_NAMES={"trailer_child_0","uncanny_waitress","literary_paw_sergeant","nightclub_dancer_vest","hotel_porter","literary_window_cleaner"};
    private java.util.List<JsonObject> styleRows;
    public BorrascaVisualProof(){super(Component.literal("Drisking / native materials and joints"));}
    @SubscribeEvent public static void launch(ClientTickEvent.Post ignored){var mc=Minecraft.getInstance();if(Boolean.getBoolean("the_oldest_house.borrascaSmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();NativeModelTextures.verify();mc.setScreen(new BorrascaVisualProof());}}
    @Override protected void init(){var mc=Minecraft.getInstance();cast=new BorrascaNativeArt.CastModel(mc.getEntityModels().bakeLayer(BorrascaNativeArt.CAST));for(int person=0;person<6;person++)if(mc.getResourceManager().getResource(BorrascaNativeArt.skin(person)).isEmpty())throw new IllegalStateException("Missing Drisking skin "+BorrascaNativeArt.skin(person));var books=new java.util.ArrayList<net.minecraft.world.item.ItemStack>();books.add(BorrascaTexts.source());books.add(BorrascaTexts.contentNote());books.add(BorrascaTexts.document("Evidence",true));for(String key:java.util.List.of("Map","Evidence","Ledger","Letter"))books.add(BorrascaTexts.document(key));for(int visit:new int[]{1,3,4})books.add(BorrascaTexts.board(visit));books.add(BorrascaTexts.notes(false));books.add(BorrascaTexts.notes(true));for(boolean stopped:new boolean[]{false,true}){var own=new net.minecraft.nbt.CompoundTag();own.putBoolean("PowerOff",stopped);books.add(BorrascaTexts.accountBook("Native reader",own));}for(var book:books)for(var page:book.get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).pages())if(mc.font.split(page.raw(),114).size()>14)throw new IllegalStateException("A Drisking document exceeds its native lectern page");}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff252925);g.drawString(font,"BORRASCA / "+(page<4?"five people and the plant's coveralls / "+new String[]{"front","side","back","dim"}[page]:page<8?"native materials / sheet "+(page-3)+" of 4":page==8?"Friend and Sheriff / native gestures":page==9?"original keepsakes / five bark states":page==10?"shared cast style / production meshes":page==11?"the Shiny Gentleman / in the pines at dusk":page==12?"a child's bike / parked, and ridden: upright, leaning each way":page==14?"the last night at the machine / on the belt, and what is left":page==15?"the last night at the machine / two seconds, the machine is him":page==16?"the machine at the plant / the cone, its arms and hoops turning":"the drugstore cabinets / DEEP SEAM and BIG CAT, mid-game"),10,10,0xffe8ddc9,false);g.flush();var buffers=mc.renderBuffers().bufferSource();int light=page==3?LightTexture.pack(7,0):LightTexture.FULL_BRIGHT;
        if(page==10){styleComparison(g);return;}if(page==11){gentleman(g);return;}if(page==12){bike(g);return;}if(page==13){arcade(g);return;}if(page==14){feeding(g);return;}if(page==15){devoured(g);return;}if(page==16){orrery(g);return;}if(page==8){gestures(g,light);return;}if(page==9){keepsakes(g,light);return;}
        if(page<4){int[] shown={0,1,8,2,3,4,5};for(int column=0;column<shown.length;column++)for(int stride=0;stride<3;stride++){int person=shown[column];var pose=g.pose();pose.pushPose();float row=(height-60)/3F,scale=Math.min(width/7*.35F,row*.42F);pose.translate((column+.5F)*width/7,40+stride*row+scale*.5F,200);pose.scale(scale,scale,scale);pose.mulPose(Axis.YP.rotationDegrees(new float[]{180,90,0,150}[page]));cast.preview(person,stride*7+frames,.7F);cast.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(BorrascaNativeArt.skin(person))),light,OverlayTexture.NO_OVERLAY,0xffffffff);buffers.endBatch();pose.popPose();}}
        else{int i=0;var kinds=BorrascaFittingBlock.Kind.values();for(int k=(page-4)*7;k<Math.min(kinds.length,(page-3)*7);k++)for(var facing:Direction.Plane.HORIZONTAL){var state=BorrascaFittingBlock.of(kinds[k],facing);if(mc.getBlockRenderer().getBlockModel(state)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing Borrasca fitting "+kinds[k]+" "+facing);var pose=g.pose();pose.pushPose();pose.translate(60+(i%4+.5F)*(width-100)/4,35+(i/4+.7F)*(height-90)/7,200);pose.scale(21,-21,21);pose.mulPose(Axis.XP.rotationDegrees(20));pose.mulPose(Axis.YP.rotationDegrees(30));mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,light,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();i++;}var pose=g.pose();pose.pushPose();pose.translate(width*.5F,height-35,210);pose.scale(35,-35,35);pose.mulPose(Axis.YP.rotationDegrees(35));BorrascaNativeArt.drawMachine(pose,buffers,light,OverlayTexture.NO_OVERLAY,frames*.12F+page);buffers.endBatch();pose.popPose();}
    }
    private void gestures(GuiGraphics g,int light){var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();int[][] used={{0,2,3,4,5,6},{0,1,2,4}};for(int row=0;row<2;row++)for(int col=0;col<used[row].length;col++){int gesture=used[row][col];g.drawString(font,(row==0?BorrascaStory.FRIEND:"Sheriff")+" / "+gesture,(int)((col+.15F)*width/6),38+row*(height-40)/2,0xffc8c5af,false);g.flush();var pose=g.pose();pose.pushPose();float scale=Math.min(width/6*.34F,(height-70)/2*.4F);pose.translate((col+.5F)*width/6,70+row*(height-40)/2+scale*.5F,200);pose.scale(scale,scale,scale);pose.mulPose(Axis.YP.rotationDegrees(160));cast.previewPose(row+1,gesture,frames);cast.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(BorrascaNativeArt.skin(row+1,false))),light,OverlayTexture.NO_OVERLAY,0xffffffff);buffers.endBatch();pose.popPose();}}
    private void keepsakes(GuiGraphics g,int light){var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();var items=new net.minecraft.world.item.Item[]{BorrascaRegistry.COMPASS.get(),BorrascaRegistry.PATCH.get(),BorrascaRegistry.FLASHLIGHT.get()};for(int i=0;i<items.length;i++){var pose=g.pose();pose.pushPose();pose.translate((i+.5F)*width/3-48,55,0);pose.scale(6,6,1);g.renderItem(new net.minecraft.world.item.ItemStack(items[i]),0,0);pose.popPose();}g.flush();for(int stage=0;stage<5;stage++){var pose=g.pose();pose.pushPose();pose.translate((stage+.5F)*width/5-28,height*.73F,200);pose.scale(56,-56,56);pose.mulPose(Axis.YP.rotationDegrees(-12));var state=BorrascaRegistry.CARVING.get().defaultBlockState().setValue(BorrascaCarvingBlock.STAGE,stage);mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,light,OverlayTexture.NO_OVERLAY);BorrascaCarvingRenderer.draw(font,BorrascaStory.FRIEND,"NativeReader_16",stage,pose,buffers,light);buffers.endBatch();pose.popPose();g.drawString(font,"Bark / "+stage,(int)((stage+.15F)*width/5),height-35,0xffc8c5af,false);}}
    /** 0.4.439: Dana laid on the belt as BorrascaFeeding lays her (feet north, the production model's lying pose), a smear on the rubber at the
     * end, her two faces standing beside, and the pile's pieces and the belt smears in a row below. */
    /**
     * 0.4.439: the feeding. Four times on the belt, as the production cast model draws her: laid out whole, and at the blade with the
     * machine at her knees and at her belly (alive, thrashing) and at her chest (after). The cut is where the slot would be. Her three
     * faces standing (ahead, gone to one side, screaming), her scream as the caption draws it, and what is left on the pile.
     */
    private void feeding(GuiGraphics g){var mc=Minecraft.getInstance();g.fill(0,24,width,height,0xff2c2a26);g.flush();var buffers=mc.renderBuffers().bufferSource();
        float[][] stages={{0,1},{9,1},{17,1},{21,0}};
        for(int n=0;n<stages.length;n++){var pose=g.pose();float s=height*.12F;pose.pushPose();pose.translate(width*(.17F+.3F*(n%2)),height*(.40F+.30F*(n/2)),200);pose.scale(s,-s,s);pose.mulPose(Axis.XP.rotationDegrees(32));pose.mulPose(Axis.YP.rotationDegrees(-50));
            for(int z=-2;z<=1;z++){pose.pushPose();pose.translate(-.5F,0,z);mc.getBlockRenderer().renderSingleBlock(BorrascaFittingBlock.of(BorrascaFittingBlock.Kind.BELT,Direction.NORTH),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
                if(n>0&&z>=-1&&z<=0){pose.translate(0,9/16F+.002F,0);mc.getBlockRenderer().renderSingleBlock(BorrascaShopBlock.of(BorrascaShopBlock.Kind.SMEAR,Direction.NORTH,z==-1?1:0),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);}pose.popPose();}
            buffers.endBatch();
            float consumed=stages[n][0];boolean living=stages[n][1]>0;
            pose.pushPose();pose.translate(0,9/16F+.13F,-1F-consumed/16F);pose.mulPose(Axis.XP.rotationDegrees(90));pose.scale(-1,-1,1);pose.translate(0,-1.501F,0);
            if(n==0)cast.previewLaid(frames);else cast.previewTorn(frames,consumed,living);
            cast.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(BorrascaNativeArt.skin(n==0?9:11))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);buffers.endBatch();pose.popPose();pose.popPose();}
        for(int i=0;i<3;i++){cast.preview(9,0,0);CharacterArtVisualProof.draw(g,cast,BorrascaNativeArt.skin(9+i),width*(.70F+.11F*i),height*.42F,width*.10F,height*.40F,165,0);}
        var line=net.minecraft.network.chat.Component.literal("AAAHHHHHHH").getVisualOrderText();int lx=width*3/4-font.width(line)/2;
        g.fill(lx-6,30,lx+font.width(line)+6,46,0x96000000);SpeechCaptions.shaking(g,line,lx,34,0xffffffff,2.4F,frames,0);
        Object[][] pieces={{BorrascaShopBlock.Kind.REMAINS,0},{BorrascaShopBlock.Kind.REMAINS,1},{BorrascaShopBlock.Kind.WRISTBAND,0},{BorrascaShopBlock.Kind.SMEAR,0},{BorrascaShopBlock.Kind.SMEAR,1}};
        for(int i=0;i<pieces.length;i++){var pose=g.pose();pose.pushPose();float b=height*.09F;pose.translate(width*(.62F+.08F*i),height-20,200);pose.scale(b,-b,b);pose.mulPose(Axis.XP.rotationDegrees(35));pose.mulPose(Axis.YP.rotationDegrees(30));pose.translate(-.5F,0,-.5F);
            mc.getBlockRenderer().renderSingleBlock(BorrascaShopBlock.of((BorrascaShopBlock.Kind)pieces[i][0],Direction.NORTH,(Integer)pieces[i][1]),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();}}
    /** 0.4.439: the plant round the machine block's corner (the pose's origin): the belt through it, the housing's walls, the drum. */
    private static void plant(com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource.BufferSource buffers,float angle){var mc=Minecraft.getInstance();
        for(int z=-6;z<=8;z++){pose.pushPose();pose.translate(0,0,z);mc.getBlockRenderer().renderSingleBlock(BorrascaFittingBlock.of(BorrascaFittingBlock.Kind.BELT,Direction.NORTH),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();}
        for(int y=1;y<=4;y++)for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)if(Math.abs(x)==2||Math.abs(z)==2){pose.pushPose();pose.translate(x,y,z);mc.getBlockRenderer().renderSingleBlock(BorrascaFittingBlock.of(BorrascaFittingBlock.Kind.POLISHED,Direction.NORTH),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();}
        buffers.endBatch();BorrascaNativeArt.drawMachine(pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,angle);buffers.endBatch();}
    /** 0.4.439: the two seconds at the machine, as its renderer draws them (BorrascaOrrery.devour): the cone gone and him in its place, her at the slot, the red. */
    private void devoured(GuiGraphics g){var mc=Minecraft.getInstance();g.fill(0,24,width,height,0xff1d1a17);g.flush();var buffers=mc.renderBuffers().bufferSource();
        var pose=g.pose();float s=height*.05F;pose.pushPose();pose.translate(width*.5F,height*.9F,400);pose.scale(s,-s,s);pose.mulPose(Axis.XP.rotationDegrees(12));pose.mulPose(Axis.YP.rotationDegrees(-70));pose.translate(-.5F,0,-1.5F);
        plant(pose,buffers,frames*.12F);
        float consumed=(float)(BorrascaFeeding.GENTLEMAN_AT*16);var slot=new net.minecraft.world.phys.Vec3(.5,BorrascaOrrery.BELT_TOP+.25,BorrascaOrrery.SLOT_Z);
        pose.pushPose();pose.translate(slot.x,slot.y-.25F+.13F,slot.z-consumed/16F);pose.mulPose(Axis.XP.rotationDegrees(90));pose.scale(-1,-1,1);pose.translate(0,-1.501F,0);
        cast.previewTorn(frames,consumed,true);cast.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(BorrascaNativeArt.skin(11))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);buffers.endBatch();pose.popPose();
        BorrascaOrrery.mouth(pose,buffers,LightTexture.FULL_BRIGHT);buffers.endBatch();
        BorrascaOrrery.devour(pose,buffers,slot,14,frames,BorrascaNativeArt.skin(11));buffers.endBatch();
        pose.popPose();
        g.fill(0,24,width,height,(int)(.8F*95)<<24|0x6E120D);}
    /** 0.4.439: the machine as it stands at the plant, as its renderer draws it: the cone on its housing, the arms and hoops at three moments of turning. */
    private void orrery(GuiGraphics g){g.fillGradient(0,24,width,height,0xff2b3140,0xff15130f);g.flush();var buffers=Minecraft.getInstance().renderBuffers().bufferSource();
        for(int i=0;i<3;i++){var pose=g.pose();float s=(height-50)/20F*(i==0?1:.5F);pose.pushPose();
            pose.translate(i==0?width*.32F:width*.76F,i==1?height*.52F:height-14,400);pose.scale(s,-s,s);pose.mulPose(Axis.XP.rotationDegrees(10));pose.mulPose(Axis.YP.rotationDegrees(-150+50*i));pose.translate(-.5F,0,-.5F);
            plant(pose,buffers,frames*.12F+i);BorrascaOrrery.draw(pose,buffers,LightTexture.FULL_BRIGHT,frames*.12+i*9,i==2?.2F:1,frames+i*40);BorrascaOrrery.mouth(pose,buffers,LightTexture.FULL_BRIGHT);buffers.endBatch();pose.popPose();}}
    /** 0.4.436: the two cabinets, each half by its block model, and a frame of each game played a while with a steady hand. */
    private void arcade(GuiGraphics g){var mc=Minecraft.getInstance();g.fill(0,24,width,height,0xff3a3430);g.flush();var buffers=mc.renderBuffers().bufferSource();
        for(int game=0;game<2;game++){var pose=g.pose();float s=height*.17F;pose.pushPose();pose.translate(width*(.12F+.2F*game),height-34,200);pose.scale(s,-s,s);pose.mulPose(Axis.XP.rotationDegrees(12));pose.mulPose(Axis.YP.rotationDegrees(200));pose.translate(-.5F,0,-.5F);
            mc.getBlockRenderer().renderSingleBlock(BorrascaShopBlock.of(BorrascaShopBlock.Kind.CABINET,Direction.NORTH,game),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.translate(0,1,0);
            mc.getBlockRenderer().renderSingleBlock(BorrascaShopBlock.of(BorrascaShopBlock.Kind.CABINET_TOP,Direction.NORTH,game),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();}
        for(int game=0;game<2;game++){var play=BorrascaArcade.of(game,434L+game,null);for(int t=0;t<600&&!play.over();t++)play.tick(game==0?(t%90==30?BorrascaArcade.UP:t%90==60?BorrascaArcade.DOWN:t%45==10?BorrascaArcade.SPACE:0):(t%120<70?0:BorrascaArcade.SPACE)|(t%200<100?BorrascaArcade.LEFT:BorrascaArcade.RIGHT));
            var pose=g.pose();pose.pushPose();float s=Math.min(width*.5F/BorrascaArcade.W,(height-70)/2F/BorrascaArcade.H);int x=(int)(width-BorrascaArcade.W*s-12),y=(int)(32+game*(BorrascaArcade.H*s+6));pose.translate(x,y,0);pose.scale(s,s,1);g.enableScissor(x,y,(int)(x+BorrascaArcade.W*s),(int)(y+BorrascaArcade.H*s));BorrascaArcadeScreen.draw(g,font,play,frames);g.disableScissor();pose.popPose();}}
    /** 0.4.435: the parked bike block on its kickstand, and the ridden bike through its renderer: upright, leaning left with the cranks a quarter round, leaning right with the wheels turned. */
    private void bike(GuiGraphics g){var mc=Minecraft.getInstance();g.fill(0,24,width,height,0xff6d8496);g.fill(0,height-40,width,height,0xff5f513e);g.flush();var buffers=mc.renderBuffers().bufferSource();
        var parked=BorrascaShopBlock.of(BorrascaShopBlock.Kind.BIKE,net.minecraft.core.Direction.NORTH,0);var pose=g.pose();pose.pushPose();pose.translate(width*.08F,height-40,200);float s=width*.13F;pose.scale(s,-s,s);pose.mulPose(Axis.XP.rotationDegrees(10));mc.getBlockRenderer().renderSingleBlock(parked,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();
        float[][] views={{-90,0,0,0},{-55,-.6F,1.2F,(float)Math.PI/2},{-125,.85F,2.4F,(float)Math.PI}};
        for(int i=0;i<views.length;i++){pose.pushPose();pose.translate(width*(.42F+.22F*i),height-40,200);pose.scale(s,-s,s);pose.mulPose(Axis.XP.rotationDegrees(10));BorrascaBikeRenderer.draw(pose,buffers.getBuffer(Sheets.cutoutBlockSheet()),views[i][0],views[i][1],views[i][2],views[i][3],LightTexture.FULL_BRIGHT);buffers.endBatch();pose.popPose();}}
    /** 0.4.236: him, as the production renderer draws him, against a dusk sky between two pines, and once small, as far off as he stands. */
    private void gentleman(GuiGraphics g){var mc=Minecraft.getInstance();g.fillGradient(0,24,width,height,0xff3a3f52,0xff1b2420);for(int side:new int[]{-1,1})for(int k=0;k<6;k++){int cx=width/2+side*(int)(width*.3F),w=12+k*9;g.fill(cx-w/2,40+k*22,cx+w/2,62+k*22,0xff142218);}g.fill(0,height-28,width,height,0xff1d2a1c);g.flush();
        var buffers=mc.renderBuffers().bufferSource();for(int i=0;i<2;i++){var pose=g.pose();pose.pushPose();float scale=i==0?(height-80)/(float)BorrascaStory.GENT_HEIGHT:14;pose.translate(i==0?width/2F:width*.82F,height-28,200);pose.scale(scale,-scale,scale);if(i==1)pose.mulPose(Axis.YP.rotationDegrees(35));BorrascaAtmosphere.drawGentleman(pose,buffers,1F,frames,1);pose.popPose();}}
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
        float cell=width/6F,rowHeight=(height-65)/2F;
        String[] names={"Kyle","Kelly","Sheriff","Prescott","Plant worker","Second worker"};
        String[] references={"Camp cousin","Waitress","Sergeant","Club vest","Hotel porter","Window cleaner"};
        for(int i=0;i<6;i++){
            float x=(i+.5F)*cell;
            g.drawCenteredString(font,names[i],(int)x,30,0xffe4d8bf);g.drawCenteredString(font,references[i],(int)x,(int)(44+rowHeight),0xffe4d8bf);g.flush();
            cast.preview(i,0,0);
            CharacterArtVisualProof.draw(g,cast,BorrascaNativeArt.skin(i),x,46+rowHeight*.47F,cell*.76F,rowHeight*.78F,165,0);
            var row=styleRows.get(i);var texture=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/"+row.get("name").getAsString()+".png");
            CharacterArtVisualProof.draw(g,CharacterArtVisualProof.model(row),texture,x,60+rowHeight*1.47F,cell*.76F,rowHeight*.78F,165,0);
        }
    }
    private static void namesVisible(NativeImage image){int cell=image.getWidth()/5;for(int[] pair:new int[][]{{0,1},{1,2},{3,4}}){int changed=0;for(int y=(int)(image.getHeight()*.60);y<(int)(image.getHeight()*.71);y++)for(int x=0;x<cell;x++)if(image.getPixelRGBA(x+pair[0]*cell,y)!=image.getPixelRGBA(x+pair[1]*cell,y))changed++;if(changed<20)throw new IllegalStateException("The native bark lettering is absent between states "+pair[0]+" and "+pair[1]+": "+changed+" changed pixels");}}
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{var mc=Minecraft.getInstance();boolean direct=Boolean.getBoolean("the_oldest_house.borrascaSmoke");if(!(direct||Boolean.getBoolean("the_oldest_house.fontSmoke"))||!(mc.screen instanceof BorrascaVisualProof s)||++s.frames<24)return;var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);String name=s.page<4?new String[]{"front","side","back","dim"}[s.page]:s.page<8?"fixtures-"+(s.page-4):s.page==8?"gestures":s.page==9?"keepsakes":s.page==10?"style":s.page==11?"gentleman":s.page==12?"bike":s.page==13?"arcade":s.page==14?"feeding":s.page==15?"devoured":"orrery";try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-borrasca-"+name+".png"));if(s.page==9)namesVisible(image);}if(++s.page<17){s.frames=0;return;}Files.writeString(dir.resolve("borrasca-assets-passed.txt"),"Seventeen native GPU views: the four people, the friend grown, and the two plant workers on their own skins at three connected gait samples, front/side/back/dim; every fitted material in four facings on four bounded sheets; original machine roller at four continuous angles; all used Friend/Sheriff gestures, the friend led by the arm among them; the three item models and five bark/name states through the production native font renderer, with visible lettering differences required on the actual frame. Direct comparison with six established cast members on their production layers. The Shiny Gentleman through the production renderer, near and far, against a dusk sky. A child's bike parked, and ridden through the production renderer: upright, and leaning each way with its wheels and cranks turned. The drugstore's two arcade cabinets, and a frame of each of their games part way through, drawn by the production screen. The woman on the belt as the production cast model lays her: whole, her arms over its sides; then at the blade, cut a slice at a time where the slot is, at her knees and her belly while she thrashes and at her chest after, with torn ends, bone, soaked slices and the split skin; her three faces standing (ahead, gone to one side, screaming), her scream as the shaking caption draws it, and what is left on the pile: pulp and bone, hair and a slipper, the band from her wrist. The two seconds at the machine as its production renderer draws them: the cone gone and the Shiny Gentleman the child saw in the pines in its place, four and a half times his height, standing in the housing with his coat on its walls and folded down over the south wall, his neck drawn out to the slot and his face split open around her, his fingers on her shoulders, under the red wash. The machine at the plant as its production renderer draws it: the silver cone on the housing with its bent tip, three arms with their balls and two tilted hoops at three moments of turning (one slowing as if the breaker had been pulled), and the toothed mouth over the slot. Staged assets, not a complete ordinary playthrough.\n");if(direct)mc.stop();else mc.setScreen(new HallChangeVisualProof());}
}
