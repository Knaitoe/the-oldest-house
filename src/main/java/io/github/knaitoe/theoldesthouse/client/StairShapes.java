package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import io.github.knaitoe.theoldesthouse.labyrinth.StairCompany;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

/**
 * 0.4.596: one of a reader's people as they meet it on the way down the great staircase (docs/STAIR_COMPANY_0_4_596.md).
 *
 * <p>The stair is too dark for a figure standing on it to be seen at all: in a real client a figure eight blocks off was no
 * brighter than the bare treads beside it. So a shape is not drawn by the light that is there. It is drawn pale and flat, with
 * nothing of its face or dress to be read, and dimmed to what a pale body is with no light on it at the reader's own brightness
 * setting: as a body beside them would look, seen further off than the dark lets anything be seen. Nothing of it until the reader
 * is nearly within {@link StairCompany#SHOWN_FROM}, as plain as it gets within {@link StairCompany#SHOWN_FULL}, and less as it
 * goes. Dimmer than the stair behind it, it is not drawn: it would be a hole in the stair and not a figure.
 *
 * <p>It is done with what every figure is already drawn with: the pale is the overlay a struck or swelling body is flashed with,
 * the dimming is the fog, set for this one figure so that it stands at the wanted depth of it. Nothing is lit by it.
 */
public final class StairShapes {
    private StairShapes(){}
    private static boolean drawing;
    /** True while a shape is being drawn, so the renderer does not begin again inside itself. */
    public static boolean drawing(){return drawing;}
    /** The flash a body is paled with, at its whitest. */
    static final int PALE=OverlayTexture.pack(OverlayTexture.u(1F),OverlayTexture.v(false));
    /** How far the set fog reaches: wide enough that every part of the figure lies at one depth of it. */
    static final float DEPTH=4096;
    /** How bright a shape is at its plainest, of a pale body standing beside the reader with no light on it. */
    static final float PLAIN=1F;
    /** Less of it showing than this, a shape is not drawn: about where it is as dark as the treads behind it. */
    static final float LEAST=.35F;
    /**
     * How bright a pale body is with no light on it, of white, at a brightness setting (0 moody, 1 bright): the darkest the game's
     * light goes (LightTexture). A shape is measured against it, so it is as dim beside what is round it on every setting.
     */
    static float unlit(double brightness){
        double g=Mth.clamp(brightness,0,1),dark=.03,lifted=1-Math.pow(1-dark,4);
        return (float)((dark+g*(lifted-dark))*.96+.03);
    }
    /** How deep in the set fog a thing stands for this much of it to be left: the fog thickens as a smooth step, not evenly. */
    static float depth(float left){
        double gone=Mth.clamp(1-left,0,1);
        return (float)(.5-Math.sin(Math.asin(1-2*gone)/3));
    }

    /** Draws a shape through {@code body}, which is the figure's own drawing handed the buffers to use. */
    public static void draw(LiteraryActor a,float partial,MultiBufferSource out,Consumer<MultiBufferSource> body){
        var eye=Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double distance=eye.distanceTo(a.getPosition(partial).add(0,a.getBbHeight()*.5,0));
        float shown=StairCompany.shown(distance,a.fade());if(shown<LEAST)return;
        float left=Math.min(1,PLAIN*unlit(Minecraft.getInstance().options.gamma().get())*shown);
        // Buffers that cannot be drawn out now cannot be given a fog of their own: nothing is drawn rather than a lit figure.
        if(!(out instanceof MultiBufferSource.BufferSource source))return;
        source.endBatch();
        float start=RenderSystem.getShaderFogStart(),end=RenderSystem.getShaderFogEnd();float[] colour=RenderSystem.getShaderFogColor().clone();
        float near=(float)distance-depth(left)*DEPTH;
        RenderSystem.setShaderFogStart(near);RenderSystem.setShaderFogEnd(near+DEPTH);RenderSystem.setShaderFogColor(0,0,0,colour[3]);
        drawing=true;
        try{body.accept(type->new Pale(source.getBuffer(type)));source.endBatch();}
        finally{drawing=false;RenderSystem.setShaderFogStart(start);RenderSystem.setShaderFogEnd(end);RenderSystem.setShaderFogColor(colour[0],colour[1],colour[2],colour[3]);}
    }

    /** Whatever is drawn through it is drawn pale, whole and as if fully lit: the fog does the rest. */
    private record Pale(VertexConsumer to) implements VertexConsumer {
        @Override public VertexConsumer addVertex(float x,float y,float z){to.addVertex(x,y,z);return this;}
        @Override public VertexConsumer setColor(int red,int green,int blue,int alpha){to.setColor(255,255,255,255);return this;}
        @Override public VertexConsumer setUv(float u,float v){to.setUv(u,v);return this;}
        @Override public VertexConsumer setUv1(int u,int v){to.setUv1(PALE&0xffff,PALE>>16&0xffff);return this;}
        @Override public VertexConsumer setUv2(int u,int v){to.setUv2(LightTexture.FULL_BRIGHT&0xffff,LightTexture.FULL_BRIGHT>>16&0xffff);return this;}
        @Override public VertexConsumer setNormal(float x,float y,float z){to.setNormal(x,y,z);return this;}
        @Override public void addVertex(float x,float y,float z,int color,float u,float v,int overlay,int light,float nx,float ny,float nz){
            to.addVertex(x,y,z,-1,u,v,PALE,LightTexture.FULL_BRIGHT,nx,ny,nz);
        }
    }
}
