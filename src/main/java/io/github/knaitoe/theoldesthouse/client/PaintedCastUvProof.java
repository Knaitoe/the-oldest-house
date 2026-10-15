package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Compare the actual baked original vertices: a mouth repair must not mirror the unchanged painted face. */
final class PaintedCastUvProof {
    private PaintedCastUvProof(){}
    static void verify(){
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        root.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-4,-8,-4,8,6,8),PartPose.ZERO);
        root.addOrReplaceChild("jaw",CubeListBuilder.create().texOffs(0,6).addBox(-4,0,-7,8,2,8),PartPose.offset(0,-2,3));
        var original=LayerDefinition.create(mesh,64,64).bakeRoot();
        var model=new LakeWitchModel(Minecraft.getInstance().getEntityModels().bakeLayer(LakeWitchModel.LAYER));
        var expectedHead=new Vertices(0);original.getChild("head").render(new PoseStack(),expectedHead,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
        var actualHead=new Vertices(0);LakeWitchJaw.upper(model.head,0,0,new PoseStack(),actualHead,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
        compare("upper",expectedHead,actualHead,20);
        var expectedJaw=new Vertices(1);original.getChild("jaw").render(new PoseStack(),expectedJaw,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
        var actualJaw=new Vertices(1);LakeWitchJaw.render(model.head,model.jaw,0,0,new PoseStack(),actualJaw,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
        compare("jaw",expectedJaw,actualJaw,16);
        var bodyMesh=new MeshDefinition();bodyMesh.getRoot().addOrReplaceChild("body",CubeListBuilder.create().texOffs(16,16).addBox(-4,0,-2,8,12,4),PartPose.ZERO);
        var body=LayerDefinition.create(bodyMesh,128,64).bakeRoot().getChild("body");
        var expectedBody=new Vertices(2);body.render(new PoseStack(),expectedBody,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
        var actualBody=new Vertices(2);LiterarySkinMesh.dennyBody(body,new PoseStack(),actualBody,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,-1);
        compare("Denny unchanged vest faces",expectedBody,actualBody,20);
    }
    private static void compare(String part,Vertices expected,Vertices actual,int count){
        if(expected.uv.size()!=count||actual.uv.size()!=count)throw new IllegalStateException("Incomplete actual painted "+part+" UV vertices: "+expected.uv.size()+" / "+actual.uv.size());
        for(var vertex:expected.uv.entrySet()){
            var found=actual.uv.get(vertex.getKey());var wanted=vertex.getValue();
            if(found==null||Math.abs(found[0]-wanted[0])>.00001F||Math.abs(found[1]-wanted[1])>.00001F)throw new IllegalStateException("Painted "+part+" outside face changed at "+vertex.getKey()+": expected "+Arrays.toString(wanted)+", actual "+Arrays.toString(found));
        }
    }
    private static final class Vertices implements VertexConsumer {
        final Map<String,float[]> uv=new HashMap<>();final int mode;float x,y,z,u,v;
        Vertices(int mode){this.mode=mode;}
        public VertexConsumer addVertex(float x,float y,float z){this.x=x;this.y=y;this.z=z;return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){return this;}
        public VertexConsumer setUv(float u,float v){this.u=u;this.v=v;return this;}
        public VertexConsumer setUv1(int u,int v){return this;}
        public VertexConsumer setUv2(int u,int v){return this;}
        public VertexConsumer setNormal(float nx,float ny,float nz){if(mode==0?ny<.5F:mode==1?Math.abs(ny)<.5F:nz<.5F)uv.put(Math.round(x*1024)+","+Math.round(y*1024)+","+Math.round(z*1024)+"/"+Math.round(nx)+","+Math.round(ny)+","+Math.round(nz),new float[]{u,v});return this;}
    }
}
