package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.VertexConsumer;

/** Fit exceptional painted nets in the mesh; never resample or repaint the approved PNGs. */
final class LiterarySkinMesh {
    private LiterarySkinMesh(){}
    static float dennyLegV(float v){float pixel=v*64;return (pixel<=20?pixel+20:40+(pixel-20)*(23F/12))/64;}
    /** The four-sided denim/boot net is v36..63; the final three PNG rows are transparent export padding. */
    static VertexConsumer dennyLeg(VertexConsumer out){
        return new VertexConsumer(){
            public VertexConsumer addVertex(float x,float y,float z){out.addVertex(x,y,z);return this;}
            public VertexConsumer setColor(int r,int g,int b,int a){out.setColor(r,g,b,a);return this;}
            public VertexConsumer setUv(float u,float v){out.setUv(u,dennyLegV(v));return this;}
            public VertexConsumer setUv1(int u,int v){out.setUv1(u,v);return this;}
            public VertexConsumer setUv2(int u,int v){out.setUv2(u,v);return this;}
            public VertexConsumer setNormal(float x,float y,float z){out.setNormal(x,y,z);return this;}
        };
    }
}
