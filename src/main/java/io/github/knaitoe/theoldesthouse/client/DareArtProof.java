package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/**
 * 0.4.173, "Third Floor Girls'": the dare's art in native renders. The kids in their sheets at each phase, the fittings and
 * the glass whole and shattered, the reader's own face at each count as the glass changes it, and the third count as the
 * glass shows it: the reflection's hands empty, the second flame and the woman behind it.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class DareArtProof extends Screen {
    private static final String[] NAMES={"kids","fittings","faces","third-count","borrowed-light"};
    private static final Path FOLDER=Path.of("../build/dare-art");
    private static boolean launched;
    private int page,frames;
    private DareArtProof(){super(Component.literal("Third Floor Girls'"));}
    @SubscribeEvent public static void start(ClientTickEvent.Post ignored){
        var mc=Minecraft.getInstance();if(Boolean.getBoolean("the_oldest_house.dareSmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new DareArtProof());}
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    private static int[][] steve()throws Exception{
        try(var in=Minecraft.getInstance().getResourceManager().open(DefaultPlayerSkin.getDefaultTexture());var image=NativeImage.read(in)){
            int[] base=new int[64],hat=new int[64];for(int y=0;y<8;y++)for(int x=0;x<8;x++){base[y*8+x]=image.getPixelRGBA(8+x,8+y)|0xff000000;hat[y*8+x]=image.getPixelRGBA(40+x,8+y);}return new int[][]{base,hat};
        }
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff1e2124);var out=mc.renderBuffers().bufferSource();var p=g.pose();
        g.drawString(font,"Third Floor Girls' / "+NAMES[page],10,10,0xffe9e2cf,false);
        try{switch(page){
            case 0->{
                com.mojang.blaze3d.platform.Lighting.setupForEntityInInventory();
                var model=new DareRenderers.SheetModel(mc.getEntityModels().bakeLayer(DareRenderers.SHEET));
                for(int i=0;i<6;i++){int role=i<3?LiteraryActor.DARE_KID_A:LiteraryActor.DARE_KID_B,phase=i%3;
                    p.pushPose();p.translate((i%3+.5F)*width/3F,(i/3+.95F)*(height-40)/2F,150);p.scale(54,54,-54);p.mulPose(Axis.YP.rotationDegrees(i/3==0?18:-18));p.translate(0,-1.501,0);
                    model.pose(role,phase,12+i*7,0,0);model.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(DareRenderers.sheet(role))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);out.endBatch();p.popPose();
                    g.drawCenteredString(font,new String[]{"waiting","giggling","silent"}[phase],(int)((i%3+.5F)*width/3F),(int)((i/3+.95F)*(height-40)/2F)+8,0xffe9e2cf);}
                p.pushPose();p.translate(width-40,40,150);p.scale(2,2,1);g.renderItem(new ItemStack(DareRegistry.CANDLE.get()),0,0);p.popPose();
            }
            case 1->{
                var kinds=new ArrayList<BlockState>();
                for(var k:DareFittingBlock.Kind.values())for(int stage=0;stage<DareFittingBlock.stages(k);stage++)kinds.add(DareFittingBlock.of(k,Direction.NORTH,stage));
                for(int part=0;part<8;part++)kinds.add(DareMirrorBlock.of(Direction.NORTH,part%4,part/4));
                for(int part=0;part<8;part++)kinds.add(DareMirrorBlock.of(Direction.NORTH,part%4,part/4).setValue(DareMirrorBlock.SHATTERED,true));
                kinds.add(DareRegistry.BATHROOM_DOOR.get().defaultBlockState());kinds.add(DareRegistry.STAIR_DOOR.get().defaultBlockState());
                for(String name:DareRegistry.MATERIAL_NAMES)kinds.add(DareRegistry.material(name).defaultBlockState());
                int columns=12;float cell=Math.min((width-20)/(float)columns,(height-40)/(float)((kinds.size()+columns-1)/columns));
                for(int i=0;i<kinds.size();i++){p.pushPose();p.translate(10+(i%columns+.5F)*cell,30+(i/columns+.62F)*cell,150);float s=cell*.44F;p.scale(s,-s,s);p.mulPose(Axis.XP.rotationDegrees(20));p.mulPose(Axis.YP.rotationDegrees(200));p.translate(-.5,-.5,-.5);
                    mc.getBlockRenderer().renderSingleBlock(kinds.get(i),p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();}
                out.endBatch();
            }
            case 2->{
                var faces=steve();DareReflection.face(faces[0],faces[1]);var body=new PlayerModel<AbstractClientPlayer>(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER),false);
                int[][] stages={{0,0},{1,200},{2,300},{2,600},{3,100},{3,300}};String[] labels={"before","first: nothing yet","second: an eye drifting","second: the mouth paling","third: both eyes, mouth gone","third: it comes back wrong"};
                for(int i=0;i<stages.length;i++){
                    for(var part:new net.minecraft.client.model.geom.ModelPart[]{body.head,body.hat})part.resetPose();
                    p.pushPose();p.translate((i%3+.5F)*width/3F,(i/3+.7F)*(height-40)/2F+20,150);p.scale(150,150,-150);p.translate(0,.25,0);
                    body.head.render(p,out.getBuffer(RenderType.entityCutoutNoCull(DefaultPlayerSkin.getDefaultTexture())),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
                    body.hat.render(p,out.getBuffer(RenderType.entityTranslucent(DefaultPlayerSkin.getDefaultTexture())),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
                    if(stages[i][0]>0){DareReflection.frame(stages[i][0],stages[i][1]);DareReflection.drawFace(body.head,p,out,LightTexture.FULL_BRIGHT);}
                    out.endBatch();p.popPose();
                    g.drawCenteredString(font,labels[i],(int)((i%3+.5F)*width/3F),(int)((i/3+.7F)*(height-40)/2F)+44,0xffe9e2cf);
                }
            }
            case 4->{
                // 0.4.199, the first puzzle room: every working part in each of its states, then the room's one solution in miniature.
                var kinds=new ArrayList<BlockState>();
                for(var k:BorrowedLightBlock.Kind.values())for(int open=0;open<2;open++)for(int turned=0;turned<(k==BorrowedLightBlock.Kind.MIRROR?2:1);turned++)
                    if(open==0||k!=BorrowedLightBlock.Kind.MIRROR&&k!=BorrowedLightBlock.Kind.LAMP&&k!=BorrowedLightBlock.Kind.SCONCE)
                        kinds.add(BorrowedLightBlock.of(k,Direction.NORTH).setValue(BorrowedLightBlock.OPEN,open==1).setValue(BorrowedLightBlock.TURNED,turned==1));
                for(String name:new String[]{"wall","floor","ceiling"})kinds.add(PuzzleRegistry.material(name).defaultBlockState());
                float cell=Math.min((width-20)/(float)kinds.size(),46);
                for(int i=0;i<kinds.size();i++){p.pushPose();p.translate(10+(i+.5F)*cell,40,150);float s=cell*.4F;p.scale(s,-s,s);p.mulPose(Axis.XP.rotationDegrees(20));p.mulPose(Axis.YP.rotationDegrees(200));p.translate(-.5,-.5,-.5);
                    mc.getBlockRenderer().renderSingleBlock(kinds.get(i),p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();}
                out.endBatch();
                // The lamp on the east, its shutter open; the mirror turned on its track; the beam turning onto the lit sun by the far door.
                p.pushPose();p.translate(width*.5F,height*.68F,150);float s=Math.min(width,height)/14F;p.scale(s,-s,s);p.mulPose(Axis.XP.rotationDegrees(28));p.mulPose(Axis.YP.rotationDegrees(35));
                Object[][] room={{BorrowedLightBlock.of(BorrowedLightBlock.Kind.LAMP,Direction.WEST),3,1,0},{BorrowedLightBlock.of(BorrowedLightBlock.Kind.SHUTTER,Direction.WEST).setValue(BorrowedLightBlock.OPEN,true),2,1,0},
                    {BorrowedLightBlock.of(BorrowedLightBlock.Kind.MIRROR,Direction.NORTH).setValue(BorrowedLightBlock.TURNED,true),-1,0,0},{BorrowedLightBlock.of(BorrowedLightBlock.Kind.PLATE,Direction.SOUTH).setValue(BorrowedLightBlock.OPEN,true),-1,1,-4}};
                for(int x=-3;x<=2;x++){p.pushPose();p.translate(x,-1,0);mc.getBlockRenderer().renderSingleBlock(BorrowedLightBlock.of(BorrowedLightBlock.Kind.TRACK,Direction.EAST).setValue(BorrowedLightBlock.OPEN,x==-1),p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();}
                for(int x=-3;x<=3;x++)for(int y=0;y<=2;y++){p.pushPose();p.translate(x,y,-5);mc.getBlockRenderer().renderSingleBlock(PuzzleRegistry.material("wall").defaultBlockState(),p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();}
                for(var part:room){p.pushPose();p.translate((int)part[1],(int)part[2],(int)part[3]);mc.getBlockRenderer().renderSingleBlock((BlockState)part[0],p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();}
                out.endBatch();
                BorrowedLightRenderer.draw(p,out,List.of(new Vec3(3.5,1.5,.5),new Vec3(-.5,1.5,.5),new Vec3(-.5,1.5,-3.5)),0);out.endBatch();p.popPose();
                g.drawString(font,"Borrowed Light: every part and state, then the beam laid on the sun",10,height-18,0xffe9e2cf,false);
            }
            default->{
                // As the glass shows it: the reflection facing the reader, the woman over its left shoulder (the image's right).
                var body=new PlayerModel<AbstractClientPlayer>(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER),false);body.young=false;
                for(int view=0;view<2;view++){
                    p.pushPose();p.translate(width*(view==0?.3F:.72F),height*.84F,150);float s=view==0?118:96;p.scale(-s,s,-s);p.mulPose(Axis.ZP.rotationDegrees(180));
                    if(view==1)p.mulPose(Axis.YP.rotationDegrees(-55));
                    // The reader's reflection: arms at its sides, no candle.
                    p.pushPose();p.mulPose(Axis.YP.rotationDegrees(180-180));p.scale(-1,-1,1);p.scale(.9375F,.9375F,.9375F);p.translate(0,-1.501,0);
                    body.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(DefaultPlayerSkin.getDefaultTexture())),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
                    if(view==0){var faces=steve();DareReflection.face(faces[0],faces[1]);DareReflection.frame(3,40);DareReflection.drawFace(body.head,p,out,LightTexture.FULL_BRIGHT);}
                    p.popPose();
                    DareReflection.draw(p,out,Vec3.ZERO,1.62F,180,new Vec3(0,1.62,-3),1,30,LightTexture.FULL_BRIGHT);
                    out.endBatch();p.popPose();
                }
                g.drawString(font,"Third count, as the glass shows it / front, and turned to show her at its back",10,height-18,0xffe9e2cf,false);
            }
        }}catch(Exception e){throw new IllegalStateException(e);}
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post ignored)throws Exception{
        var mc=Minecraft.getInstance();if(!(mc.screen instanceof DareArtProof proof)||++proof.frames<20)return;Files.createDirectories(FOLDER);
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
            image.writeToFile(FOLDER.resolve("native-dare-"+NAMES[proof.page]+".png"));
            var colors=new HashSet<Integer>();for(int y=image.getHeight()/6;y<image.getHeight()*5/6;y+=3)for(int x=image.getWidth()/6;x<image.getWidth()*5/6;x+=3)colors.add(image.getPixelRGBA(x,y));
            if(colors.size()<12)throw new IllegalStateException("Blank or untextured dare view "+NAMES[proof.page]+": "+colors.size());
        }
        if(++proof.page<NAMES.length){proof.frames=0;return;}
        Files.writeString(FOLDER.resolve("passed.txt"),"Four native views of the third floor: the sheeted kids waiting, giggling and silent; every fitting state, the glass whole and shattered, both doors and the seven materials; the reader's face at each count as the glass changes it; and the third count as the glass shows it. And one of the first puzzle room, Borrowed Light: every working part in each state, its three materials, and the beam turned by the mirror onto the lit sun.\n");
        mc.stop();
    }
}
