package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 0.4.333, State Route 9: the highway's anchor draws the traffic where the shared scene says, carried on smoothly between updates.
 * Each vehicle is built of boxes at sixteen pixels to the block, its cabin hollow behind glass you can see through, so every car on the
 * road can be seen to be empty; the wagon's seats are full of still people whose heads turn to the reader (or do not, for a reader the
 * House read as one who learned to keep out of sight, crouched in the median). Lamps and their pools on the wet tar are drawn at full
 * brightness, a pickup's short and warm, a semi's long; three seconds before a semi comes out of the hedge its shine slides down the
 * lane ahead of it. With the road dark, nothing is lit.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class StateRouteRenderer implements BlockEntityRenderer<StateRouteScene> {
    public StateRouteRenderer(BlockEntityRendererProvider.Context ignored){}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(StateRouteRegistry.SCENE.get(),StateRouteRenderer::new);}
    static ResourceLocation texture(String m){
        String path=m.startsWith("dr:")?"deer_road/"+m.substring(3):"state_route/"+m;
        return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/block/"+path+".png");
    }
    @Override public int getViewDistance(){return 160;}
    @Override public boolean shouldRenderOffScreen(StateRouteScene s){return true;}
    @Override public AABB getRenderBoundingBox(StateRouteScene s){var b=s.base();return new AABB(b.getX()+StateRouteSite.WEST-14,b.getY()-3,b.getZ()+StateRouteSite.FAR,b.getX()+StateRouteSite.EAST+14,b.getY()+8,b.getZ()+2);}
    /** Where a vehicle is now: its last synced place carried on at its speed (never more than a short way past the update). */
    static double x(StateRouteScene s,StateRouteScene.Vehicle v,float now){
        if(s.mode==StateRouteScene.DARK)return v.x;
        return v.x+v.dir()*v.speed*Mth.clamp(now-s.syncedAt,0,12);
    }
    @Override public void render(StateRouteScene s,float partial,PoseStack p,MultiBufferSource out,int light,int overlay){
        if(s.getLevel()==null)return;float now=s.getLevel().getGameTime()+partial;var a=StateRouteSite.ANCHOR;boolean lit=s.mode!=StateRouteScene.DARK;
        var mc=Minecraft.getInstance();var me=mc.player;
        for(var v:s.traffic){
            double vx=x(s,v,now),vz=StateRouteSite.LANE_Z[v.lane];
            int at=net.minecraft.client.renderer.LevelRenderer.getLightColor(s.getLevel(),s.base().offset((int)Math.floor(vx),1,(int)Math.floor(vz)));
            p.pushPose();p.translate(vx-a.getX(),-a.getY(),vz-a.getZ());p.mulPose(Axis.YP.rotationDegrees(v.dir()>0?-90:90));
            switch(v.kind){
                case StateRouteScene.PICKUP->pickup(p,out,at,overlay,v,lit);
                case StateRouteScene.SEMI->semi(p,out,at,overlay,v,lit);
                case StateRouteScene.WAGON->{wagon(p,out,at,overlay,v,lit);if(me!=null)heads(s,p,out,overlay,v,vx,vz,me);}
                default->car(p,out,at,overlay,v,lit);
            }
            p.popPose();
        }
        // The shine of a semi still behind the hedge, sliding down the wet tar of its lane ahead of it.
        if(lit)for(var c:s.coming){
            float left=c.arrives()-now;if(left>StateRoute.WARN||left<0)continue;int dir=StateRouteSite.dir(c.lane());
            double head=-dir*StateRoute.SEMI_SPEED*left;double tail=head-dir*(18+20*(1-left/StateRoute.WARN));
            double from=Mth.clamp(Math.min(head,tail),-StateRouteSite.HEDGE-3,StateRouteSite.HEDGE+3),to=Mth.clamp(Math.max(head,tail),-StateRouteSite.HEDGE-3,StateRouteSite.HEDGE+3);
            if(to-from<.5)continue;float strength=Mth.clamp(1-left/StateRoute.WARN,0,1)*.8F+.2F;
            p.pushPose();p.translate(-a.getX(),-a.getY(),-a.getZ());
            flat(p,out,"shine",(float)from,(float)StateRouteSite.LANE_Z[c.lane()]-.25F,(float)to,(float)StateRouteSite.LANE_Z[c.lane()]+.25F,.03F,strength);
            p.popPose();
        }
    }

    // ------------------------------------------------------------------ the vehicles, front toward -Z, sixteen pixels to the block
    private static String paint(StateRouteScene.Vehicle v){return "paint_"+Math.floorMod(v.paint,6);}
    private static void wheels(PoseStack p,MultiBufferSource out,int light,int overlay,float half,float[] zs,float r){
        for(float z:zs)for(int side=-1;side<=1;side+=2){float x0=side<0?-half-1:half-3,x1=side<0?-half+3:half+1;
            box(p,out,x0,0,z-r,x1,r*2,z+r,"dr:tyre",light,overlay);box(p,out,side<0?x0-.1F:x1,r-2.5F,z-2.5F,side<0?x0:x1+.1F,r+2.5F,z+2.5F,"dr:hub",light,overlay);}
    }
    private static void lamps(PoseStack p,MultiBufferSource out,int light,int overlay,float half,float front,float rear,float y,boolean lit,float reach,int warmth){
        int bright=lit?LightTexture.FULL_BRIGHT:light;
        box(p,out,-half+2,y,front-.3F,-half+8,y+4,front,"dr:headlamp",bright,overlay);box(p,out,half-8,y,front-.3F,half-2,y+4,front,"dr:headlamp",bright,overlay);
        box(p,out,-half+2,y+1,rear,-half+6,y+4,rear+.3F,"dr:tail",bright,overlay);box(p,out,half-6,y+1,rear,half-2,y+4,rear+.3F,"dr:tail",bright,overlay);
        if(lit)flat(p,out,warmth==1?"beam_warm":"beam",-(half+6)/16F,(front-reach*16)/16F,(half+6)/16F,front/16F,.02F,.55F);
    }
    /** A cabin hollow behind glass: a roof on four pillars, panes you can see through, the seats inside empty. */
    private static void cabin(PoseStack p,MultiBufferSource out,int light,int overlay,float half,float y0,float y1,float z0,float z1,String paint){
        float w=half-1;
        box(p,out,-w,y1-1.5F,z0,w,y1,z1,paint,light,overlay);
        for(float x:new float[]{-w,w-1.5F})for(float z:new float[]{z0,z1-1.5F})box(p,out,x,y0,z,x+1.5F,y1-1.5F,z+1.5F,paint,light,overlay);
        pane(p,out,-w+1.5F,y0,z0+.2F,w-1.5F,y1-1.5F,z0+.4F,light,overlay);pane(p,out,-w+1.5F,y0,z1-.4F,w-1.5F,y1-1.5F,z1-.2F,light,overlay);
        pane(p,out,-w,y0,z0+1.5F,-w+.3F,y1-1.5F,z1-1.5F,light,overlay);pane(p,out,w-.3F,y0,z0+1.5F,w,y1-1.5F,z1-1.5F,light,overlay);
        box(p,out,-w+2,y0,z0+3,w-2,y0+1,z1-2,"dr:carpet",light,overlay);
        box(p,out,-w+2,y0,(z0+z1)/2-1,-1,y0+5,(z0+z1)/2+3,"dr:vinyl",light,overlay);box(p,out,1,y0,(z0+z1)/2-1,w-2,y0+5,(z0+z1)/2+3,"dr:vinyl",light,overlay);
    }
    private static void car(PoseStack p,MultiBufferSource out,int light,int overlay,StateRouteScene.Vehicle v,boolean lit){
        float half=16,len=37;String c=paint(v);
        box(p,out,-half,4,-len,half,15,len,c,light,overlay);box(p,out,-half+1,2,-len,half-1,4,len,"dr:under",light,overlay);
        box(p,out,-half+3,7,-len-.4F,half-3,12,-len,"dr:grille",light,overlay);box(p,out,-half,4,-len-.6F,half,6,-len,"dr:chrome",light,overlay);
        cabin(p,out,light,overlay,half,15,27,-12,16,c);
        wheels(p,out,light,overlay,half,new float[]{-24,24},5);lamps(p,out,light,overlay,half,-len,len,9,lit,7,0);
    }
    private static void pickup(PoseStack p,MultiBufferSource out,int light,int overlay,StateRouteScene.Vehicle v,boolean lit){
        float half=17,len=40;String c=paint(v);
        box(p,out,-half,5,-len,half,17,-8,c,light,overlay);box(p,out,-half+1,3,-len,half-1,5,len,"dr:under",light,overlay);
        box(p,out,-half+3,8,-len-.4F,half-3,14,-len,"dr:grille",light,overlay);box(p,out,-half,5,-len-.6F,half,7,-len,"dr:chrome",light,overlay);
        cabin(p,out,light,overlay,half,17,31,-22,-6,c);
        // The bed: a floor and three low walls, open at the top.
        box(p,out,-half,5,-8,half,8,len,c,light,overlay);
        box(p,out,-half,8,-6,-half+2,17,len,c,light,overlay);box(p,out,half-2,8,-6,half,17,len,c,light,overlay);box(p,out,-half,8,len-2,half,17,len,c,light,overlay);
        wheels(p,out,light,overlay,half,new float[]{-28,26},6);lamps(p,out,light,overlay,half,-len,len,10,lit,4,1);
    }
    private static void semi(PoseStack p,MultiBufferSource out,int light,int overlay,StateRouteScene.Vehicle v,boolean lit){
        float half=19,front=-112;
        // The cab: a long nose, the sleeper behind it, glass you can see is empty.
        box(p,out,-half,8,front,half,26,front+28,"cab",light,overlay);box(p,out,-half+3,12,front-.4F,half-3,24,front,"dr:grille",light,overlay);
        box(p,out,-half,6,front-.8F,half,10,front,"dr:chrome",light,overlay);
        box(p,out,-half,8,front+28,half,28,front+46,"cab",light,overlay);cabin(p,out,light,overlay,half,28,46,front+28,front+44,"cab");
        box(p,out,-half,28,front+44,half,54,front+62,"cab",light,overlay);
        for(int x=-12;x<=12;x+=8)box(p,out,x-1,54,front+44,x+1,55,front+46,"dr:amber",lit?LightTexture.FULL_BRIGHT:light,overlay);
        // The trailer: a flat bed of bunks with logs chained down, the bark ends toward you.
        box(p,out,-half,12,front+66,half,17,112,"trailer",light,overlay);
        box(p,out,-half+2,17,front+70,half-2,47,108,"logs",light,overlay);
        box(p,out,-half+2,17,front+69.6F,half-2,47,front+70,"log_end",light,overlay);box(p,out,-half+2,17,108,half-2,47,108.4F,"log_end",light,overlay);
        for(float z=front+80;z<104;z+=24){box(p,out,-half,17,z,-half+1.5F,50,z+2,"dr:chrome",light,overlay);box(p,out,half-1.5F,17,z,half,50,z+2,"dr:chrome",light,overlay);}
        wheels(p,out,light,overlay,half,new float[]{front+14,front+52,front+64,80,96},7);lamps(p,out,light,overlay,half,front,112,14,lit,10,0);
    }
    private static void wagon(PoseStack p,MultiBufferSource out,int light,int overlay,StateRouteScene.Vehicle v,boolean lit){
        float half=16,len=43;
        box(p,out,-half,4,-len,half,16,len,"wagon",light,overlay);box(p,out,-half+1,2,-len,half-1,4,len,"dr:under",light,overlay);
        box(p,out,-half-.3F,7,-len+8,-half,14,len-4,"panel_wood",light,overlay);box(p,out,half,7,-len+8,half+.3F,14,len-4,"panel_wood",light,overlay);
        box(p,out,-half+3,7,-len-.4F,half-3,13,-len,"dr:grille",light,overlay);box(p,out,-half,4,-len-.6F,half,6,-len,"dr:chrome",light,overlay);
        cabin(p,out,light,overlay,half,16,28,-18,len-2,"wagon");
        box(p,out,-half+2,16,8,half-2,21,12,"dr:vinyl",light,overlay);box(p,out,-half+2,16,26,half-2,21,30,"dr:vinyl",light,overlay);
        wheels(p,out,light,overlay,half,new float[]{-28,30},5);lamps(p,out,light,overlay,half,-len,len,9,lit,6,0);
    }
    /** The wagon's people: still, dark, packed into every seat; their heads turn to the reader. */
    private static final float[][] SEATS={{-8,-6},{8,-6},{-8,10},{0,10},{8,10},{-8,28},{0,28},{8,28}};
    private static void heads(StateRouteScene s,PoseStack p,MultiBufferSource out,int overlay,StateRouteScene.Vehicle v,double vx,double vz,net.minecraft.world.entity.player.Player me){
        boolean hidden=s.unseen.contains(me.getUUID());int light=LightTexture.pack(2,4);
        for(var seat:SEATS){
            box(p,out,seat[0]-4,16,seat[1]-2,seat[0]+4,24,seat[1]+3,"silhouette",light,overlay);
            float turn=0;
            if(!hidden){
                // The vehicle's frame is turned to face along its lane; turn each head from there toward the reader's eyes.
                var b=s.base();double hx=b.getX()+vx+(v.dir()>0?-seat[1]:seat[1])/16.0,hz=b.getZ()+vz+(v.dir()>0?seat[0]:-seat[0])/16.0;
                double dx=me.getX()-hx,dz=me.getZ()-hz;float world=(float)Math.toDegrees(Math.atan2(-dx,dz));float facing=v.dir()>0?-90:90;
                turn=Mth.clamp(Mth.wrapDegrees(world-facing-180),-110,110);
            }
            p.pushPose();p.translate(seat[0]/16F,24/16F,seat[1]/16F);p.mulPose(Axis.YP.rotationDegrees(-turn));
            box(p,out,-2.5F,0,-2.5F,2.5F,6,2.5F,"silhouette",light,overlay);
            p.popPose();
        }
    }

    // ------------------------------------------------------------------ drawing
    private static void pane(PoseStack p,MultiBufferSource out,float x0,float y0,float z0,float x1,float y1,float z1,int light,int overlay){box(p,out,x0,y0,z0,x1,y1,z1,"glass",light,overlay);}
    static void box(PoseStack p,MultiBufferSource buffers,float x0,float y0,float z0,float x1,float y1,float z1,String material,int light,int overlay){
        var out=buffers.getBuffer(material.equals("glass")?RenderType.entityTranslucent(texture(material)):RenderType.entityCutoutNoCull(texture(material)));
        face(p,out,x0,y0,z0,x1-x0,y1-y0,0,0,-1,light,overlay);
        face(p,out,x1,y0,z1,x1-x0,y1-y0,0,0,1,light,overlay);
        face(p,out,x0,y0,z1,z1-z0,y1-y0,-1,0,0,light,overlay);
        face(p,out,x1,y0,z0,z1-z0,y1-y0,1,0,0,light,overlay);
        face(p,out,x0,y0,z1,x1-x0,z1-z0,0,-1,0,light,overlay);
        face(p,out,x0,y1,z0,x1-x0,z1-z0,0,1,0,light,overlay);
    }
    /** A glow lying flat on the wet tar (in blocks): headlight pools and the semi's shine. Additive, fading at its edges by its texture. */
    static void flat(PoseStack p,MultiBufferSource buffers,String material,float x0,float z0,float x1,float z1,float y,float alpha){
        var out=buffers.getBuffer(RenderType.entityTranslucentEmissive(texture(material)));int a=Mth.clamp((int)(alpha*255),0,255);int colour=(a<<24)|0xFFFFFF;
        var m=p.last();int light=LightTexture.FULL_BRIGHT;int ov=net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;
        out.addVertex(m,x0,y,z0).setColor(colour).setUv(0,0).setOverlay(ov).setLight(light).setNormal(m,0,1,0);
        out.addVertex(m,x0,y,z1).setColor(colour).setUv(0,1).setOverlay(ov).setLight(light).setNormal(m,0,1,0);
        out.addVertex(m,x1,y,z1).setColor(colour).setUv(1,1).setOverlay(ov).setLight(light).setNormal(m,0,1,0);
        out.addVertex(m,x1,y,z0).setColor(colour).setUv(1,0).setOverlay(ov).setLight(light).setNormal(m,0,1,0);
    }
    /** Tiled at sixteen pixels to the block, as the county road's car is, rather than stretched. */
    private static void face(PoseStack p,VertexConsumer out,float x,float y,float z,float width,float height,int nx,int ny,int nz,int light,int overlay){
        for(float v=0;v<height;v+=16)for(float u=0;u<width;u+=16){
            float w=Math.min(16,width-u),h=Math.min(16,height-v);
            for(int i=0;i<4;i++){
                float du=(i==1||i==2)?w:0,dv=i>=2?h:0,aa=u+du,c=v+dv;
                float X=x+(nx==0?(nz==1?-aa:aa):0),Y=y+(ny==0?c:0),Z=z+(nx!=0?(nx<0?-aa:aa):ny!=0?(ny<0?-c:c):0);
                out.addVertex(p.last(),X/16,Y/16,Z/16).setColor(-1).setUv(du/16,dv/16).setOverlay(overlay).setLight(light).setNormal(p.last(),nx,ny,nz);
            }
        }
    }
    /** Every material the road draws with; tools/draw_state_route_art.py draws the ones of its own. */
    static final List<String> MATERIALS=List.of("paint_0","paint_1","paint_2","paint_3","paint_4","paint_5","cab","trailer","logs","log_end","wagon","panel_wood","glass","silhouette","beam","beam_warm","shine");
}
