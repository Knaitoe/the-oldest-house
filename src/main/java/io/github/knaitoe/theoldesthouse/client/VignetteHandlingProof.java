package io.github.knaitoe.theoldesthouse.client;
import com.google.gson.*;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import com.mojang.serialization.JsonOps;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** Standing-view-angle close-ups of actual server ray-use snapshots, through the production renderer. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class VignetteHandlingProof extends Screen {
 private static boolean launched;private final List<Path> files;private int index,frames;private String name;private VignetteHandlingBlockEntity object;private net.minecraft.client.resources.sounds.SoundInstance preview;
 @SubscribeEvent public static void launch(ClientTickEvent.Post e)throws Exception{var mc=Minecraft.getInstance();if(Boolean.getBoolean("the_oldest_house.handlingSmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new VignetteHandlingProof());}}
 private VignetteHandlingProof()throws Exception{super(Component.literal("Object handling review"));try(var f=Files.list(Path.of("../build/handling-proof"))){files=f.filter(p->p.toString().endsWith(".json")).sorted().toList();}if(files.size()!=24)throw new IllegalStateException("Expected 24 actual server handling snapshots, got "+files.size());load();}
 private void load()throws Exception{
  var j=JsonParser.parseString(Files.readString(files.get(index))).getAsJsonObject();name=j.get("name").getAsString();var s=BlockState.CODEC.parse(JsonOps.INSTANCE,j.get("state")).getOrThrow();object=new VignetteHandlingBlockEntity(BlockPos.ZERO,s);object.handleUpdateTag(TagParser.parseTag(j.get("data").getAsString()),Minecraft.getInstance().level==null?net.minecraft.core.RegistryAccess.EMPTY: Minecraft.getInstance().level.registryAccess());frames=0;
  var mc=Minecraft.getInstance();if(preview!=null)mc.getSoundManager().stop(preview);var v=object.view();
  var sound=switch(object.kind()){
   case TOY_PIANO->LiteraryRegistry.HANDLING_TOY.get();case SPINET->LiteraryRegistry.HANDLING_SPINET.get();case SLIDES->LiteraryRegistry.HANDLING_SLIDE.get();case MODEL_KIT->LiteraryRegistry.HANDLING_WOOD.get();
   case TAPE->v.mode()==2?LiteraryRegistry.HANDLING_WIND.get():v.mode()==1?LiteraryRegistry.HANDLING_TAPE.get(Math.min(31,v.cursor()/10)).get():LiteraryRegistry.HANDLING_CLICK.get();};
  if(mc.getSoundManager().getSoundEvent(sound.getLocation())==null)throw new IllegalStateException("Missing production handling sound "+sound.getLocation());
  preview=net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(sound,(object.kind()==VignetteHandlingBlock.Kind.TOY_PIANO||object.kind()==VignetteHandlingBlock.Kind.SPINET)?(float)Math.pow(2,Math.max(0,v.key())/12.0):1F,.25F);mc.getSoundManager().play(preview);

 }
 @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
 @Override public void render(GuiGraphics g,int x,int y,float partial){
  var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff292b28);g.drawString(font,"This House Has Ghosts in It / native object handling",16,12,0xffe9debe,false);g.drawString(font,name.replace('_',' '),16,30,0xfff0e7d1,false);
  var v=object.view();String detail=switch(object.kind()){case TOY_PIANO,SPINET->"Picked key: "+v.key()+" / individual pitched native sound";case SLIDES->"Slide: "+(v.slide()+1)+" / tray "+(v.tray()?"out":"in")+" / light "+(v.lamp()?"on":"off");case MODEL_KIT->"Chair cell: "+v.cells()[0]+" / quarter turns: "+v.turns()[0]+" / lifted piece: "+v.held();case TAPE->"Tape position: "+v.cursor()+" / 320 ticks / "+(v.mode()==1?"playing":v.mode()==2?"rewinding":"stopped");};
  g.drawString(font,detail,16,48,0xffc1c7b3,false);g.drawString(font,"Actual server-use snapshot / production geometry / standing view angle / full bright",16,height-18,0xffbec3b2,false);g.flush();
  var pose=g.pose();pose.pushPose();pose.translate(width/2.,height*.62,260);float scale=Math.min(width*.55F,height*.64F);pose.scale(scale,-scale,scale);pose.mulPose(Axis.XP.rotationDegrees(29));pose.mulPose(Axis.YP.rotationDegrees(158+90*VignetteHandlingBlock.turns(object.getBlockState().getValue(VignetteHandlingBlock.FACING))));pose.translate(-.5,-.35,-.5);
  var buffers=mc.renderBuffers().bufferSource();VignetteHandlingRenderer.draw(object.getBlockState(),v,0,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();
 }
 @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
  var mc=Minecraft.getInstance();if(!(mc.screen instanceof VignetteHandlingProof s)||++s.frames<7)return;
  var dir=Path.of("../build/font-smoke/handling");Files.createDirectories(dir);try(NativeImage im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(dir.resolve(s.name+".png"));}
  if(++s.index<s.files.size())s.load();else{Files.writeString(Path.of("../build/font-smoke/handling-passed.txt"),"24 native close-ups from actual server ray-pick/use snapshots; keys/pitches, slides/tray/light, spare model placement/rotation and finite tape transport. Production geometry, standing view angle; not a normal-lighting playthrough.\\n");mc.stop();}
 }
}
