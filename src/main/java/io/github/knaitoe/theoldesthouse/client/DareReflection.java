package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

/**
 * 0.4.173, "Third Floor Girls'": what the bathroom's glass does to what it shows, count by count. The reflection is the
 * reader's own House appearance throughout; only these change, and only in the glass.
 * <ul>
 * <li>First count: nothing yet. 0.4.199 (owner: "the changes to the players face ... shouldn't start until the second count at
 * least and should be much more gradual. like, difficult to discern until the face returns to normal").</li>
 * <li>Second count: so slowly it is hard to say anything changed, one eye drifts a pixel aside, a fraction at a time, and the
 * mouth begins to pale; the first stall stands a few inches ajar in the glass while the real one is shut.</li>
 * <li>Then, through the third count, the other eye drifts, the mouth pales away and comes back wrong, lower and wider, its
 * far corner dragged down, each as a slow fade. It is only when the face snaps back to the reader's own that the difference
 * is plain.</li>
 * <li>Third count: the reflection's candle is gone and its arms hang; a second flame rises behind its left ear; a woman
 * stands at its back, chin on its shoulder, fingers on its collarbones, her head tilted back so she looks at the reader.</li>
 * </ul>
 */
public final class DareReflection implements HouseReadReflection.Hook {
    public static final DareReflection HOOK=new DareReflection();
    public static final ResourceLocation WOMAN=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/dare_woman.png");
    public static final ResourceLocation FLAME=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/dare_flame.png");
    private static final ResourceLocation FACE=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"dare_face");
    private DareReflection(){}

    // ------------------------------------------------------------------------------------------------ the stall
    @Override public BlockState block(BlockPos pos,BlockState actual){
        int count=DareClient.count();
        if(count>=2&&count<4&&DareFittingBlock.is(actual,DareFittingBlock.Kind.STALL_DOOR)&&actual.getValue(DareFittingBlock.STAGE)<2)return actual.setValue(DareFittingBlock.STAGE,actual.getValue(DareFittingBlock.STAGE)+2);
        return actual;
    }
    @Override public int version(){int count=DareClient.count();return count>=2&&count<4?1:0;}

    // ------------------------------------------------------------------------------------------------ the candle
    private ItemStack heldMain=ItemStack.EMPTY,heldOff=ItemStack.EMPTY;private boolean emptied;
    private static boolean third(AbstractClientPlayer p){return DareClient.count()==3&&p==Minecraft.getInstance().player;}
    @Override public void before(AbstractClientPlayer p){
        if(!third(p))return;var inv=p.getInventory();heldMain=inv.items.get(inv.selected);heldOff=inv.offhand.get(0);
        inv.items.set(inv.selected,ItemStack.EMPTY);inv.offhand.set(0,ItemStack.EMPTY);emptied=true;
    }
    @Override public void after(AbstractClientPlayer p){
        if(!emptied)return;var inv=p.getInventory();inv.items.set(inv.selected,heldMain);inv.offhand.set(0,heldOff);heldMain=heldOff=ItemStack.EMPTY;emptied=false;
    }

    // ------------------------------------------------------------------------------------------------ the face
    /** The reader's own face, base and hat layers, as NativeImage pixels, and where the House found the eyes and mouth. */
    private static final int[] base=new int[64],hat=new int[64];
    private static ResourceLocation loadedSkin;private static int skinTone;private static List<Integer> leftEye=List.of(),rightEye=List.of(),mouth=List.of();private static int mouthRow=6,mouthInk;
    private static DynamicTexture faceTexture;private static String faceKey="";
    private static int r(int c){return c&255;}private static int g(int c){return c>>8&255;}private static int b(int c){return c>>16&255;}private static int a(int c){return c>>>24;}
    private static int abgr(int a,int r,int g,int b){return a<<24|b<<16|g<<8|r;}
    private static int composed(int i){return a(hat[i])>=128?hat[i]:base[i];}
    private static boolean differs(int c){return a(c)<128||Math.abs(r(c)-r(skinTone))+Math.abs(g(c)-g(skinTone))+Math.abs(b(c)-b(skinTone))>60;}
    /** Reads the skin's face from the GPU once per skin: as the House's body check does, it is only on the card, not on disk. */
    private static boolean load(ResourceLocation skin){
        if(skin.equals(loadedSkin))return true;var mc=Minecraft.getInstance();
        var tex=mc.getTextureManager().getTexture(skin);RenderSystem.bindTexture(tex.getId());
        int w=GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D,0,GL11.GL_TEXTURE_WIDTH),h=GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D,0,GL11.GL_TEXTURE_HEIGHT);
        if(w<64||h<32)return false;int k=w/64;
        try(var image=new NativeImage(w,h,false)){
            image.downloadTexture(0,false);
            for(int y=0;y<8;y++)for(int x=0;x<8;x++){base[y*8+x]=image.getPixelRGBA((8+x)*k,(8+y)*k)|0xff000000;hat[y*8+x]=image.getPixelRGBA((40+x)*k,(8+y)*k);}
        }
        detect(base,hat);loadedSkin=skin;faceKey="";return true;
    }
    /** Proofs only: a face given directly, base and hat layers, ABGR. */
    public static void face(int[] faceBase,int[] faceHat){System.arraycopy(faceBase,0,base,0,64);System.arraycopy(faceHat,0,hat,0,64);detect(base,hat);loadedSkin=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"proof_face");faceKey="";}
    private static void detect(int[] faceBase,int[] faceHat){
        var counts=new HashMap<Integer,Integer>();
        for(int y=5;y<8;y++)for(int x=1;x<7;x++){int c=composed(y*8+x);if(a(c)>=128&&HouseBodies.skinColour(r(c),g(c),b(c)))counts.merge(c,1,Integer::sum);}
        if(counts.isEmpty())for(int y=4;y<8;y++)for(int x=0;x<8;x++){int c=composed(y*8+x);if(a(c)>=128)counts.merge(c,1,Integer::sum);}
        skinTone=counts.entrySet().stream().max(Map.Entry.<Integer,Integer>comparingByValue().thenComparing(Map.Entry.comparingByKey())).map(Map.Entry::getKey).orElse(abgr(255,0xc9,0x96,0x6f));
        int eyeRow=4,best=-1;for(int y=2;y<=5;y++){int n=0;for(int x=1;x<7;x++)if(differs(composed(y*8+x)))n++;if(n>best){best=n;eyeRow=y;}}
        var left=new ArrayList<Integer>();var right=new ArrayList<Integer>();
        for(int y=eyeRow;y<=Math.min(5,eyeRow+1);y++){if(y>eyeRow){int n=0;for(int x=1;x<7;x++)if(differs(composed(y*8+x)))n++;if(n<2)break;}
            for(int x=1;x<7;x++)if(differs(composed(y*8+x)))(x<4?left:right).add(y*8+x);}
        if(left.isEmpty()&&right.isEmpty()){left.addAll(List.of(eyeRow*8+1,eyeRow*8+2));right.addAll(List.of(eyeRow*8+5,eyeRow*8+6));}
        leftEye=left;rightEye=right;
        int lowest=Math.max(eyeRow,left.isEmpty()?eyeRow:left.getLast()/8);mouthRow=Math.min(7,lowest+2);best=0;var m=new ArrayList<Integer>();
        // The mouth is looked for between the cheeks only, so a beard or a jaw line is not taken for it; the upper row wins a tie.
        for(int y=Math.min(7,lowest+2);y<8;y++){var row=new ArrayList<Integer>();for(int x=2;x<6;x++)if(differs(composed(y*8+x)))row.add(y*8+x);if(row.size()>best){best=row.size();mouthRow=y;m=row;}}
        mouth=m;mouthInk=m.stream().map(DareReflection::composed).min(Comparator.comparingInt(c->r(c)+g(c)+b(c))).orElse(abgr(255,0x3a,0x1a,0x1a));
    }
    /** Moves one feature a pixel: what it leaves behind is skin on the face and nothing on the hat. */
    private static void shift(int[] layer,List<Integer> feature,int dx,int dy,boolean isHat){
        var old=layer.clone();for(int i:feature){layer[i]=isHat?0:skinTone;}
        for(int i:feature){int x=i%8+dx,y=i/8+dy;if(x>=0&&x<8&&y>=0&&y<8)layer[y*8+x]=old[i];}
    }
    private static int blend(int c,int to,float t){return abgr(255,(int)Mth.lerp(t,r(c),r(to)),(int)Mth.lerp(t,g(c),g(to)),(int)Mth.lerp(t,b(c),b(to)));}
    /** Each change fades in over this many steps, so no single frame jumps. */
    static final int STEPS=8;
    /** How far through a fade that starts at {@code start} and lasts {@code length} ticks, in steps. */
    private static int step(float age,float start,float length){return Mth.clamp((int)((age-start)/length*STEPS),0,STEPS);}
    /**
     * The face at this count and moment, as a key that changes only when a pixel does: four digits, each in eighths, for the
     * left eye's drift, the right eye's, the mouth paling and the wrong mouth coming in. The second count lasts about 26
     * seconds of staring; the third until the reader holds still.
     */
    static String key(int count,float age){
        int eyeL=0,eyeR=0,pale=0,wrong=0;
        if(count==2){eyeL=step(age,160,280);pale=Math.min(STEPS/2,step(age,300,400));}
        else if(count==3){eyeL=STEPS;eyeR=step(age,0,160);pale=Math.max(STEPS/2,step(age,-80,160));wrong=step(age,60,200);}
        return ""+eyeL+eyeR+pale+wrong;
    }
    /** A layer part way to another: each pixel's colour and opacity a fraction of the way there. */
    private static void toward(int[] layer,int[] target,float t){
        for(int i=0;i<64;i++){int c=layer[i],d=target[i];if(c==d)continue;
            layer[i]=((int)Mth.lerp(t,a(c),a(d))<<24)|((int)Mth.lerp(t,b(c),b(d))<<16)|((int)Mth.lerp(t,g(c),g(d))<<8)|(int)Mth.lerp(t,r(c),r(d));}
    }
    private static void build(String key){
        var faceBase=base.clone();var faceHat=hat.clone();
        float eyeL=(key.charAt(0)-'0')/(float)STEPS,eyeR=(key.charAt(1)-'0')/(float)STEPS,pale=(key.charAt(2)-'0')/(float)STEPS,wrongIn=(key.charAt(3)-'0')/(float)STEPS;
        // Each eye drifts a pixel by fading from where it was to where it goes, never in one jump.
        if(eyeL>0){int dx=leftEye.stream().allMatch(i->i%8>0)?-1:0;var b=faceBase.clone();var h=faceHat.clone();shift(b,leftEye,dx,dx==0?1:0,false);shift(h,leftEye,dx,dx==0?1:0,true);toward(faceBase,b,eyeL);toward(faceHat,h,eyeL);}
        if(eyeR>0){int dy=rightEye.stream().allMatch(i->i/8>0)?-1:0;var b=faceBase.clone();var h=faceHat.clone();shift(b,rightEye,dy==0?1:0,dy,false);shift(h,rightEye,dy==0?1:0,dy,true);toward(faceBase,b,eyeR);toward(faceHat,h,eyeR);}
        if(pale>0)for(int i:mouth){faceBase[i]=blend(faceBase[i],skinTone,pale);if(a(faceHat[i])>0)faceHat[i]=(faceHat[i]&0xffffff)|((int)(a(faceHat[i])*(1-pale))<<24);}
        if(wrongIn>0){
            // It comes back a pixel to one side and a pixel too long; then its far corner sags (or, on the lowest row, lifts).
            var back=new ArrayList<Integer>();if(mouth.isEmpty())back.addAll(List.of(mouthRow*8+3,mouthRow*8+4));else back.addAll(mouth);
            int far=back.stream().mapToInt(i->i%8).max().getAsInt(),farRow=back.stream().filter(i->i%8==far).findFirst().get()/8;
            var wrong=new ArrayList<Integer>();for(int i:back){int x=Math.min(7,i%8+1);wrong.add(i/8*8+x);}if(far+2<8)wrong.add(farRow*8+far+2);
            {int end=wrong.stream().mapToInt(i->i%8).max().getAsInt();int endRow=wrong.stream().filter(i->i%8==end).findFirst().get()/8;wrong.removeIf(i->i%8==end);wrong.add((endRow<7?endRow+1:endRow-1)*8+end);}
            for(int i:wrong){faceBase[i]=blend(faceBase[i],mouthInk,wrongIn);faceHat[i]=0;}
        }
        var image=new NativeImage(16,8,true);
        for(int y=0;y<8;y++)for(int x=0;x<8;x++){image.setPixelRGBA(x,y,faceBase[y*8+x]);image.setPixelRGBA(8+x,y,faceHat[y*8+x]);}
        if(faceTexture==null){faceTexture=new DynamicTexture(image);Minecraft.getInstance().getTextureManager().register(FACE,faceTexture);}
        else{faceTexture.setPixels(image);faceTexture.upload();}
    }
    /** Proofs: the face for a count and moment, from the face given to {@link #face(int[],int[])}. */
    static void frame(int count,float age){String key=key(count,age);if(!key.equals(faceKey)){build(key);faceKey=key;}}
    @Override public void face(PlayerModel<AbstractClientPlayer> model,AbstractClientPlayer player,PoseStack pose,MultiBufferSource out,int light){
        int count=DareClient.count();if(count<1||count>3||player!=Minecraft.getInstance().player||!load(player.getSkin().texture()))return;
        String key=key(count,DareClient.age(0));if(!key.equals(faceKey)){build(key);faceKey=key;}
        if(key.equals("0"))return;
        drawFace(model.head,pose,out,light);
    }
    /** The changed face over the head's front: the face layer just proud of the head, the hat layer just proud of the hat. */
    static void drawFace(net.minecraft.client.model.geom.ModelPart head,PoseStack pose,MultiBufferSource out,int light){
        pose.pushPose();head.translateAndRotate(pose);var buffer=out.getBuffer(RenderType.entityTranslucent(FACE));
        quad(pose,buffer,-4,-8,4,0,-4.03F,0,.5F,light);quad(pose,buffer,-4.5F,-8.5F,4.5F,.5F,-4.53F,.5F,1,light);
        pose.popPose();
    }
    private static void quad(PoseStack pose,VertexConsumer b,float x0,float y0,float x1,float y1,float z,float u0,float u1,int light){
        var m=pose.last().pose();float s=1/16F;
        b.addVertex(m,x0*s,y0*s,z*s).setColor(255,255,255,255).setUv(u0,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose.last(),0,0,-1);
        b.addVertex(m,x0*s,y1*s,z*s).setColor(255,255,255,255).setUv(u0,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose.last(),0,0,-1);
        b.addVertex(m,x1*s,y1*s,z*s).setColor(255,255,255,255).setUv(u1,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose.last(),0,0,-1);
        b.addVertex(m,x1*s,y0*s,z*s).setColor(255,255,255,255).setUv(u1,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose.last(),0,0,-1);
    }

    // ------------------------------------------------------------------------------------------------ the woman and the second flame
    private static PlayerModel<AbstractClientPlayer> woman;
    static PlayerModel<AbstractClientPlayer> womanModel(){if(woman==null)woman=new PlayerModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_SLIM),true);return woman;}
    /** How far she leans in over the reader's shoulder, about her feet. */
    static final float LEAN=.3F;
    @Override public void extra(PoseStack pose,MultiBufferSource out,Vec3 camera,float partial,Vec3 plane,Vec3 normal){
        var mc=Minecraft.getInstance();var p=mc.player;if(DareClient.count()!=3||p==null)return;
        float age=DareClient.age(partial),bodyYaw=Mth.rotLerp(partial,p.yBodyRotO,p.yBodyRot);
        // The reflected camera, behind the glass: she looks there, which in the glass is at the reader.
        var reflected=camera.subtract(normal.scale(2*camera.subtract(plane).dot(normal)));
        draw(pose,out,p.getPosition(partial).subtract(camera),p.getEyeHeight(),bodyYaw,reflected.subtract(camera),Mth.clamp(age/8F,0,1),age,LevelRenderer.getLightColor(mc.level,BlockPos.containing(p.getEyePosition(partial))));
    }
    /**
     * Her and the flame behind the reader whose feet are at {@code feet} (camera-relative), with eyes {@code eyeHeight} up and
     * body turned {@code bodyYaw}, she looking at {@code look}; drawn at {@code alpha}.
     */
    static void draw(PoseStack pose,MultiBufferSource out,Vec3 feet,float eyeHeight,float bodyYaw,Vec3 look,float alpha,float age,int light){
        double rad=Math.toRadians(bodyYaw);var forward=new Vec3(-Math.sin(rad),0,Math.cos(rad));var right=new Vec3(-Math.cos(rad),0,-Math.sin(rad));
        // Right behind the reader and over their right shoulder (the reflection's left), leaning in.
        var at=feet.add(forward.scale(-.42)).add(right.scale(.42));
        var model=womanModel();for(var part:new net.minecraft.client.model.geom.ModelPart[]{model.head,model.hat,model.body,model.jacket,model.leftArm,model.rightArm,model.leftSleeve,model.rightSleeve,model.leftLeg,model.rightLeg,model.leftPants,model.rightPants})part.resetPose();
        model.young=false;model.crouching=false;model.riding=false;model.attackTime=0;
        var head=at.add(forward.scale(1.5*Math.sin(LEAN))).add(0,1.5*Math.cos(LEAN),0);var d=look.subtract(head);
        float yaw=Mth.clamp(Mth.wrapDegrees((float)Math.toDegrees(Math.atan2(-d.x,d.z))-bodyYaw),-55,55);
        float pitch=(float)Math.toDegrees(-Math.atan2(d.y,Math.sqrt(d.x*d.x+d.z*d.z)));
        // Owner: her head tilted back, so her attention is on the reader. The lean is taken back out of it, and a little more.
        var aim=new org.joml.Quaternionf().rotationYXZ(yaw*Mth.DEG_TO_RAD,pitch*Mth.DEG_TO_RAD-LEAN-.16F,.22F).getEulerAnglesZYX(new org.joml.Vector3f());
        model.head.xRot=aim.x;model.head.yRot=aim.y;model.head.zRot=aim.z;
        // Her arms come forward round the reader to the collarbones, the near one across, fingers pressed in.
        model.rightArm.xRot=-1.3F;model.rightArm.yRot=-1.05F;model.rightArm.zRot=.1F;
        model.leftArm.xRot=-1.35F;model.leftArm.yRot=-.55F;model.leftArm.zRot=-.04F;
        model.rightLeg.xRot=.05F;model.leftLeg.xRot=-.05F;
        model.hat.copyFrom(model.head);model.jacket.copyFrom(model.body);model.rightSleeve.copyFrom(model.rightArm);model.leftSleeve.copyFrom(model.leftArm);model.rightPants.copyFrom(model.rightLeg);model.leftPants.copyFrom(model.leftLeg);
        pose.pushPose();pose.translate(at.x,at.y,at.z);pose.mulPose(Axis.YP.rotationDegrees(180-bodyYaw));pose.mulPose(Axis.XP.rotation(-LEAN));
        pose.scale(-1,-1,1);pose.scale(.9375F,.9375F,.9375F);pose.translate(0,-1.501F,0);
        int colour=((int)(alpha*255)<<24)|0xffffff;
        model.renderToBuffer(pose,out.getBuffer(RenderType.entityTranslucent(WOMAN)),light,OverlayTexture.NO_OVERLAY,colour);
        pose.popPose();
        // The second flame, behind the reflection's left ear: the reader's right, a hand's breadth back.
        // Behind the reflection's left ear, where its head and hers meet, rising clear of both.
        var flame=feet.add(0,eyeHeight+.22,0).add(right.scale(.3)).add(forward.scale(-.3));
        float flicker=1+.12F*Mth.sin(age*1.7F)+.06F*Mth.sin(age*4.3F+1),w=.08F*alpha,h=.19F*flicker*alpha;
        var side=right;var m=pose.last().pose();var buffer=out.getBuffer(RenderType.entityTranslucent(FLAME));
        float[][] corners={{-1,0,0,1},{1,0,1,1},{1,1,1,0},{-1,1,0,0}};
        for(float[] c:corners){var v=flame.add(side.scale(c[0]*w)).add(0,c[1]*h,0);buffer.addVertex(m,(float)v.x,(float)v.y,(float)v.z).setColor(255,255,255,(int)(alpha*235)).setUv(c[2],c[3]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(),0,0,1);}
    }
}
