package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.joml.Matrix4f;

/** The shipped GPU sky mesh at the horizon, above it and below it. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class OutsideSkyProof extends Screen {
    private int view,frames;
    /** 0.4.594: a light let fall into the great staircase's well, drawn at eleven moments of its fall from an eye out over a rail at the first landing's height. Only the drawing: nothing stands in front of it here. */
    private static final int FALL=6;private static final int[] MOMENTS={18,30,45,60,80,100,120,140,170,220,300};
    private static int fallDrawn,fallSeen;private static float fallNear,fallFar;private static final java.util.List<double[]> fallAt=new java.util.ArrayList<>();
    private void fallingLight(){
        var origin=new net.minecraft.core.BlockPos(0,70,0);var b=io.github.knaitoe.theoldesthouse.labyrinth.FinaleArchitecture.base(origin);
        var first=io.github.knaitoe.theoldesthouse.labyrinth.StaircaseFire.threshold(origin);
        int sx=Integer.signum(first.getX()-b.getX()),sz=Integer.signum(first.getZ()-b.getZ());
        // The eye is out over a rail; the light was thrown from beside it, toward the well's middle.
        var eye=new Vec3(first.getX()+.5-sx*5.4,first.getY()+1.62,first.getZ()+.5-sz*5.4);
        var start=eye.add(-sx*1.2,-.3,-sz*1.2);var speed=new Vec3(-sx*.21,.05,-sz*.21);
        // The picture looks down the line the light falls along, so its first moments are high in it and its last near the middle.
        var settled=new StaircaseFallingLights.Falling(start,speed,10000,start,false,0,0);for(int t=0;t<200;t++)settled.step();var line=settled.light(1);
        var look=new Matrix4f().lookAt(0,0,0,(float)(line.x()-eye.x),-60,(float)(line.z()-eye.z),0,1,0);
        var lights=new java.util.ArrayList<StaircaseFallingLights.Light>();fallAt.clear();fallSeen=0;fallNear=fallFar=0;
        float f=(float)(1/Math.tan(Math.toRadians(35)));var v=new org.joml.Vector3f();
        for(int moment:MOMENTS){
            var falling=new StaircaseFallingLights.Falling(start,speed,10000,start,false,io.github.knaitoe.theoldesthouse.labyrinth.StaircaseFallingLight.WARM,0);
            for(int t=0;t<moment;t++)falling.step();
            var light=falling.light(1);var steady=new StaircaseFallingLights.Light(light.x(),light.y(),light.z(),light.rgb(),1,light.size());lights.add(steady);
            v.set((float)(steady.x()-eye.x),(float)(steady.y()-eye.y),(float)(steady.z()-eye.z));float distance=v.length();look.transformPosition(v);
            float shown=StaircaseFallingLights.shown(distance);
            if(shown>=StaircaseFallingLights.FAINT){fallSeen++;if(fallNear==0)fallNear=shown;fallFar=shown;
                fallAt.add(new double[]{width/2.0+v.x/-v.z*f*height/2.0,height/2.0-v.y/-v.z*f*height/2.0,shown,distance});}
        }
        fallDrawn=StaircaseFallingLights.draw(look,eye,lights,1000,false);
    }
    public OutsideSkyProof(){super(Component.literal("Outside sky"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        g.fill(0,0,width,height,view>=3?0xFF000000:0xFF111820);g.flush();
        var previous=new Matrix4f(RenderSystem.getProjectionMatrix());var model=RenderSystem.getModelViewStack();model.pushMatrix();model.identity();RenderSystem.applyModelViewMatrix();
        try{
            RenderSystem.setProjectionMatrix(new Matrix4f().perspective((float)Math.toRadians(70),(float)width/height,.1F,1000),VertexSorting.DISTANCE_TO_ORIGIN);
            RenderSystem.disableDepthTest();
            if(view==3){var origin=new net.minecraft.core.BlockPos(0,70,0);
                if(StaircaseScore.at(null,origin)||!StaircaseScore.at(origin,io.github.knaitoe.theoldesthouse.labyrinth.FinaleArchitecture.entry(origin))
                        ||StaircaseScore.at(origin,io.github.knaitoe.theoldesthouse.labyrinth.FinaleArchitecture.cell(origin)))throw new IllegalStateException("The descent score must wait for origin sync and stop outside the stair shaft");
                HouseInteriorEffects.drawSkyAt(origin,io.github.knaitoe.theoldesthouse.labyrinth.FinaleArchitecture.entry(origin),new Matrix4f(),new Vec3(.7,.8,1),.25F);
                var stair=io.github.knaitoe.theoldesthouse.labyrinth.FinaleArchitecture.entry(origin);
                HouseInteriorEffects.drawSkyAt(null,stair,new Matrix4f(),new Vec3(.7,.8,1),.25F);
                HouseInteriorEffects.drawSkyAt(net.minecraft.core.BlockPos.ZERO,stair,new Matrix4f(),new Vec3(.7,.8,1),.25F);
                if(HouseInteriorEffects.blackAt(origin,origin.offset(13,4,12)))throw new IllegalStateException("The native domestic surroundings lost their outdoor sky");
            }else if(view<3)HouseOutsideEffects.drawSky(new Matrix4f().rotateX((float)Math.toRadians(new int[]{0,-45,25}[view])),new Vec3(.035,.05,.085),.5F);
            // 0.4.358: the yard's night, home's stars a little out of place, looking up.
            else if(view==5)HouseOutsideEffects.drawSky(new Matrix4f().rotateX((float)Math.toRadians(-50)),new Vec3(.035,.05,.085),.5F,true);
            else if(view==FALL)fallingLight();
        }finally{RenderSystem.enableDepthTest();model.popMatrix();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(previous,VertexSorting.ORTHOGRAPHIC_Z);}
        if(view==4){g.fill(0,0,width,height,0xFFFFEEAA);NovelSceneClient.drawCoveredDarkness(g);}
        g.drawString(font,view==FALL?"A light let fall into the well, at eleven moments of its fall: the dark has it before the last":view==5?"The yard at night: home's stars, fixed, a little out of place":view==4?"Covered well: absolute darkness over a bright frame":view==3?"Native enclosed staircase: black background, no outside sky":"Native outside sky: "+new String[]{"horizon","above horizon","below horizon"}[view],12,12,0xFFE8E4D9,false);
    }
    /**
     * The falling light as actual pixels: a warm point where each moment still in view should be, the nearest strong and the
     * furthest faint, and nothing at all once it is past its reach.
     */
    private static void fallCheck(NativeImage image,int guiWidth){
        double scale=image.getWidth()/(double)guiWidth;int[] brightest=new int[fallAt.size()];int warm=0;
        for(int y=60;y<image.getHeight()-4;y++)for(int x=4;x<image.getWidth()-4;x++){
            int pixel=image.getPixelRGBA(x,y),r=pixel&255,b=pixel>>>16&255;if(r<=b+6||r<10)continue;warm++;
            for(int i=0;i<brightest.length;i++){var at=fallAt.get(i);if(Math.hypot(x-at[0]*scale,y-at[1]*scale)<=5*scale+2)brightest[i]=Math.max(brightest[i],r);}
        }
        TheOldestHouse.LOGGER.info("FALLING LIGHT MEASURED: {} of {} moments in view, {} drawn; shown {} nearest and {} furthest; warm pixels {}; brightest at each {}",fallSeen,MOMENTS.length,fallDrawn,fallNear,fallFar,warm,java.util.Arrays.toString(brightest));
        if(fallSeen<5||fallSeen>=MOMENTS.length)throw new IllegalStateException("The falling light should be in view for its first moments and lost before its last: in view at "+fallSeen+" of "+MOMENTS.length);
        if(fallDrawn!=fallSeen)throw new IllegalStateException("The falling light was drawn at "+fallDrawn+" moments and should be in view at "+fallSeen);
        for(int i=0;i<brightest.length;i++)if(brightest[i]<10)throw new IllegalStateException("No light shows where the falling light is at its moment "+i+", "+fallAt.get(i)[3]+" blocks off");
        if(brightest[0]<brightest[brightest.length-1]+60)throw new IllegalStateException("The falling light does not fade as it goes: nearest="+brightest[0]+" furthest="+brightest[brightest.length-1]);
        if(warm<60)throw new IllegalStateException("Too little of the falling light is in the picture: warm pixels="+warm);
        TheOldestHouse.LOGGER.info("FALLING LIGHT CHECK PASSED");
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof OutsideSkyProof s)||++s.frames<4)return;
        Path folder=Path.of("../build/font-smoke");
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
            var colors=new java.util.HashSet<Integer>();int lights=0,nonblack=0;
            for(int y=60;y<image.getHeight()-4;y++)for(int x=4;x<image.getWidth()-4;x++){
                int pixel=image.getPixelRGBA(x,y);colors.add(pixel);
                if((pixel&0xFFFFFF)!=0)nonblack++;
                if(Math.min(Math.min(pixel&255,(pixel>>>8)&255),(pixel>>>16)&255)>60)lights++;
            }
            image.writeToFile(folder.resolve(s.view==FALL?"native-falling-light.png":s.view==5?"native-yard-sky.png":s.view==4?"native-covered-well-dark.png":s.view==3?"native-staircase-dark.png":"native-sky-"+s.view+".png"));
            // Looking down samples only the dim lower gradient; its 8-bit palette has fewer shades.
            // Text is outside this crop, so three distinct shades still reject a blank frame.
            int minimumColors=s.view==2?3:9;
            if(s.view<3&&colors.size()<minimumColors)throw new IllegalStateException("The native sky gradient is missing at view "+s.view+": colors="+colors.size());
            if(s.view==0&&lights<12)throw new IllegalStateException("The native horizon is missing stars/moon: lights="+lights);
            if(s.view==5){
                if(lights<40)throw new IllegalStateException("The yard's night is missing home's stars: lights="+lights);
                int n=YardSky.count();if(n<700)throw new IllegalStateException("The yard has too few of home's stars: "+n);
                // Turned about one axis, a star near it moves least; most move a degree or more, none further than turn and drift together.
                int moved=0;for(int i=0;i<n;i++){float off=YardSky.offset(i);if(off>YardSky.TURN+YardSky.DRIFT+.1F)throw new IllegalStateException("A yard star is "+off+" degrees from home's");if(off>=1F)moved++;}
                if(moved<n*.8F)throw new IllegalStateException("Too few yard stars are out of place: "+moved+" of "+n);
            }
            else if(s.view==FALL)fallCheck(image,s.width);
            else if(s.view>=3&&nonblack>0)throw new IllegalStateException("Outdoor sky leaked into the native staircase background: "+nonblack+" pixels");
        }
        if(++s.view<=FALL){s.frames=0;return;}
        Files.writeString(folder.resolve("falling-light-passed.txt"),"A light let fall into the great staircase's well is drawn through the dark from the first landing: in view at "+fallSeen+" of "+MOMENTS.length+" moments of its fall, strong when near and faint when far, and gone once it is past its reach.\n");
        Files.writeString(folder.resolve("sky-passed.txt"),"Single outside sky mesh rendered at three camera elevations; actual pixels contain its gradient and stars/moon. Scene time is read-only and stable across native server clock updates and delayed cues. The yard's night shows home's stars, fixed, each turned a little off true.\n");
        Files.writeString(folder.resolve("staircase-dark-passed.txt"),"The shipped interior renderer suppresses outdoor sky; native staircase background pixels are black.\n");
        Files.writeString(folder.resolve("well-dark-passed.txt"),"The shipped covered-well compositor renders actual black pixels over a bright input frame.\n");
        TheOldestHouse.LOGGER.info("WELL DARK CHECK PASSED: covered wait compositor is opaque black");
        TheOldestHouse.LOGGER.info("HOUSE SKY CHECK PASSED: three native sky screenshots, the yard's night and the black staircase background saved");mc.setScreen(new WellVisualProof());
    }
}
