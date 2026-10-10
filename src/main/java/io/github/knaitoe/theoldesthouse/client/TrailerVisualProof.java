package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DoorBlock;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Four native GPU views of all cousins, complete strides, connected fear/laughter and the actual fixtures. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class TrailerVisualProof extends Screen {
    private static boolean started;
    private int page,frames;
    public TrailerVisualProof(){super(Component.literal("Trailer update"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    private static ResourceLocation skin(int i){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/trailer_child_"+i+".png");}
    @SubscribeEvent public static void start(ClientTickEvent.Post e){
        if(!Boolean.getBoolean("the_oldest_house.trailerSmoke")||started)return;var mc=Minecraft.getInstance();if(mc.getOverlay()!=null||mc.screen==null)return;
        started=true;mc.options.guiScale().set(2);mc.resizeDisplay();TheOldestHouse.LOGGER.info("TRAILER PROOF: opening native models from {}",mc.screen.getClass().getSimpleName());mc.setScreen(new TrailerVisualProof());
    }
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();var out=mc.renderBuffers().bufferSource();g.fill(0,0,width,height,0xff22262b);
        String[] titles={"All nine cousins","Walking and running: consecutive strides","Seated, cowering and laughing: connected joints","Porcelain fixtures, glazed sash, food and stressed door","Tracking behind the shoulders: bounded neck turns","Cooler, native wheels, painted cars and the note post","Institute enamel notices and centered brass room plates","Proofrock streetlights: fitted iron and warm glass"};g.drawString(font,titles[page],8,8,0xffe8e0d3,false);g.flush();
        if(page==7){streetlights(g,out);return;}
        if(page<3||page==4){
            int total=page==0?9:8,columns=page==0?3:4,rows=page==0?3:2;
            for(int i=0;i<total;i++){
                var m=new GoatmanChildModel(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER));float phase=(i%4)*((float)Math.PI/2);
                boolean seat=page==2&&i<2,cower=page==2&&i>=2&&i<5,heave=page==2&&i>=5;
                float speed=page==1?(i<4?.075F:.18F):0;
                if(page==4){float[] turns={-179,-135,-91,-80,80,91,135,179};m.lookPose(turns[i],25,i>=4);if(!Float.isFinite(m.head.zRot)||Math.abs(m.head.zRot)>Math.PI/2)throw new IllegalStateException("Folded head at player tracking boundary "+turns[i]);}else m.pose(phase,speed,i*11,seat,cower,heave);joints(m);
                float cellWidth=(width-16F)/columns,cellHeight=(height-24F)/rows;
                var p=g.pose();p.pushPose();p.translate(8+(i%columns+.5F)*cellWidth,24+(i/columns+1)*cellHeight-5,150);float scale=Math.min(39,cellHeight/2.1F);p.scale(scale,scale,scale);p.mulPose(Axis.YP.rotationDegrees(i%2==0?160:200));p.translate(0,-1.5,0);
                m.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(skin(page==0?i:i%9))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xffffffff);p.popPose();
            }out.endBatch();return;
        }
        if(page>=5){fixtures(g,out,page==6);return;}
        var states=new net.minecraft.world.level.block.state.BlockState[]{
            GoatmanRegistry.TOILET.get().defaultBlockState(),GoatmanRegistry.SINK.get().defaultBlockState(),GoatmanRegistry.SINK.get().defaultBlockState().setValue(TrailerFixtureBlock.FILLED,true),
            GoatmanRegistry.WINDOW.get().defaultBlockState(),GoatmanRegistry.WINDOW.get().defaultBlockState().setValue(TrailerFixtureBlock.OPEN,true)
        };
        for(int i=0;i<states.length;i++){
            if(mc.getBlockRenderer().getBlockModel(states[i])==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing trailer fixture "+states[i]);
            var p=g.pose();p.pushPose();p.translate(8+(i+.5F)*(width-16F)/5,height*.53F,150);p.scale(48,-48,48);p.mulPose(Axis.XP.rotationDegrees(20));p.mulPose(Axis.YP.rotationDegrees(140));p.translate(-.5,0,-.5);mc.getBlockRenderer().renderSingleBlock(states[i],p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();
        }
        var door=GoatmanRegistry.DOOR.get().defaultBlockState().setValue(DoorBlock.FACING,Direction.SOUTH);
        for(int i=0;i<2;i++){var p=g.pose();p.pushPose();p.translate(width-108+i*52,height-20,150);p.scale(38,-38,38);p.mulPose(Axis.YP.rotationDegrees(155));TrailerDoorRenderer.draw(door,i==0?0:1.8F,i==0?0:20,p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();}out.endBatch();
        int i=0;for(var item:new ItemStack[]{new ItemStack(GoatmanRegistry.FRANKS.get()),new ItemStack(GoatmanRegistry.RAW_FRANK.get()),new ItemStack(GoatmanRegistry.BRAT.get())}){var p=g.pose();p.pushPose();p.translate(12+i++*58,height-62,0);p.scale(3,3,1);g.renderItem(item,0,0);p.popPose();}
    }
    private void fixtures(GuiGraphics g,MultiBufferSource.BufferSource out,boolean boards){
        var mc=Minecraft.getInstance();var states=new java.util.ArrayList<net.minecraft.world.level.block.state.BlockState>();var labels=new java.util.ArrayList<String>();
        if(boards){for(var k:InstituteSignBlock.Kind.values()){states.add(InstituteSignBlock.of(k,Direction.NORTH));labels.add(k.getSerializedName().replace('_',' '));}}
        else{states.add(GoatmanRegistry.COOLER.get().defaultBlockState());labels.add("cooler");states.add(GoatmanRegistry.WHEEL.get().defaultBlockState());labels.add("RV wheel");states.add(DrownedTownRegistry.NOTICE.get().defaultBlockState());labels.add("note post");for(var k:new TownFixtureBlock.Kind[]{TownFixtureBlock.Kind.CAR_RED,TownFixtureBlock.Kind.CAR_BLUE,TownFixtureBlock.Kind.HOOD_RED,TownFixtureBlock.Kind.CAR_CABIN,TownFixtureBlock.Kind.CAR_WHEEL}){states.add(TownFixtureBlock.of(k,Direction.NORTH));labels.add(k.getSerializedName().replace('_',' '));}}
        int columns=4,rows=(states.size()+columns-1)/columns;float cw=(width-16F)/columns,ch=(height-30F)/rows;
        for(int i=0;i<states.size();i++){var state=states.get(i);if(mc.getBlockRenderer().getBlockModel(state)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing playtest fixture "+state);var pose=g.pose();pose.pushPose();pose.translate(8+(i%columns+.5F)*cw,26+(i/columns+.72F)*ch,150);float scale=Math.min(cw*.7F,ch*.7F);pose.scale(scale,-scale,scale);if(!boards)pose.mulPose(Axis.XP.rotationDegrees(20));pose.mulPose(Axis.YP.rotationDegrees(boards?180:145));pose.translate(-.5,0,-.5);mc.getBlockRenderer().renderSingleBlock(state,pose,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();g.drawString(font,labels.get(i),(int)(8+(i%columns)*cw),(int)(26+(i/columns+.92F)*ch),0xffe8e0d3,false);}out.endBatch();
    }
    private void streetlights(GuiGraphics g,MultiBufferSource.BufferSource out){
        var mc=Minecraft.getInstance();var parts=ProofrockTown.streetlights(net.minecraft.core.BlockPos.ZERO);
        var poles=new net.minecraft.core.BlockPos[]{new net.minecraft.core.BlockPos(-5,0,-9),new net.minecraft.core.BlockPos(5,0,-9),new net.minecraft.core.BlockPos(4,-11,-99)};
        String[] labels={"east arm","west arm","submerged street"};
        for(int i=0;i<poles.length;i++){
            var origin=poles[i];var p=g.pose();p.pushPose();p.translate((i+.5F)*width/3,height-35,150);p.scale(35,-35,35);p.mulPose(Axis.XP.rotationDegrees(14));p.mulPose(Axis.YP.rotationDegrees(145));
            for(var e:parts.entrySet()){var rel=e.getKey().subtract(origin);if(rel.getY()<0||rel.getY()>4||Math.abs(rel.getX())>1||rel.getZ()!=0)continue;
                var state=e.getValue();if(mc.getBlockRenderer().getBlockModel(state)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing fitted streetlight "+state);
                p.pushPose();p.translate(rel.getX()-.5,rel.getY(),-.5);mc.getBlockRenderer().renderSingleBlock(state,p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);p.popPose();
            }
            p.popPose();g.drawString(mc.font,labels[i],(int)((i+.5F)*width/3-45),height-20,0xffe8e0d3,false);
        }out.endBatch();
    }
    private static void joints(GoatmanChildModel m){
        var rotation=new Quaternionf().rotationZYX(m.body.zRot,m.body.yRot,m.body.xRot);var hip=rotation.transform(new Vector3f(0,12,0)).add(m.body.x,m.body.y,m.body.z);
        var legs=new Vector3f((m.leftLeg.x+m.rightLeg.x)/2,(m.leftLeg.y+m.rightLeg.y)/2,(m.leftLeg.z+m.rightLeg.z)/2);
        if(hip.distance(legs)>.01||new Vector3f(m.head.x,m.head.y,m.head.z).distance(m.body.x,m.body.y,m.body.z)>.01)throw new IllegalStateException("Detached torso or neck in the actual child model");
        for(var arm:new net.minecraft.client.model.geom.ModelPart[]{m.leftArm,m.rightArm}){
            var shoulder=new Quaternionf(rotation).transform(new Vector3f(arm==m.leftArm?5:-5,2,0)).add(m.body.x,m.body.y,m.body.z);
            if(shoulder.distance(arm.x,arm.y,arm.z)>.01)throw new IllegalStateException("Detached child shoulder");
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();boolean focused=Boolean.getBoolean("the_oldest_house.trailerSmoke");
        if(!focused&&!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof TrailerVisualProof proof)||++proof.frames<16)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(dir.resolve(proof.page==7?"native-streetlights-0469.png":(proof.page<4?"native-trailer-0465-":"native-playtest-0468-")+proof.page+".png"));}
        proof.frames=0;if(++proof.page<8)return;
        Files.writeString(dir.resolve("streetlights-0469-passed.txt"),"Three assembled native streetlights: both street-arm directions and the fitted waterlogged lamps of the submerged street.\n");
        TheOldestHouse.LOGGER.info("TRAILER PROOF PASSED: four native views and connected joints");
        Files.writeString(dir.resolve("trailer-0465-passed.txt"),"All nine native cousin atlases; eight walking/running stride poses; connected hips, shoulders and neck in fear/laughter/seating; actual toilet, sink states, sash states, raw/cooked food and door stress rendered in four GPU views.\n");Files.writeString(dir.resolve("playtest-0468-passed.txt"),"Three additional native GPU views: bounded tracking at eight wrap/shoulder boundaries, the cooler/wheels/cars/note post, and all twelve institute signs and number plates.\n");mc.setScreen(new InstituteVisualProof());
    }
}
