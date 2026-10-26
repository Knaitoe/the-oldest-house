package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/** Tiny independent, full-bright bodies inside the existing jar's actual cavity. */
public final class FireflyJar {
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/block/cabin/firefly.png");
    private FireflyJar(){}
    public record Fly(double x,double y,double z,float glow){}
    public static Fly sample(int index,double age,long seed){
        double phase=index*2.399+(seed&255)*.017;
        double x=.5+Math.sin(age*.018+phase)*(.09+index*.004);
        double y=.16+index*.054+Math.sin(age*.025+phase*1.7)*.026;
        double z=.5+Math.cos(age*.014+phase)*.105;
        double pulse=Math.max(0,Math.sin(age*(.036+index*.004)+phase));
        return new Fly(x,y,z,(float)(pulse*pulse*pulse));
    }
    public static void render(PoseStack pose,MultiBufferSource out,double age,BlockPos at,int overlay){
        var vertices=out.getBuffer(RenderType.entityTranslucent(TEXTURE));
        for(int i=0;i<6;i++){
            var f=sample(i,age,at.asLong());if(f.glow()<.015F)continue;
            pose.pushPose();pose.translate(f.x(),f.y(),f.z());pose.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
            var p=pose.last();float r=.022F;int alpha=(int)(255*f.glow());
            vertices.addVertex(p,-r,-r,0).setColor(255,255,255,alpha).setUv(0,1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(p,0,0,1);
            vertices.addVertex(p,r,-r,0).setColor(255,255,255,alpha).setUv(1,1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(p,0,0,1);
            vertices.addVertex(p,r,r,0).setColor(255,255,255,alpha).setUv(1,0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(p,0,0,1);
            vertices.addVertex(p,-r,r,0).setColor(255,255,255,alpha).setUv(0,0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(p,0,0,1);
            pose.popPose();
        }
    }
}
