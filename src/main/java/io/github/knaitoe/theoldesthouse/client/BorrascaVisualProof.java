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
    @Override public void render(GuiGraphics g,int x,int y,float partial){var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff252925);g.drawString(font,"BORRASCA / "+(page<4?"five people and the plant's coveralls / "+new String[]{"front","side","back","dim"}[page]:page<8?"native materials / sheet "+(page-3)+" of 4":page==8?"Friend and Sheriff / native gestures":page==9?"original keepsakes / five bark states":page==10?"shared cast style / production meshes":page==11?"the Shiny Gentleman / in the pines at dusk":page==12?"a child's bike / parked, and ridden: upright, leaning each way":page==14?"the last night at the machine / on the belt, and what is left":page==15?"the last night at the machine / two seconds, the machine is him":page==16?"the machine at the plant / the cone, its arms and hoops turning":page==27?"the Sheriff's fight / his eight poses, each from two sides; and his baton":page==26?"the four households / what tells them apart, in the childhood year (above) and years on (below)":page>=23?"the corkboard / "+(page==23?"as it hangs on the first visit and the second, and a card of each in the hand":page==24?"years on; lifted; taken down; an old card; the envelope turned over":"the rest of its pieces in the hand"):page==22?"the pumps, the pews and the benches / each as it stands, turned to show its sides; the price board in both its years":page==21?"the store's front / its sheets and boards as they hang, and papered over":page>=19?"the town's painted signs / their blocks as they are hung / sheet "+(page-18)+" of 2":page==18?"the names on the Triple Tree / the burl as built; beside the carving, visit by visit":page==17?"the comic in the window / its eight pages bright and bleached, in the sill and in the hand":"the drugstore cabinets / DEEP SEAM and BIG CAT, mid-game"),10,10,0xffe8ddc9,false);g.flush();var buffers=mc.renderBuffers().bufferSource();int light=page==3?LightTexture.pack(7,0):LightTexture.FULL_BRIGHT;
        if(page==10){styleComparison(g);return;}if(page==11){gentleman(g);return;}if(page==12){bike(g);return;}if(page==13){arcade(g);return;}if(page==14){feeding(g);return;}if(page==15){devoured(g);return;}if(page==16){orrery(g);return;}if(page==17){comic(g);return;}if(page==18){names(g);return;}if(page==27){fight(g);return;}if(page==26){homes(g);return;}if(page>=23){cards(g,page-23);return;}if(page==22){seats(g);return;}if(page==21){papers(g);return;}if(page>=19){signs(g,page-19);return;}if(page==8){gestures(g,light);return;}if(page==9){keepsakes(g,light);return;}
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
    /**
     * 0.4.445: Kit &amp; Caboose No. 31. Its eight pages as the production screen draws them, bright and then as the window bleached
     * them; the comic in the fort's sill at both stages, on the ledge inside a pane on a plank wall, from the room and from the
     * bough outside; and the item, bright and bleached.
     */
    /** 0.4.448: the burl's south face as it is built, names all over it, and the block beside the carving as it stands on each visit. */
    private void names(GuiGraphics g){var mc=Minecraft.getInstance();g.fill(0,24,width,height,0xff2b2f2a);g.flush();var buffers=mc.renderBuffers().bufferSource();
        for(int k=0;k<=BorrascaNamesBlock.LONG_GONE;k++)if(mc.getBlockRenderer().getBlockModel(BorrascaRegistry.NAMES.get().defaultBlockState().setValue(BorrascaNamesBlock.FACE,k))==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing model for the Tree's names: face "+k);
        var pose=g.pose();float s=height*.15F;pose.pushPose();pose.translate(width*.36F,height*.72F,200);pose.scale(s,-s,s);pose.mulPose(Axis.XP.rotationDegrees(7));pose.mulPose(Axis.YP.rotationDegrees(-10));
        for(int x=-3;x<=2;x++)for(int y=0;y<=2;y++){var state=x==0&&y==0?BorrascaRegistry.CARVING.get().defaultBlockState():x==2&&y==0?BorrascaNamesBlock.friends(1):BorrascaNamesBlock.old(x,y,-98);
            pose.pushPose();pose.translate(x,y,0);mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();}
        buffers.endBatch();pose.popPose();
        for(int visit=1;visit<=4;visit++){float t=height*.14F;pose.pushPose();pose.translate(width*.72F+((visit-1)%2)*t*1.7F,height*.42F+((visit-1)/2)*t*2.1F,200);pose.scale(t,-t,t);pose.mulPose(Axis.XP.rotationDegrees(7));pose.mulPose(Axis.YP.rotationDegrees(-10));
            mc.getBlockRenderer().renderSingleBlock(BorrascaNamesBlock.friends(visit),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();
            g.drawString(font,"visit "+visit,(int)(width*.72F+((visit-1)%2)*t*1.7F),(int)(height*.42F+((visit-1)/2)*t*2.1F)+8,0xffd8d2c0,false);}
    }
    /**
     * 0.4.449: the town's painted signs, each hung as the builders hang it (BorrascaSigns.cell), looking out of the screen. A sign whose
     * blocks were in the wrong order, or whose lettering ran backwards, would show here.
     */
    private void signs(GuiGraphics g,int sheet){var mc=Minecraft.getInstance();g.fill(0,24,width,height,0xff5d646b);g.flush();var buffers=mc.renderBuffers().bufferSource();
        for(int piece=0;piece<=BorrascaSignBlock.PIECE.getPossibleValues().size()-1;piece++)if(mc.getBlockRenderer().getBlockModel(BorrascaRegistry.SIGN.get().defaultBlockState().setValue(BorrascaSignBlock.PIECE,piece))==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing model for a painted sign: block "+piece);
        var S=BorrascaSigns.Sign.values();var rows=new java.util.ArrayList<java.util.List<BorrascaSigns.Sign>>();
        if(sheet==0){rows.add(java.util.List.of(BorrascaSigns.Sign.DINER,BorrascaSigns.Sign.ANTIQUES,BorrascaSigns.Sign.STATION));rows.add(java.util.List.of(BorrascaSigns.Sign.DRUG,BorrascaSigns.Sign.DOCTOR,BorrascaSigns.Sign.TIPPLE));
            rows.add(java.util.List.of(BorrascaSigns.Sign.STORE,BorrascaSigns.Sign.COLD,BorrascaSigns.Sign.SHERIFF));rows.add(java.util.List.of(BorrascaSigns.Sign.SOCIETY,BorrascaSigns.Sign.HARDWARE,BorrascaSigns.Sign.LOST,BorrascaSigns.Sign.TRAIL));}
        else{rows.add(java.util.List.of(BorrascaSigns.Sign.ARCH,BorrascaSigns.Sign.FORT,BorrascaSigns.Sign.DEPOT,BorrascaSigns.Sign.NO_ENTRY));rows.add(java.util.List.of(BorrascaSigns.Sign.CHURCH,BorrascaSigns.Sign.SERVICE,BorrascaSigns.Sign.COUNTY,BorrascaSigns.Sign.PRESCOTT,BorrascaSigns.Sign.ROUNDEL));
            rows.add(java.util.List.of(BorrascaSigns.Sign.ARCH_MILE,BorrascaSigns.Sign.REFINING,BorrascaSigns.Sign.NAME_PRUITT,BorrascaSigns.Sign.NAME_HOLLIS,BorrascaSigns.Sign.NAME_WEBB,BorrascaSigns.Sign.NAME_CORLEY));
            rows.add(java.util.List.of(BorrascaSigns.Sign.OPEN_DINER,BorrascaSigns.Sign.OPEN_DRUG,BorrascaSigns.Sign.OPEN_ANTIQUES,BorrascaSigns.Sign.IN_DOCTOR));
            for(int from=1;from<=7;from+=6){var plates=new java.util.ArrayList<BorrascaSigns.Sign>();for(int room=from;room<from+6;room++)plates.add(BorrascaSigns.Sign.room(room));rows.add(plates);}}
        float tall=0;for(var row:rows){int h=1;for(var s:row)h=Math.max(h,s.rows);tall+=h+.6F;}
        float wide=0;for(var row:rows){float w=0;for(var s:row)w+=span(s)+.6F;wide=Math.max(wide,w);}
        float s=Math.min((width-20)/wide,(height-44)/tall);var pose=g.pose();pose.pushPose();pose.translate(10,34,200);pose.scale(s,-s,s);
        float y=0;for(var row:rows){int h=1;for(var sign:row)h=Math.max(h,sign.rows);y-=h;float x=0;
            for(var sign:row){float pad=(span(sign)-sign.cols)/2F;
                for(int r=0;r<sign.rows;r++)for(int c=0;c<sign.cols;c++){var at=BorrascaSigns.cell(sign,net.minecraft.core.BlockPos.ZERO,Direction.SOUTH,c,r);
                    pose.pushPose();pose.translate(x+pad+at.getX(),y+at.getY(),0);mc.getBlockRenderer().renderSingleBlock(BorrascaSignBlock.of(sign,r*sign.cols+c,Direction.SOUTH),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();}
                x+=span(sign)+.6F;}
            y-=.6F;}
        buffers.endBatch();pose.popPose();
    }
    /**
     * 0.4.451: the papers on the front of Drisking General, each block looking out of the screen as a reader on the porch sees it. Top:
     * the sheets that hang by the door, new and gone yellow, and the noticeboard at its four stages. Below: the front papered over,
     * each square dealt by BorrascaPapers.wall, the sheets on its windows in their glass models; then two of those from behind the glass.
     */
    private void papers(GuiGraphics g){var mc=Minecraft.getInstance();g.fill(0,24,width,height,0xffb8a070);g.flush();var buffers=mc.renderBuffers().bufferSource();
        for(int piece=0;piece<BorrascaPapers.Sheet.TOTAL;piece++)for(boolean glass:new boolean[]{false,true})if(mc.getBlockRenderer().getBlockModel(BorrascaRegistry.PAPER.get().defaultBlockState().setValue(BorrascaPaperBlock.PIECE,piece).setValue(BorrascaPaperBlock.GLASS,glass))==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing model for a block of the store's papers: "+piece+(glass?" on glass":""));
        float s=Math.min((width-20)/15.6F,(height-44)/7.4F);var pose=g.pose();pose.pushPose();pose.translate(10,34,200);pose.scale(s,-s,s);
        java.util.function.BiConsumer<net.minecraft.world.level.block.state.BlockState,float[]> put=(state,at)->{pose.pushPose();pose.translate(at[0],at[1],0);mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();};
        float x=0;
        for(var single:new BorrascaPapers.Sheet[]{BorrascaPapers.Sheet.PATRICK,BorrascaPapers.Sheet.KENDRA,BorrascaPapers.Sheet.PATRICK_YELLOWED,BorrascaPapers.Sheet.KENDRA_YELLOWED}){put.accept(BorrascaPaperBlock.of(single,0,Direction.SOUTH,false),new float[]{x,-1.5F});x+=1.2F;}
        x+=.4F;
        for(int stage:new int[]{BorrascaPapers.AS_BUILT,BorrascaPapers.KENDRA,BorrascaPapers.LAST,BorrascaPapers.PAPERED}){var board=BorrascaPapers.board(stage);for(int k=0;k<4;k++)put.accept(BorrascaPaperBlock.of(board,k,Direction.SOUTH,false),new float[]{x+k%2,-1-k/2});x+=2.4F;}
        var cells=java.util.Arrays.asList(BorrascaRooms.BOARD);
        for(int z=BorrascaPapers.SOUTH;z>=BorrascaPapers.NORTH;z--)for(int y=BorrascaPapers.FOOT;y<=BorrascaPapers.HEAD;y++){
            var at=new float[]{BorrascaPapers.SOUTH-z,-3.6F-(BorrascaPapers.HEAD-y)};var cell=new net.minecraft.core.BlockPos(BorrascaPapers.FRONT_X,y,z);
            if(cells.contains(cell)){put.accept(BorrascaPapers.boardPiece(BorrascaPapers.Sheet.BOARD_COVERED,cell).setValue(BorrascaPaperBlock.FACING,Direction.SOUTH),at);continue;}
            // The doorway, the store's tin sign and its bench are left as they are.
            if(z==BorrascaPapers.DOOR_Z&&y<=BorrascaPapers.FOOT+1||y==2&&(z==-25||z==-24)||y==0&&(z==-27||z==-26))continue;
            boolean window=(y==1||y==2)&&(z<=-26&&z>=-28||z==-19||z==-18);
            put.accept(BorrascaPaperBlock.of(BorrascaPapers.wall(y,z),0,Direction.SOUTH,window),at);
        }
        put.accept(BorrascaPaperBlock.of(BorrascaPapers.Sheet.WALL_KELLY,0,Direction.NORTH,true),new float[]{13.4F,-4.6F});put.accept(BorrascaPaperBlock.of(BorrascaPapers.Sheet.WALL_KAYLA,0,Direction.NORTH,true),new float[]{14.6F,-4.6F});
        buffers.endBatch();pose.popPose();
    }
    /**
     * 0.4.454 (owner: "Borrasca needs a custom texture pass"; of the pews, "again, custom textures would be beneficial here."): the
     * pumps, the price board, a pew and the benches as the game draws them, each group turned on its own middle.
     */
    /**
     * 0.4.455: the doctor's corkboard as its production screen draws it (BorrascaCardsScreen), at the size it is drawn. The first
     * sheet is the board as it hangs on the first visit and on the second, with a card of each in the hand beside it. The second
     * is the board years on, and the first again with a card under the pointer and another taken down, with an old card and the
     * envelope's open mouth beside them. The third is the rest of its pieces in the hand. Every piece of every visit's board is
     * first asked whether everything written on it fits its card in the production fonts.
     */
    private void cards(GuiGraphics g,int sheet){
        var pose=g.pose();
        for(int visit=1;visit<=3;visit++)for(var piece:BorrascaCards.board(visit)){String problem=BorrascaCardsScreen.problem(font,piece);if(problem!=null)throw new IllegalStateException("The doctor's corkboard, visit "+visit+": "+problem);}
        var first=BorrascaCards.board(1);var second=BorrascaCards.board(2);var grown=BorrascaCards.board(3);
        if(sheet<2){
            for(int i=0;i<2;i++){
                var pieces=sheet==0?(i==0?first:second):i==0?grown:first;boolean handled=sheet==1&&i==1;
                pose.pushPose();pose.translate(8,26+i*188,0);BorrascaCardsScreen.board(g,font,pieces,handled?2:-1,handled?4:-1);pose.popPose();
                g.drawString(font,sheet==0?(i==0?"the first visit":"the second visit: the newest card over the envelope"):i==0?"years on: a later year's cards over the old":"a card under the pointer; another taken down",10,26+i*188+BorrascaCardsScreen.BH+3,0xff9a927f,false);
                var piece=sheet==0?(i==0?first.get(0):second.get(6)):i==0?grown.get(2):second.get(5);
                pose.pushPose();pose.translate(382,56+i*188,0);BorrascaCardsScreen.large(g,font,piece,sheet==1&&i==1);pose.popPose();
            }
            return;
        }
        Object[] hands={first.get(1),first.get(3),first.get(4),grown.get(5),grown.get(6),grown.get(9),second.get(5),grown.get(10),grown.get(3)};
        for(int i=0;i<hands.length;i++){pose.pushPose();pose.translate(16+i%3*196,34+i/3*122,0);BorrascaCardsScreen.large(g,font,(BorrascaCards.Piece)hands[i],false);pose.popPose();}
    }
    /**
     * 0.4.456: what tells the street's four households apart (BorrascaHomeBlock), each thing in the childhood year (the upper row) and
     * years on (the lower): what stands on the floor, what hangs, and what lies on a table, a desk or the drawers.
     */
    private void homes(GuiGraphics g){var mc=Minecraft.getInstance();g.fill(0,24,width,height,0xff7c8a8e);g.flush();var buffers=mc.renderBuffers().bufferSource();
        var kinds=BorrascaHomeBlock.Kind.values();
        for(var kind:kinds)for(int stage=0;stage<2;stage++)for(var facing:Direction.Plane.HORIZONTAL)if(mc.getBlockRenderer().getBlockModel(BorrascaHomeBlock.of(kind,facing,stage))==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing household thing "+kind+" "+stage+" "+facing);
        float s=Math.min(width/(kinds.length+1.2F),(height-50)/5.2F);
        var furniture=io.github.knaitoe.theoldesthouse.house.HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.FACING,Direction.SOUTH);
        for(int year=0;year<2;year++){
            float foot=year==0?34+2.45F*s:height-10;
            for(int i=0;i<kinds.length;i++){
                var kind=kinds[i];float x=(1.1F+i)*s;var thing=BorrascaHomeBlock.of(kind,Direction.SOUTH,year);
                if(kind.lies()){
                    var under=furniture.setValue(io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.KIND,kind==BorrascaHomeBlock.Kind.BIBLE?io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.Kind.WALNUT_DESK
                        :kind==BorrascaHomeBlock.Kind.ATLAS?io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.Kind.FORMICA_TABLE:io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS);
                    stand(g,buffers,x,foot,s*.8F,i%2==0?-24:24,.5F,under,new float[]{0,0,0},thing,new float[]{0,1,0});
                }else stand(g,buffers,x,foot-(kind.wall()?.5F*s:0),s*.8F,kind.wall()?(i%2==0?-14:14):(i%2==0?-30:30),.5F,thing,new float[]{0,0,0});
            }
        }
        buffers.endBatch();
    }
    /**
     * 0.4.457: the Sheriff's fight (BorrascaSheriffFight), each of his eight poses through the production cast model on his own skin,
     * a quarter turned so the shoulders show; and his baton, as it is held and as it lies.
     */
    private void fight(GuiGraphics g){var mc=Minecraft.getInstance();g.fill(0,24,width,height,0xff3a3f38);g.flush();var buffers=mc.renderBuffers().bufferSource();
        int[][] rows={{BorrascaSheriffFight.GUARD,BorrascaSheriffFight.BATON_UP,BorrascaSheriffFight.BATON_DOWN,BorrascaSheriffFight.REEL},{BorrascaSheriffFight.LEFT_BACK,BorrascaSheriffFight.LEFT_OUT,BorrascaSheriffFight.RIGHT_BACK,BorrascaSheriffFight.RIGHT_OUT}};
        String[][] names={{"hands up","the baton raised","brought down","reeling"},{"the left drawn back","the left thrown","the right drawn back","the right thrown"}};
        if(mc.getBlockRenderer().getBlockModel(BorrascaShopBlock.of(BorrascaShopBlock.Kind.BATON,Direction.NORTH))==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing the Sheriff's baton where it lies");
        if(mc.getItemRenderer().getModel(new net.minecraft.world.item.ItemStack(BorrascaRegistry.BATON.get()),null,null,0)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing the Sheriff's baton in the hand");
        float cell=(width-110)/4F,scale=Math.min(cell*.3F,(height-70)/2*.36F);
        for(int row=0;row<2;row++)for(int col=0;col<4;col++){
            g.drawString(font,names[row][col],(int)((col+.12F)*cell),38+row*(height-40)/2,0xffc8c5af,false);g.flush();
            for(int side=0;side<2;side++){
                var pose=g.pose();pose.pushPose();pose.translate((col+.3F+side*.42F)*cell,62+row*(height-40)/2+scale*.5F,200);pose.scale(scale,scale,scale);pose.mulPose(Axis.YP.rotationDegrees(side==0?160:235));
                cast.previewPose(2,rows[row][col],0);cast.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(BorrascaNativeArt.skin(2,false))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);buffers.endBatch();
                // While he has it, the baton in his right hand, by the held-item layer's own steps.
                if(row==0&&col<3){
                    pose.pushPose();cast.translateToHand(net.minecraft.world.entity.HumanoidArm.RIGHT,pose);pose.mulPose(Axis.XP.rotationDegrees(-90));pose.mulPose(Axis.YP.rotationDegrees(180));pose.translate(1/16F,.125F,-.625F);
                    mc.getItemRenderer().renderStatic(new net.minecraft.world.item.ItemStack(BorrascaRegistry.BATON.get()),net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,pose,buffers,null,0);
                    buffers.endBatch();pose.popPose();
                }
                pose.popPose();
            }
        }
        g.drawString(font,"his baton",width-96,38,0xffc8c5af,false);g.flush();
        var pose=g.pose();pose.pushPose();pose.translate(width-86,56,0);pose.scale(4,4,1);g.renderItem(new net.minecraft.world.item.ItemStack(BorrascaRegistry.BATON.get()),0,0);pose.popPose();g.flush();
        stand(g,buffers,width-54,height-30,64,-30,.5F,BorrascaShopBlock.of(BorrascaShopBlock.Kind.BATON,Direction.SOUTH),new float[]{0,0,0});
        buffers.endBatch();
    }
    private void seats(GuiGraphics g){var mc=Minecraft.getInstance();g.fill(0,24,width,height,0xff7c8a8e);g.flush();var buffers=mc.renderBuffers().bufferSource();
        var kinds=new BorrascaShopBlock.Kind[]{BorrascaShopBlock.Kind.PUMP,BorrascaShopBlock.Kind.PUMP_TOP,BorrascaShopBlock.Kind.PRICE,BorrascaShopBlock.Kind.PEW_END,BorrascaShopBlock.Kind.PEW_MID,BorrascaShopBlock.Kind.BENCH,BorrascaShopBlock.Kind.BENCH_END,BorrascaShopBlock.Kind.BENCH_MID};
        for(var kind:kinds)for(int stage=0;stage<2;stage++)for(var facing:Direction.Plane.HORIZONTAL)if(mc.getBlockRenderer().getBlockModel(BorrascaShopBlock.of(kind,facing,stage))==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing Drisking fitting "+kind+" "+stage+" "+facing);
        float s=Math.min(width/15.5F,(height-50)/5.6F),top=34+2.9F*s,foot=height-12;
        // The pumps: the first from the side its hose hangs on and from the other, then the second; and the board in both its years.
        for(int i=0;i<3;i++){int which=i/2;stand(g,buffers,(1.4F+i*2.4F)*s,top,s,i==1?-38:38,.5F,BorrascaShopBlock.of(BorrascaShopBlock.Kind.PUMP,Direction.SOUTH,which),new float[]{0,0,0},BorrascaShopBlock.of(BorrascaShopBlock.Kind.PUMP_TOP,Direction.SOUTH,which),new float[]{0,1,0});}
        for(int year=0;year<2;year++)stand(g,buffers,(9.2F+year*1.5F)*s,top-.9F*s,s,0,.5F,BorrascaShopBlock.of(BorrascaShopBlock.Kind.PRICE,Direction.SOUTH,year),new float[]{0,0,0});
        // A pew three blocks long from the front, and one from behind; a bench alone, and one three blocks long.
        for(int back=0;back<2;back++){var looks=back==0?Direction.SOUTH:Direction.NORTH;int first=back==0?2:0,last=2-first;
            stand(g,buffers,(2.4F+back*4.4F)*s,foot,s,back==0?-28:28,1.5F,BorrascaShopBlock.of(BorrascaShopBlock.Kind.PEW_END,looks,0),new float[]{first,0,0},BorrascaShopBlock.of(BorrascaShopBlock.Kind.PEW_MID,looks,0),new float[]{1,0,0},BorrascaShopBlock.of(BorrascaShopBlock.Kind.PEW_END,looks,1),new float[]{last,0,0});}
        stand(g,buffers,10.2F*s,foot,s,-32,.5F,BorrascaShopBlock.of(BorrascaShopBlock.Kind.BENCH,Direction.SOUTH,0),new float[]{0,0,0});
        stand(g,buffers,13.2F*s,foot,s,-28,1.5F,BorrascaShopBlock.of(BorrascaShopBlock.Kind.BENCH_END,Direction.SOUTH,0),new float[]{2,0,0},BorrascaShopBlock.of(BorrascaShopBlock.Kind.BENCH_MID,Direction.SOUTH,0),new float[]{1,0,0},BorrascaShopBlock.of(BorrascaShopBlock.Kind.BENCH_END,Direction.SOUTH,1),new float[]{0,0,0});
        buffers.endBatch();
    }
    /** A group of blocks stood on the page, turned about its own middle (half its width along x) and tipped a little toward the eye: pairs of a state and its place. */
    private static void stand(GuiGraphics g,MultiBufferSource.BufferSource buffers,float x,float foot,float s,float turn,float half,Object... blocks){
        var mc=Minecraft.getInstance();var pose=g.pose();pose.pushPose();pose.translate(x,foot,300);pose.scale(s,-s,s);pose.mulPose(Axis.XP.rotationDegrees(16));pose.mulPose(Axis.YP.rotationDegrees(turn));pose.translate(-half,0,-.5F);
        for(int i=0;i<blocks.length;i+=2){var at=(float[])blocks[i+1];pose.pushPose();pose.translate(at[0],at[1],at[2]);mc.getBlockRenderer().renderSingleBlock((net.minecraft.world.level.block.state.BlockState)blocks[i],pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();}
        pose.popPose();
    }
    /** How many blocks wide a sign shows: a single one's board may reach a block past it each side. */
    private static float span(BorrascaSigns.Sign s){return s.cols>1||s==BorrascaSigns.Sign.ROUNDEL?s.cols:s==BorrascaSigns.Sign.ARCH_MILE||s==BorrascaSigns.Sign.REFINING?3:2;}
    private void comic(GuiGraphics g){var mc=Minecraft.getInstance();g.fill(0,24,width,height,0xff2f3338);g.flush();var buffers=mc.renderBuffers().bufferSource();
        for(var texture:new net.minecraft.resources.ResourceLocation[]{BorrascaComicScreen.FRESH,BorrascaComicScreen.AGED})if(mc.getResourceManager().getResource(texture).isEmpty())throw new IllegalStateException("Missing comic pages: "+texture);
        for(int row=0;row<2;row++)for(int i=0;i<BorrascaComic.PAGES;i++)BorrascaComicScreen.draw(g,row==1,i,(width-8*66)/2+i*66,30+row*102,.5F);
        g.flush();
        var wall=net.minecraft.world.level.block.Blocks.SPRUCE_PLANKS.defaultBlockState();
        var pane=net.minecraft.world.level.block.Blocks.GLASS_PANE.defaultBlockState().setValue(net.minecraft.world.level.block.IronBarsBlock.NORTH,true).setValue(net.minecraft.world.level.block.IronBarsBlock.SOUTH,true);
        for(int stage=0;stage<2;stage++)for(var facing:Direction.Plane.HORIZONTAL)if(mc.getBlockRenderer().getBlockModel(BorrascaShopBlock.of(BorrascaShopBlock.Kind.COMIC,facing,stage))==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing sill comic model: stage "+stage+" facing "+facing);
        for(int n=0;n<4;n++){var pose=g.pose();float s=height*.15F;pose.pushPose();pose.translate(width*(.13F+.19F*n),height-22,200);pose.scale(s,-s,s);pose.mulPose(Axis.XP.rotationDegrees(16));pose.mulPose(Axis.YP.rotationDegrees(n%2==0?-58:122));pose.translate(0,0,-.5F);
            pose.pushPose();pose.translate(-1,0,0);mc.getBlockRenderer().renderSingleBlock(wall,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.translate(0,1,0);mc.getBlockRenderer().renderSingleBlock(pane,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();
            pose.pushPose();pose.translate(0,1,0);mc.getBlockRenderer().renderSingleBlock(BorrascaShopBlock.of(BorrascaShopBlock.Kind.COMIC,Direction.EAST,n/2),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();
            buffers.endBatch();pose.popPose();}
        var bright=new net.minecraft.world.item.ItemStack(BorrascaRegistry.COMIC.get());var bleached=bright.copy();bleached.set(net.minecraft.core.component.DataComponents.CUSTOM_MODEL_DATA,new net.minecraft.world.item.component.CustomModelData(1));
        for(int i=0;i<2;i++){var pose=g.pose();pose.pushPose();pose.translate(width-128+i*60,height-84,0);pose.scale(3,3,1);g.renderItem(i==0?bright:bleached,0,0);pose.popPose();}
        g.flush();
    }
    private void orrery(GuiGraphics g){g.fillGradient(0,24,width,height,0xff2b3140,0xff15130f);g.flush();var buffers=Minecraft.getInstance().renderBuffers().bufferSource();
        for(int i=0;i<3;i++){var pose=g.pose();float s=(height-50)/20F*(i==0?1:.5F);pose.pushPose();
            pose.translate(i==0?width*.32F:width*.76F,i==1?height*.52F:height-14,400);pose.scale(s,-s,s);pose.mulPose(Axis.XP.rotationDegrees(10));pose.mulPose(Axis.YP.rotationDegrees(-150+50*i));pose.translate(-.5F,0,-.5F);
            plant(pose,buffers,frames*.12F+i);BorrascaOrrery.draw(pose,buffers,LightTexture.FULL_BRIGHT,frames*.12+i*9,i==2?.2F:1,frames+i*40);BorrascaOrrery.mouth(pose,buffers,LightTexture.FULL_BRIGHT);buffers.endBatch();pose.popPose();}}
    /** 0.4.436: the two cabinets, each half by its block model, and a frame of each game played a while with a steady hand. */
    private void arcade(GuiGraphics g){var mc=Minecraft.getInstance();g.fill(0,24,width,height,0xff3a3430);g.flush();var buffers=mc.renderBuffers().bufferSource();
        for(int game=0;game<2;game++){var pose=g.pose();float s=height*.17F;pose.pushPose();pose.translate(width*(.12F+.2F*game),height-34,200);pose.scale(s,-s,s);pose.mulPose(Axis.XP.rotationDegrees(12));pose.mulPose(Axis.YP.rotationDegrees(200));pose.translate(-.5F,0,-.5F);
            mc.getBlockRenderer().renderSingleBlock(BorrascaShopBlock.of(BorrascaShopBlock.Kind.CABINET,Direction.NORTH,game),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.translate(0,1,0);
            mc.getBlockRenderer().renderSingleBlock(BorrascaShopBlock.of(BorrascaShopBlock.Kind.CABINET_TOP,Direction.NORTH,game),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();}
        for(int game=0;game<2;game++){var play=BorrascaArcade.of(game,434L+game,null);var hand=new BorrascaArcade.Hand(434L+game,game==0?BorrascaArcade.ROUND:BorrascaArcade.FISHES);for(int t=0;t<600&&!play.over();t++)play.tick(hand.keys(play));
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
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{var mc=Minecraft.getInstance();boolean direct=Boolean.getBoolean("the_oldest_house.borrascaSmoke");if(!(direct||Boolean.getBoolean("the_oldest_house.fontSmoke"))||!(mc.screen instanceof BorrascaVisualProof s)||++s.frames<24)return;var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);String name=s.page<4?new String[]{"front","side","back","dim"}[s.page]:s.page<8?"fixtures-"+(s.page-4):s.page==8?"gestures":s.page==9?"keepsakes":s.page==10?"style":s.page==11?"gentleman":s.page==12?"bike":s.page==13?"arcade":s.page==14?"feeding":s.page==15?"devoured":s.page==16?"orrery":s.page==17?"comic":s.page==18?"names":s.page==27?"fight":s.page==26?"homes":s.page>=23?"cards-"+(s.page-23):s.page==22?"seats":s.page==21?"papers":"signs-"+(s.page-19);try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-borrasca-"+name+".png"));if(s.page==9)namesVisible(image);}if(++s.page<28){s.frames=0;return;}Files.writeString(dir.resolve("borrasca-assets-passed.txt"),"Eighteen native GPU views: the four people, the friend grown, and the two plant workers on their own skins at three connected gait samples, front/side/back/dim; every fitted material in four facings on four bounded sheets; original machine roller at four continuous angles; all used Friend/Sheriff gestures, the friend led by the arm among them; the three item models and five bark/name states through the production native font renderer, with visible lettering differences required on the actual frame. Direct comparison with six established cast members on their production layers. The Shiny Gentleman through the production renderer, near and far, against a dusk sky. A child's bike parked, and ridden through the production renderer: upright, and leaning each way with its wheels and cranks turned. The drugstore's two arcade cabinets, and a frame of each of their games part way through, drawn by the production screen: DEEP SEAM as Patrick plays it once told to go round the timbers, BIG CAT as Kendra fishes it, each by the hand a watching reader's screen plays it with. The woman on the belt as the production cast model lays her: whole, her arms over its sides; then at the blade, cut a slice at a time where the slot is, at her knees and her belly while she thrashes and at her chest after, with torn ends, bone, soaked slices and the split skin; her three faces standing (ahead, gone to one side, screaming), her scream as the shaking caption draws it, and what is left on the pile: pulp and bone, hair and a slipper, the band from her wrist. The two seconds at the machine as its production renderer draws them: the cone gone and the Shiny Gentleman the child saw in the pines in its place, four and a half times his height, standing in the housing with his coat on its walls and folded down over the south wall, his neck drawn out to the slot and his face split open around her, his fingers on her shoulders, under the red wash. The machine at the plant as its production renderer draws it: the silver cone on the housing with its bent tip, three arms with their balls and two tilted hoops at three moments of turning (one slowing as if the breaker had been pulled), and the toothed mouth over the slot. Kit & Caboose No. 31: its eight pages through the production screen's own drawing, bright and as the window bleached them; the comic in the fort's sill at both stages, on the ledge inside a pane on a plank wall, from the room and from outside; and the item, bright and bleached. The town's painted signs on two sheets, every one hung by the builders' own rule from the blocks the game draws: the six shops' boards, the store's, the Sheriff's, the society's, the church's, the trailhead's and the arch's, the fort's, the depot's, NO ENTRY, the trucks' lettering, the families' plates, the twelve room plates and the cards in the windows. The papers on the store's front, each block as the game draws it: the sheets that hang by its door on Patrick's and Kendra's own faces, new and gone yellow; the noticeboard at each of its four stages, one board two blocks by two; the front papered over after the ending, each square dealt by the game's own rule, the sheets on its windows in their glass models, the doorway and the places of the tin sign and the bench left open; and two of the window sheets from behind the glass. The station's two pumps, each built of its parts and each with its own wheels behind the glass, from the side the hose hangs on and from the other; the price board in the childhood years and years on; a pew from the front and from behind, its ends shaped and the hymnals in the rack on its back; a bench alone and one three blocks long, their slats on cast iron. The doctor's corkboard as its production screen draws it: the board of the first visit, of the second with the newest card pinned over the envelope, and of a reader come back grown with the later year's cards over the yellowed ones; a card lifted under the pointer and the hole a taken pin leaves; and its pieces in the hand, every line on every piece of every visit's board measured against its card through the production fonts: cards in five hands, the twins' two lines, an old card, the Tates', and the envelope from the front, turned over and gone yellow. What tells the street's four households apart, each thing through its production model in the childhood year and years on: a crib, a framed school picture, a cross, a family Bible on a desk, a television in its cabinet, a work jacket on its hooks, a suitcase, a road atlas on a table, and a picture frame on its face on the drawers. The Sheriff's fight through the production cast model: his hands up, the baton raised over his shoulder and brought down, reeling from a blow, and each fist drawn back and thrown; and his baton as a thing in the hand and lying where it fell. Staged assets, not a complete ordinary playthrough.\n");if(direct)mc.stop();else mc.setScreen(new HallChangeVisualProof());}
}
