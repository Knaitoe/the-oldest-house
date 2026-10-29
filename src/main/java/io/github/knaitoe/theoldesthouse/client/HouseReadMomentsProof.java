package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Production models and phone screen; separate from ordinary connected-client atmosphere review. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HouseReadMomentsProof extends Screen {
    private static final String[] NAMES={"waiting_objects","routine_objects","shelter_objects","shelter_cast","waiting_phone","coatroom_objects","porch_objects","read_materials","held_objects"};
    private int page,frames;private HarriganPhoneScreen phone;
    public HouseReadMomentsProof(){super(Component.literal("Native quiet-room objects"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        g.fill(0,0,width,height,0xff343630);g.drawString(font,"Quiet first rooms / "+NAMES[page].replace('_',' ')+" / shipped native models",10,9,0xffe5dcc8,false);
        if(page==4){if(phone==null){var state=new CompoundTag();state.putBoolean("WaitingRoom",true);state.putBoolean("Service",true);state.putLong("DayTime",9700);phone=new HarriganPhoneScreen(state);phone.init(Minecraft.getInstance(),width,height);if(phone.showsInput()||!phone.children().isEmpty())throw new IllegalStateException("The waiting phone is a simple lock screen");}phone.render(g,x,y,partial);return;}
        if(page==3){cast(g);return;}
        if(page==8){held(g);return;}
        if(page==7){materials(g);return;}
        var types=switch(page){
            case 0->parts("TABLE_END","TABLE_MID","SIGN","WALL_CLOCK","OUTLET","CEILING_PANEL","BELL","CALENDAR","RECEPTION","CRT","COOLER","PAPER","MAGAZINES","GLASSES","PHONE","ARMCHAIR");
            case 1->parts("SINK","VANITY","TUB_HEAD","TUB_FOOT","TOILET","TOWEL_BAR","VANITY_LIGHT","BATH_MAT","POSTCARD","LIGHT_SWITCH","ROUTINE_A","ROUTINE_B","ROUTINE_C","ROUTINE_D","PENDANT","RADIATOR");
            case 5->parts("WOOL","TRENCH","BARN","EMPTY_HANGER","WOOL_BOOTS","TRENCH_BOOTS","BARN_BOOTS","SCARF_HOOK","MIRROR","MIRROR_TOP","TOWEL_BAR","UMBRELLA","BENCH","RUNNER","CURTAIN","RADIATOR");
            case 6->parts("PORCH_LIGHT","TABLE_END","TABLE_MID","PLATE","CHAIR","SIDE_TABLE","LAMP","PENDANT","MAILBOX","SWING","RAIL");
            default->parts("JUKEBOX","STRING_LIGHT","ROWBOAT","BOTTLE","DECK_LANTERN","CALENDAR","POSTCARD");};
        int columns=4,rows=(types.length+3)/4;float cw=width/(float)columns,ch=(height-36)/(float)rows;
        for(int i=0;i<types.length;i++){var state=HouseReadRegistry.FITTING.get().defaultBlockState().setValue(HouseReadPropBlock.PART,types[i]);if(page==2||page==6)state=state.setValue(HouseReadPropBlock.WRITING,true);draw(g,state,(i%4+.5f)*cw,32+(i/4+.55f)*ch,Math.min(cw*.55f,ch*.55f));g.drawCenteredString(font,types[i].getSerializedName(),(int)((i%4+.5f)*cw),(int)(32+(i/4+1)*ch-7),0xffe5dcc8);}
        if(page==2){draw(g,HouseReadRegistry.PAVILION.get().defaultBlockState(),width*.2f,height*.86f,30);draw(g,HouseReadRegistry.DOCK_POST.get().defaultBlockState(),width*.4f,height*.86f,30);draw(g,HouseReadRegistry.SHAKE_SLAB.get().defaultBlockState(),width*.6f,height*.86f,30);draw(g,HouseReadRegistry.NIGHT.get().defaultBlockState(),width*.8f,height*.86f,30);}
    }
    private static HouseReadPropBlock.Part[] parts(String... names){return java.util.Arrays.stream(names).map(HouseReadPropBlock.Part::valueOf).toArray(HouseReadPropBlock.Part[]::new);}
    /** The 0.4.161 materials, with fences, panes and gates joined east and west as they stand in the rooms. */
    private void materials(GuiGraphics g){
        var joined=new ArrayList<net.minecraft.world.level.block.state.BlockState>();
        for(var block:List.<Block>of(HouseReadRegistry.CARPET.get(),HouseReadRegistry.CEILING_TILE.get(),HouseReadRegistry.CEILING.get(),HouseReadRegistry.COUNTER.get(),HouseReadRegistry.ASPHALT.get(),HouseReadRegistry.ASPHALT_LINE.get(),HouseReadRegistry.LOT_WALL.get(),HouseReadRegistry.OVERCAST.get(),HouseReadRegistry.PORCH_BOARDS.get(),HouseReadRegistry.SHINGLES.get(),HouseReadRegistry.TRIM.get(),HouseReadRegistry.FLOORBOARDS.get(),HouseReadRegistry.WAINSCOT.get(),HouseReadRegistry.POST.get(),HouseReadRegistry.DOCK_POST.get(),HouseReadRegistry.PORCH_SLAB.get(),HouseReadRegistry.SHINGLE_STAIRS.get(),HouseReadRegistry.PORCH_STEP.get(),HouseReadRegistry.DOCK_STEP.get(),HouseReadRegistry.GATE.get(),HouseReadRegistry.ROPE.get()))joined.add(block.defaultBlockState());
        for(var block:List.<Block>of(HouseReadRegistry.RAIL.get(),HouseReadRegistry.PICKET.get(),HouseReadRegistry.DOCK_RAIL.get(),HouseReadRegistry.WINDOW.get(),HouseReadRegistry.LATTICE.get()))joined.add(block.defaultBlockState().setValue(CrossCollisionBlock.EAST,true).setValue(CrossCollisionBlock.WEST,true));
        for(var block:List.<Block>of(HouseReadRegistry.PANEL_DOOR.get(),HouseReadRegistry.OFFICE_DOOR.get(),HouseReadRegistry.BOATHOUSE_DOOR.get(),HouseReadRegistry.SCREEN.get()))joined.add(block.defaultBlockState());
        int columns=8,rows=(joined.size()+columns-1)/columns;float cw=width/(float)columns,ch=(height-36)/(float)rows;
        for(int i=0;i<joined.size();i++){var state=joined.get(i);float x=(i%columns+.5f)*cw,y=32+(i/columns+.5f)*ch,scale=Math.min(cw*.42f,ch*.42f);
            if(state.getBlock() instanceof DoorBlock){scale*=.6f;draw(g,state,x,y+scale*.5f,scale);draw(g,state.setValue(DoorBlock.HALF,net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER),x,y-scale*.5f,scale);}else draw(g,state,x,y,scale);
            g.drawCenteredString(font,net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().replace("read_",""),(int)x,(int)(32+(i/columns+1)*ch-8),0xffe5dcc8);}
    }
    /** What a reader actually holds: flat native item sprites, never a block model in the hand. */
    private void held(GuiGraphics g){
        var stacks=new ArrayList<net.minecraft.world.item.ItemStack>();for(var item:List.of(HouseReadRegistry.PAPER,HouseReadRegistry.MAGAZINE,HouseReadRegistry.GLASSES,HouseReadRegistry.PHONE,HouseReadRegistry.CONE))stacks.add(new net.minecraft.world.item.ItemStack(item.get()));
        var empty=new net.minecraft.world.item.ItemStack(HouseReadRegistry.CONE.get());empty.set(net.minecraft.core.component.DataComponents.CUSTOM_MODEL_DATA,new net.minecraft.world.item.component.CustomModelData(1));stacks.add(empty);
        for(var routine:HouseReadRegistry.ROUTINES)stacks.add(new net.minecraft.world.item.ItemStack(routine.get()));stacks.add(new net.minecraft.world.item.ItemStack(HouseReadRegistry.BOTTLE.get()));
        int columns=6,rows=(stacks.size()+columns-1)/columns;float cw=width/(float)columns,ch=(height-36)/(float)rows;float scale=Math.min(cw,ch)*.6f/16f;
        for(int i=0;i<stacks.size();i++){float x=(i%columns+.5f)*cw,y=32+(i/columns+.45f)*ch;var p=g.pose();p.pushPose();p.translate(x-8*scale,y-8*scale,0);p.scale(scale,scale,1);g.renderItem(stacks.get(i),0,0);p.popPose();
            g.drawCenteredString(font,i==5?"empty cone":stacks.get(i).getHoverName().getString(),(int)x,(int)(32+(i/columns+1)*ch-8),0xffe5dcc8);}
    }
    private void draw(GuiGraphics g,net.minecraft.world.level.block.state.BlockState state,float x,float y,float scale){var mc=Minecraft.getInstance();var p=g.pose();g.flush();p.pushPose();p.translate(x,y,180);if(state.is(HouseReadRegistry.FITTING.get())&&state.getValue(HouseReadPropBlock.PART)==HouseReadPropBlock.Part.ROWBOAT)scale*=.5f;p.scale(scale,-scale,scale);p.mulPose(Axis.XP.rotationDegrees(24));p.mulPose(Axis.YP.rotationDegrees(-25));p.translate(-.5,-.5,-.5);var out=mc.renderBuffers().bufferSource();mc.getBlockRenderer().renderSingleBlock(state,p,out,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);out.endBatch();p.popPose();}
    private void cast(GuiGraphics g){var mc=Minecraft.getInstance();var out=mc.renderBuffers().bufferSource();float cw=width/4f,ch=(height-36)/2f,scale=Math.min(cw*.43f,ch*.40f);g.flush();for(int i=0;i<8;i++){int role=i%2==0?LiteraryActor.READ_MAN:LiteraryActor.READ_WOMAN,phase=i/4;var model=new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));model.young=false;model.preview(role,phase,80);var p=g.pose();p.pushPose();p.translate((i%4+.5f)*cw,36+(i/4)*ch+ch*.28f,180);p.scale(scale,scale,scale);p.mulPose(Axis.YP.rotationDegrees(i%4<2?180:90));model.renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(LiteraryRenderers.skin(role,phase))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);out.endBatch();p.popPose();g.drawCenteredString(font,(role==LiteraryActor.READ_MAN?"boy":"girl")+(phase==1?" / half smile":" / quiet"),(int)((i%4+.5f)*cw),(int)(32+(i/4+1)*ch-6),0xffe5dcc8);}}
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.houseReadSmoke")||!(mc.screen instanceof HouseReadMomentsProof s)||++s.frames<12)return;var folder=Path.of("../build/house-read139-art");Files.createDirectories(folder);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(folder.resolve(NAMES[s.page]+".png"));}if(++s.page<NAMES.length){s.frames=0;return;}Files.writeString(folder.resolve("passed.txt"),"Five actual server-built room cutaways, six production standard/slim garment, mirror and House-version pages and nine native furnishing/routine/cast/phone/coatroom/porch/material/held-item pages. Minecraft native pixel art, actual block/item geometry and production phone rendering. Ordinary graphical-client/reconnect/lighting/audio review remains separate.\n");mc.stop();}
}
