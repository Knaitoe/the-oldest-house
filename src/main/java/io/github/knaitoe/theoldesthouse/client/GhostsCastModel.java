package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;

/** Connected elbows, knees and jaw on the six domestic characters; each pose remains a possible human movement. */
public final class GhostsCastModel extends HumanoidModel<LiteraryActor> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"ghosts_cast"),"main");
    private final ModelPart rightElbow,leftElbow,rightKnee,leftKnee,jaw,lids,hair,hairEnds,hairLeft,hairRight,ponytail,ponyEnds;
    private int currentRole,currentPhase,currentDay;private boolean currentHurt;
    private float blink,hairMotion;
    /** 0.4.242: brow lift, brow tilt (inner ends up is worry, down is anger) and mouth corners, each -1..1. */
    private final float[] face=new float[3];
    private final java.util.List<ModelPart> joints;
    private final java.util.WeakHashMap<LiteraryActor,Tween> transitions=new java.util.WeakHashMap<>();
    private static final class Tween {int phase,seat;float started,seed;float[] from,last,faceFrom,faceLast;String line="";float lineAt=-1000;Tween(int phase){this.phase=phase;}}
    /**
     * 0.4.324 (owner: "a better sitting/standing animation for npcs, particularly in the ghost set"): sitting down and standing up.
     * Which poses stand, which sit at a table, and whether a change of pose is someone sitting down (1), getting up (-1) or neither (0).
     * The server moves the body between the seat and the floor beside it in the same tick (GhostsMotion.sit and rise); the renderer
     * carries it across over the same time as the blend here, and the blend bends at the hips and knees on the way.
     */
    static boolean upright(int role,int phase){return phase==GhostsSet.STAND||phase==GhostsSet.HELP||phase==GhostsSet.PRAY&&role!=LiteraryActor.GHOST_MERRY;}
    static boolean seated(int role,int phase){return GhostsSet.seatedPose(role,phase)&&phase!=GhostsSet.SLIP&&phase!=GhostsSet.DEAD;}
    public static int seatChange(int role,int from,int to){
        // 0.4.325: down onto the floor (2) and up off it (-2), for Merry in the basement.
        if(upright(role,from)&&to==GhostsSet.FLOOR)return 2;if(from==GhostsSet.FLOOR&&upright(role,to))return -2;
        return upright(role,from)&&seated(role,to)?1:seated(role,from)&&upright(role,to)?-1:0;
    }
    /** How long each kind of sitting down or getting up takes, in ticks. */
    static float seatTicks(int seat){return seat==1?GhostsMotion.SIT_TICKS:seat==-1?GhostsMotion.RISE_TICKS:24;}
    /**
     * 0.4.309, the living layer (docs/GHOSTS_ANIMATION_0_4_309.md): how long the person has held this pose (for a fall's flailing and an
     * impact's settling), how far through speaking a line they are (0 silent), and the per-pose blend length.
     */
    private float since=1000,speech;
    public GhostsCastModel(ModelPart root){super(root);rightElbow=rightArm.getChild("elbow");leftElbow=leftArm.getChild("elbow");rightKnee=rightLeg.getChild("knee");leftKnee=leftLeg.getChild("knee");jaw=head.getChild("jaw");lids=head.getChild("lids");hair=head.getChild("long_hair");hairEnds=hair.getChild("ends");hairLeft=hair.getChild("left_lock");hairRight=hair.getChild("right_lock");ponytail=head.getChild("ponytail");ponyEnds=ponytail.getChild("tail");joints=root.getAllParts().toList();}
    public static LayerDefinition layer(){
        var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);var r=mesh.getRoot();
        for(boolean left:new boolean[]{false,true}){
            var arm=r.addOrReplaceChild(left?"left_arm":"right_arm",SegmentedSkin.limb(40,16,0,left?-1:-3,-2,-2,4,6,4,left,true),PartPose.offset(left?5:-5,2,0));
            arm.addOrReplaceChild("elbow",SegmentedSkin.limb(40,16,6,left?-1:-3,0,-2,4,6,4,left,true),PartPose.offset(0,4,0));
            var leg=r.addOrReplaceChild(left?"left_leg":"right_leg",SegmentedSkin.limb(0,16,0,-2,0,-2,4,6,4,left,false),PartPose.offset(left?1.9F:-1.9F,12,0));
            leg.addOrReplaceChild("knee",SegmentedSkin.limb(0,16,6,-2,0,-2,4,6,4,left,false),PartPose.offset(0,6,0));
        }
        // Explicit face UVs keep the opened jaw's inner surface out of the eye/forehead island.
        var head=r.addOrReplaceChild("head",CubeListBuilder.create(),PartPose.ZERO);
        head.addOrReplaceChild("jaw",CubeListBuilder.create(),PartPose.offset(0,-2,0));
        head.addOrReplaceChild("lids",CubeListBuilder.create(),PartPose.ZERO);
        // Her own long hair: a back curtain to the waist and loose, uneven locks beside the face.
        var hair=head.addOrReplaceChild("long_hair",CubeListBuilder.create().texOffs(80,0).addBox(-4,-5.5F,3.8F,8,6,1),PartPose.ZERO);
        hair.addOrReplaceChild("ends",CubeListBuilder.create().texOffs(80,8).addBox(-4,0,0,8,7,1).addBox(-4,6,0,2,4,1).addBox(-2,6,0,4,4.5F,1).addBox(2,6,0,2,3,1),PartPose.offset(0,.5F,3.8F));
        hair.addOrReplaceChild("left_lock",CubeListBuilder.create().texOffs(101,0).addBox(-1,0,0,2,10,1),PartPose.offset(-3.1F,-4,-4.2F));
        hair.addOrReplaceChild("right_lock",CubeListBuilder.create().texOffs(111,0).addBox(-1,0,0,2,9,1),PartPose.offset(3.1F,-3,-4.15F));
        var ponytail=head.addOrReplaceChild("ponytail",CubeListBuilder.create().texOffs(110,0).addBox(-2,-.25F,0,4,2,3),PartPose.offset(0,-3,4));
        ponytail.addOrReplaceChild("tail",CubeListBuilder.create().texOffs(80,0).addBox(-1.5F,0,0,3,7,3)
                .texOffs(96,0).addBox(-1,6,.3F,2,3,2),PartPose.offset(0,1,1));
        return LayerDefinition.create(mesh,128,64);
    }
    @Override public void setupAnim(LiteraryActor a,float walk,float amount,float age,float yaw,float pitch){
        float partial=age-a.tickCount;
        age=GhostsTakeClient.poseAge(a,age);
        for(var part:new ModelPart[]{head,hat,body,rightArm,leftArm,rightLeg,leftLeg})part.getAllParts().forEach(ModelPart::resetPose);
        currentRole=a.role();crouching=false;riding=false;super.setupAnim(a,walk,amount,age,yaw,pitch);hat.visible=false;jaw.visible=true;
        var motion=HumanoidMotion.sample(a,partial);float step=(float)Math.sin(motion.phase());
        var tw=transitions.get(a);
        // Getting up or sitting down carries the body a step between floor and seat: that is not a walk, so no stride plays over it.
        boolean seatMove=tw!=null&&(tw.phase!=a.phase()?seatChange(a.role(),tw.phase,a.phase())!=0:tw.seat!=0&&tw.from!=null);
        if(a.phase()==GhostsSet.STAND&&!seatMove){HumanoidMotion.gait(this,motion.phase(),motion.amount(),motion.running());walkLike(a.role(),motion.amount());}
        since=tw==null||tw.phase!=a.phase()?0:age-tw.started;
        // A new line over someone's head starts them talking, for about as long as the line takes to say.
        // While their words are on screen (the server's speaking timer), or a new line goes up over them, they are talking.
        speech=a.speaking()>0?1:0;if(speech==0&&tw!=null&&a.hasCustomName()&&a.isCustomNameVisible()){String line=a.getCustomName().getString();if(!line.equals(tw.line)){tw.line=line;tw.lineAt=age;}
            float length=Math.min(90,12+line.length()*1.6F),t=(age-tw.lineAt)/length;speech=t<0||t>1?0:1;}
        moving=motion.amount()>.15F&&!seatMove;hairMotion=net.minecraft.util.Mth.clamp(motion.amount(),0,1);
        pose(a.phase(),a.getScale(),step,age);
        blend(a,age);
    }
    private float[] snapshot(){var values=new float[joints.size()*6];int i=0;for(var p:joints){values[i++]=p.x;values[i++]=p.y;values[i++]=p.z;values[i++]=p.xRot;values[i++]=p.yRot;values[i++]=p.zRot;}return values;}
    private void blend(LiteraryActor actor,float age){
        var state=transitions.computeIfAbsent(actor,a->new Tween(a.phase()));
        // A newly visible apex must freeze in its complete leap, even if its earlier phase was last rendered off-camera.
        if(GhostsTakeClient.frozen()&&actor.phase()==GhostsSet.LEAP){state.phase=actor.phase();state.from=null;state.last=snapshot();state.faceLast=face.clone();return;}
        if(state.phase!=actor.phase()){state.seat=seatChange(actor.role(),state.phase,actor.phase());state.seed=new java.util.Random(actor.getId()*31L+(long)age).nextFloat();state.from=state.last;state.faceFrom=state.faceLast;state.started=age;state.phase=actor.phase();}
        // A face changes over two seconds or more, not in a cut: feeling arrives and settles.
        if(state.faceFrom!=null){float t=net.minecraft.util.Mth.clamp((age-state.started)/48F,0,1);t=t*t*(3-2*t);for(int i=0;i<3;i++)face[i]=net.minecraft.util.Mth.lerp(t,state.faceFrom[i],face[i]);if(t==1)state.faceFrom=null;}
        state.faceLast=face.clone();
        if(state.from!=null){float raw=net.minecraft.util.Mth.clamp((age-state.started)/(state.seat!=0?seatTicks(state.seat):blendLength(actor.phase())),0,1);
            lerpFrom(state.from,raw,state.seat,state.seed);
            if(raw==1){state.from=null;state.seat=0;}
        }state.last=snapshot();
    }
    /** How far the legs are from standing to seated, raw of the way through sitting down (1) or getting up (-1). */
    private static float legsSeated(int seat,float raw){return seat>0?ease((raw-.55F)/.45F):1-ease(raw/.45F);}
    /**
     * Every joint from a snapshot toward the pose now set, raw of the way. 0.4.324: while sitting down or getting up the legs keep their
     * own time, so the feet stay on the floor until the seat has the weight and come back down to it before the push up, and the bend
     * goes on top (seatBend).
     */
    private void lerpFrom(float[] from,float raw,int seat,float seed){
        // On and off the floor the legs move with the rest of the body; only a chair holds them back.
        float t=ease(raw),legs=seat==0||Math.abs(seat)==2?t:seat>0?legsSeated(seat,raw):1-legsSeated(seat,raw);int i=0;
        for(var p:joints){float r=seat!=0&&(p==leftLeg||p==rightLeg||p==leftKnee||p==rightKnee)?legs:t;
            p.x=net.minecraft.util.Mth.lerp(t,from[i++],p.x);p.y=net.minecraft.util.Mth.lerp(t,from[i++],p.y);p.z=net.minecraft.util.Mth.lerp(t,from[i++],p.z);p.xRot=angle(from[i++],p.xRot,r);p.yRot=angle(from[i++],p.yRot,r);p.zRot=angle(from[i++],p.zRot,r);}
        if(seat!=0)seatBend(Integer.signum(seat),raw,seed,Math.abs(seat)==2);
    }
    /**
     * Each walks as themselves (scaling the shared gait): John heavier, leaning in, his arms swinging less; Sarah brisk, arms close;
     * Merry quick, her arms swinging wide; Marjorie wrong, her arms all but still and her head level, leaning back as if carried.
     */
    private void walkLike(int role,float amount){
        if(amount<.05F)return;float k=net.minecraft.util.Mth.clamp(amount*2,0,1);
        float arms,legs,lean;
        switch(role){
            case LiteraryActor.GHOST_JOHN->{arms=.65F;legs=1;lean=.08F;}
            case LiteraryActor.GHOST_SARAH->{arms=.75F;legs=.9F;lean=.03F;leftArm.zRot-=.06F*k;rightArm.zRot+=.06F*k;}
            case LiteraryActor.GHOST_MERRY->{arms=1.35F;legs=.85F;lean=.02F;}
            case LiteraryActor.GHOST_MARJORIE->{arms=.12F;legs=.7F;lean=-.06F;head.xRot*=.2F;head.yRot*=.2F;}
            default->{return;}
        }
        leftArm.xRot*=net.minecraft.util.Mth.lerp(k,1,arms);rightArm.xRot*=net.minecraft.util.Mth.lerp(k,1,arms);
        leftLeg.xRot*=net.minecraft.util.Mth.lerp(k,1,legs);rightLeg.xRot*=net.minecraft.util.Mth.lerp(k,1,legs);
        body.xRot+=lean*k;head.xRot-=lean*k*.5F;
    }
    /**
     * 0.4.324: the bend through sitting down or getting up, at its deepest partway and gone at either end. The seat here is as high as
     * a standing hip, so the body perches: while the feet are down the knees give and the hips lower just as far as keeps them on the
     * floor, with one short step as the body comes round to the seat. Sitting down: weight forward over the feet, the hands going out
     * for balance, then the seat takes the weight and the legs come up. Getting up: the feet come down first, the lean comes early and
     * hardest with the hands pressed down for a push, then the hips rise under it and the back straightens last. The head stays level.
     */
    /**
     * 0.4.324 (owner: "ensure subtle variances between standing animations"): how each of them gets up and sits down. lean: how far
     * forward; late: how late the press comes; out: how wide the hands go; world: where the hands press, as seen in the room; lag: how
     * far the second hand trails the first; oneHand: how little the second hand does (0 both alike, 1 not at all); drop: how heavily
     * they come down onto a seat, braking less.
     */
    record Style(float lean,float late,float out,float world,float lag,float oneHand,float drop){}
    static Style style(int role){return switch(role){
        // John: heavy, both hands flat on his thighs and a hard push; he drops onto a seat more than he lowers onto it.
        case LiteraryActor.GHOST_JOHN->new Style(1.2F,0,.14F,-.22F,.04F,0,.6F);
        // Sarah: upright, one hand on the table's edge or her knee doing the work, the other barely leaving her lap.
        case LiteraryActor.GHOST_SARAH->new Style(.82F,-.03F,.1F,-.14F,.1F,.65F,0);
        // Merry: small; her hands go either side onto the seat's edge and she slides off it or climbs back onto it.
        case LiteraryActor.GHOST_MERRY->new Style(.5F,.02F,.34F,.3F,0,0,0);
        // Marjorie: slow, hands on her knees, the press late and long.
        case LiteraryActor.GHOST_MARJORIE->new Style(.8F,.08F,.08F,-.38F,.06F,0,.2F);
        // Barry: pushes off the arms of his director's chair, hands wide.
        case LiteraryActor.GHOST_PRODUCER->new Style(.9F,-.02F,.42F,.05F,.03F,0,.3F);
        default->new Style(1,0,.14F,-.18F,.05F,0,0);
    };}
    private static float frac(float x){return x-(float)Math.floor(x);}
    /**
     * 0.4.324: the bend through sitting down or getting up, at its deepest partway and gone at either end. The seat here is as high as
     * a standing hip, so the body perches: while the feet are down the knees give and the hips lower just as far as keeps them on the
     * floor, with one short step as the body comes round to the seat. Sitting down: weight forward over the feet, then the seat takes
     * the weight, the hands go back to it and the legs come up. Getting up: the feet come down first, the lean comes early and hardest,
     * the hands press down off the thighs, then the hips rise under it and the back straightens last. The head stays level. Each person
     * has their own way of doing it (style), and each time differs a little (seed): how far they lean, which hand leads, the head's turn.
     */
    void seatBend(int dir,float raw,float seed,boolean floor){
        var st=style(currentRole);float r1=seed,r2=frac(seed*7.13F+.31F),r3=frac(seed*13.7F+.57F);
        float k=(float)Math.sin(Math.PI*Math.pow(raw,(dir>0?1.2:.6)*(.9+.2*r2)));if(k<=0)return;
        if(!floor){
            float down=1-legsSeated(dir,raw),give=.6F*k*down,dip=12*(1-net.minecraft.util.Mth.cos(give));
            for(var part:new ModelPart[]{head,body,leftArm,rightArm,leftLeg,rightLeg})part.y+=dip;
            float step=.32F*net.minecraft.util.Mth.sin((float)Math.PI*raw)*down*(r2<.5F?1:-1);
            leftLeg.xRot-=give-step;rightLeg.xRot-=give+step;leftKnee.xRot+=2*give;rightKnee.xRot+=2*give;
        }
        // Up off a floor the body has to come right forward over its feet before it can stand.
        float lean=(floor?(dir>0?.45F:.85F):(dir>0?.5F:.62F))*k*st.lean()*(.9F+.2F*r1);
        // The arms do work (owner, 7 November: "use their arms as they get up too"). Getting up: the hands come off the table onto the
        // thighs, elbows bent, then press down, the elbows straightening as the hips lift; they let go once the body is over its feet.
        // Sitting down: as the seat takes the weight the hands go back to it either side, braking. Angles are as seen in the room, so
        // the lean added afterwards is taken off here. One hand leads; the other follows a little behind, or hardly at all.
        boolean leftLeads=r2<.5F;
        for(int side=0;side<2;side++){
            boolean lead=(side==0)==leftLeads;float late=lead?0:st.lag()+.04F*r3,share=lead?1:1-st.oneHand(),at=raw-late,arms,world,bend;
            if(floor&&dir<0){float press=ease((at-.05F)/.35F);arms=ease(at/.12F)*(1-ease((raw-.45F)/.3F));world=net.minecraft.util.Mth.lerp(press,.38F,.12F);bend=net.minecraft.util.Mth.lerp(press,-.7F,-.05F);}
            else if(floor){arms=ease((at-.2F)/.3F)*(1-ease((raw-.75F)/.25F));world=.32F;bend=-.3F;}
            else if(dir<0){float press=ease((at-.12F-st.late())/.3F);arms=ease(at/.18F)*(1-ease((raw-.5F-st.late())/.3F));world=net.minecraft.util.Mth.lerp(press,-.55F,st.world());bend=net.minecraft.util.Mth.lerp(press,-.9F,-.1F);}
            else{arms=ease((at-.3F)/.25F)*(1-ease((raw-.8F)/.2F))*(1-st.drop()*.5F);world=.22F+Math.max(0,st.world())*.3F;bend=-.45F;}
            arms*=share;if(arms<=0)continue;
            var arm=side==0?leftArm:rightArm;var elbow=side==0?leftElbow:rightElbow;float out=(side==0?-1:1)*(st.out()+(dir>0?.1F:0)+.04F*(r1-.5F));
            arm.xRot=net.minecraft.util.Mth.lerp(arms,arm.xRot,world-lean);arm.yRot=net.minecraft.util.Mth.lerp(arms,arm.yRot,0);arm.zRot=net.minecraft.util.Mth.lerp(arms,arm.zRot,out);
            elbow.xRot=net.minecraft.util.Mth.lerp(arms,elbow.xRot,bend);
        }
        HumanoidMotion.lean(this,lean);head.xRot-=lean*.75F;head.yRot+=(r3-.5F)*.36F*k;
        hat.copyFrom(head);
    }
    /** A flinch or a landing is fast; sitting down or settling into grace takes the body's weight. */
    private static float blendLength(int phase){return switch(phase){case GhostsSet.LEAP->5F;case GhostsSet.FLINCH,GhostsSet.IMPACT,GhostsSet.FALL->3F;case GhostsSet.SIT,GhostsSet.SLUMP,GhostsSet.PRAY,GhostsSet.WARY,GhostsSet.DEAD->14F;case GhostsSet.FLOOR->20F;default->8F;};}
    private boolean moving;
    private static float angle(float from,float to,float t){return from+net.minecraft.util.Mth.wrapDegrees((to-from)*net.minecraft.util.Mth.RAD_TO_DEG)*net.minecraft.util.Mth.DEG_TO_RAD*t;}
    /** For the native proofs: render as if speaking, and as if on a given filming day (0 keeps the client's own day). */
    public static boolean previewSpeaking;public static int previewDay;
    private static int day(){return previewDay>0?previewDay:GhostsTakeClient.day();}
    /** 0.4.324, for the filmstrip: t (0 to 1) of the way through sitting down or getting up, blended and bent as the scene does it. */
    public void previewSeat(int role,int from,int to,float t,float age,float seed){
        preview(role,from,age);var start=snapshot();preview(role,to,age);lerpFrom(start,t,seatChange(role,from,to),seed);
    }
    public void preview(int role,int phase,float age){since=age;speech=previewSpeaking?1:0;moving=false;hairMotion=phase<0?1:0;for(var part:new ModelPart[]{head,hat,body,rightArm,leftArm,rightLeg,leftLeg})part.getAllParts().forEach(ModelPart::resetPose);currentRole=role;young=false;crouching=false;riding=false;hat.visible=false;jaw.visible=true;if(phase<0){moving=true;phase=GhostsSet.STAND;HumanoidMotion.gait(this,age*.55F,1,false);walkLike(role,1);}pose(phase,role==LiteraryActor.GHOST_MERRY?.58F:role==LiteraryActor.GHOST_MARJORIE?.86F:1,(float)Math.sin(age*.08F),age);}
    @Override public void renderToBuffer(PoseStack p,VertexConsumer out,int light,int overlay,int colour){
        var fitted=GhostsSkinMesh.fit(out,currentRole,currentPhase);
        super.renderToBuffer(p,fitted,light,overlay,colour);
        GhostsSkinMesh.head(head,jaw,blink,face,p,fitted,light,overlay,colour);
        if(currentRole==LiteraryActor.GHOST_JOHN&&(currentPhase==GhostsSet.FLINCH||currentPhase==GhostsSet.HAND))GhostsSkinMesh.coverLeftHandMark(leftArm,leftElbow,p,fitted,light,overlay,colour);
        // 0.4.328: hurt on the second day, at supper, and bandaged from the third.
        // The cut from the moment he makes it on day 2 (his hand pose, or the take past it once he has put the hand down), the bandage on days 3 to 7.
        if(currentRole==LiteraryActor.GHOST_JOHN){int d=currentDay;if(d>=3||d==2&&(currentPhase==GhostsSet.HAND||currentHurt))GhostsSkinMesh.rightHand(rightArm,rightElbow,p,fitted,light,overlay,colour,d>=3);}
    }
    private void seatLegs(){
        for(var part:new ModelPart[]{head,hat,body,leftArm,rightArm,leftLeg,rightLeg})part.y+=12;
        leftLeg.xRot=rightLeg.xRot=-(float)Math.PI/2;leftKnee.xRot=rightKnee.xRot=(float)Math.PI/2;
    }
    private void pose(int phase,float scale,float step,float age){
        currentPhase=phase;currentDay=day();currentHurt=previewDay<=0&&GhostsTakeClient.johnHurt();boolean stillOne=stillAtDinner(phase);float cycle=(age+currentRole*13)%(stillOne?311:93);
        blink=phase==GhostsSet.DEAD||phase==GhostsSet.PRAY?1:phase!=GhostsSet.LEAP&&phase!=GhostsSet.SLIP&&cycle<6?(float)Math.sin(Math.PI*cycle/6):0;lids.visible=blink>0;
        expression(phase,age);
        switch(phase){
            case GhostsSet.SIT,GhostsSet.SLUMP,GhostsSet.VOMIT,GhostsSet.HAND,GhostsSet.EATING,GhostsSet.CHOKING,GhostsSet.DEAD->{
                seatLegs();leftArm.xRot=-.7F;rightArm.xRot=-.8F;leftElbow.xRot=-.65F;rightElbow.xRot=-.6F;
                // Each sits at the table as themselves: John on his forearms, Sarah with her hands together at the edge, Merry with
                // hers in her lap, Marjorie with both hands flat on the table, exactly alike.
                if(phase==GhostsSet.SIT)switch(currentRole){
                    // John sits back: his left hand on his thigh, his right forearm along the table.
                    case LiteraryActor.GHOST_JOHN->{body.xRot=-.08F;head.xRot=-.04F;leftArm.xRot=-.12F;leftArm.zRot=-.08F;leftElbow.xRot=-.55F;rightArm.xRot=-.5F;rightArm.zRot=.1F;rightElbow.xRot=-1.05F;}
                    // Sarah leans in a little, her hands drawn together toward the middle of the table.
                    case LiteraryActor.GHOST_SARAH->{body.xRot=.12F;head.xRot=.06F;leftArm.xRot=rightArm.xRot=-.78F;leftArm.yRot=.5F;rightArm.yRot=-.5F;leftElbow.xRot=rightElbow.xRot=-.85F;}
                    // Merry, hands in her lap, shoulders drawn up, chin down.
                    case LiteraryActor.GHOST_MERRY->{body.xRot=.12F;head.xRot=.22F;leftArm.xRot=rightArm.xRot=-.06F;leftArm.zRot=.16F;rightArm.zRot=-.16F;leftArm.y=rightArm.y=1.2F;leftElbow.xRot=rightElbow.xRot=-1.05F;}
                    // Marjorie bolt upright, both arms straight out, flat on the table, exactly alike.
                    case LiteraryActor.GHOST_MARJORIE->{body.xRot=0;head.xRot=0;leftArm.xRot=rightArm.xRot=-1.05F;leftArm.zRot=rightArm.zRot=0;leftArm.yRot=rightArm.yRot=0;leftElbow.xRot=rightElbow.xRot=0;}
                    default->{}
                }
                if(phase==GhostsSet.HAND){rightArm.xRot=-1.25F;rightElbow.xRot=-1.4F;head.xRot=.35F;head.yRot=-.3F;}
                if(phase==GhostsSet.SLUMP){body.xRot=.52F;head.xRot=.85F;head.zRot=.17F;leftArm.xRot=rightArm.xRot=-.1F;leftElbow.xRot=rightElbow.xRot=.2F;}
                if(phase==GhostsSet.EATING){float chew=(float)Math.sin(age*.23F);head.xRot=.12F+chew*.025F;head.yRot=.2F;rightArm.xRot=-1.04F;rightElbow.xRot=-1.65F+chew*.08F;jaw.xRot=.04F+Math.max(0,chew)*.16F;}
                if(phase==GhostsSet.CHOKING){float rate=currentRole==LiteraryActor.GHOST_JOHN?.33F:currentRole==LiteraryActor.GHOST_SARAH?.75F:.48F;float catchBreath=(float)Math.sin(age*rate+currentRole);body.xRot=.3F+catchBreath*.065F;body.zRot=(float)Math.sin(age*rate*.69F)*.045F;head.xRot=.27F-catchBreath*.08F;head.zRot=(float)Math.sin(age*rate*.78F)*.06F;rightArm.xRot=-1.18F;rightElbow.xRot=-1.95F;leftArm.xRot=-.5F;leftElbow.xRot=-.95F;jaw.xRot=.28F+Math.max(0,catchBreath)*.07F;if(currentRole==LiteraryActor.GHOST_JOHN){body.xRot=-.12F+catchBreath*.12F;head.xRot=-.45F;leftArm.xRot=-1.1F;leftElbow.xRot=-1.8F;}if(currentRole==LiteraryActor.GHOST_SARAH){body.zRot=.23F+catchBreath*.09F;leftArm.xRot=-1.2F;rightArm.xRot=-.4F;}}
                if(phase==GhostsSet.DEAD){body.xRot=.66F;body.zRot=.12F;head.xRot=.93F;head.zRot=.24F;leftArm.xRot=rightArm.xRot=-.12F;leftArm.zRot=rightArm.zRot=0;leftElbow.xRot=rightElbow.xRot=.13F;leftKnee.xRot=1.36F;rightKnee.xRot=1.48F;jaw.xRot=.32F;}
                if(phase==GhostsSet.DEAD&&currentRole==LiteraryActor.GHOST_JOHN){body.xRot=-.3F;body.zRot=-.12F;head.xRot=-.55F;leftLeg.yRot=.18F;rightArm.zRot=.32F;}
                if(phase==GhostsSet.DEAD&&currentRole==LiteraryActor.GHOST_SARAH){body.xRot=.24F;body.zRot=.38F;head.xRot=.65F;head.zRot=.45F;leftArm.zRot=-.2F;rightArm.xRot=-.65F;}
                // The dinner is an unnaturally still stare and a slowly opening mouth, without a retching body loop.
                if(phase==GhostsSet.VOMIT){head.yRot=.55F;head.xRot=.03F;jaw.xRot=.25F+(float)Math.sin(age*.035F)*.025F;}
            }
            // 0.4.325: Merry on the basement floor after the supper: knees drawn up, her arms round them, looking over them at the cross.
            case GhostsSet.FLOOR->{for(var part:new ModelPart[]{head,hat,body,leftArm,rightArm,leftLeg,rightLeg})part.y+=11;
                leftLeg.xRot=rightLeg.xRot=-2.35F;leftKnee.xRot=rightKnee.xRot=2.3F;leftLeg.yRot=.14F;rightLeg.yRot=-.14F;
                body.xRot=.42F;head.xRot=-.5F;leftArm.xRot=rightArm.xRot=-.95F;leftArm.yRot=.55F;rightArm.yRot=-.55F;leftArm.zRot=-.12F;rightArm.zRot=.12F;leftElbow.xRot=rightElbow.xRot=-1.05F;}
            case GhostsSet.CROUCH->{body.xRot=.3F;head.xRot=.2F;leftLeg.xRot=rightLeg.xRot=-.5F;leftKnee.xRot=rightKnee.xRot=.8F;leftElbow.xRot=rightElbow.xRot=-.9F;}
            case GhostsSet.CRAWL->{
                body.y=17.8F;body.z=1;body.xRot=1.43F;head.y=21;head.z=-2.8F;head.xRot=-.1F;
                leftArm.y=rightArm.y=18.1F;leftArm.z=rightArm.z=3;leftArm.xRot=-.35F+step*.2F;rightArm.xRot=-.35F-step*.2F;
                leftElbow.xRot=-1.15F-step*.15F;rightElbow.xRot=-1.15F+step*.15F;
                leftLeg.y=rightLeg.y=19.5F;leftLeg.z=rightLeg.z=12.88F;leftLeg.xRot=-1.2F-step*.22F;rightLeg.xRot=-1.2F+step*.22F;leftKnee.xRot=2.77F+step*.22F;rightKnee.xRot=2.77F-step*.22F;
                head.yRot+=(float)Math.sin(age*.035F)*.07F;
            }
            case GhostsSet.WALL->{leftArm.xRot=-2.9F;rightArm.xRot=-2.65F;leftArm.zRot=-.3F;rightArm.zRot=.25F;leftElbow.xRot=-.55F;rightElbow.xRot=-.8F;leftLeg.xRot=-.8F;rightLeg.xRot=-.55F;leftKnee.xRot=1.2F;rightKnee.xRot=.95F;head.xRot=.3F;}
            case GhostsSet.BED->{leftElbow.xRot=rightElbow.xRot=-.3F;leftArm.zRot=-.3F;rightArm.zRot=.3F;leftKnee.xRot=.2F;rightKnee.xRot=.08F;head.yRot=.12F;}
            case GhostsSet.FLINCH->{body.zRot=.15F;head.xRot=-.2F;rightArm.xRot=-1.25F;rightElbow.xRot=-1.4F;leftArm.xRot=-.7F;leftElbow.xRot=-1.1F;rightKnee.xRot=.35F;}
            case GhostsSet.LEAP->{body.xRot=-.12F;leftArm.xRot=-1.05F;rightArm.xRot=-.85F;leftArm.zRot=-.05F;rightArm.zRot=.05F;leftElbow.xRot=-.45F;rightElbow.xRot=-.65F;leftLeg.xRot=-.55F;rightLeg.xRot=.2F;leftKnee.xRot=.95F;rightKnee.xRot=.75F;}
            // 0.4.242: grace is said holding hands. Sarah stands at Merry's right and reaches down for her left hand; John stands at
            // Marjorie's right and holds out his left, which she never takes. Heads bowed, eyes shut.
            case GhostsSet.PRAY->{
                if(currentRole==LiteraryActor.GHOST_MERRY){seatLegs();head.xRot=.3F;leftArm.xRot=-.5F;leftArm.zRot=-.95F;leftElbow.xRot=-.25F;rightArm.xRot=-.7F;rightElbow.xRot=-.65F;}
                else if(currentRole==LiteraryActor.GHOST_SARAH){head.xRot=.4F;head.yRot=-.25F;rightArm.xRot=-.3F;rightArm.zRot=.12F;rightElbow.xRot=-.3F;leftArm.xRot=-.1F;leftElbow.xRot=-.25F;}
                else if(currentRole==LiteraryActor.GHOST_JOHN){head.xRot=.3F;head.yRot=.3F;leftArm.xRot=-.55F;leftArm.zRot=-.35F;leftElbow.xRot=-.2F;rightArm.xRot=-.1F;rightElbow.xRot=-.25F;}
                else{head.xRot=.35F;leftArm.xRot=rightArm.xRot=-.65F;leftArm.yRot=.67F;rightArm.yRot=-.67F;leftElbow.xRot=rightElbow.xRot=-.8F;}
            }
            // Merry holding back from the grace: forearms drawn up to her chest, hunched, watching her sister.
            case GhostsSet.WARY->{seatLegs();body.xRot=.1F;head.xRot=.12F;leftArm.xRot=rightArm.xRot=-.55F;leftArm.zRot=.18F;rightArm.zRot=-.18F;leftElbow.xRot=rightElbow.xRot=-1.75F;}
            // Marjorie sliding off her chair, boneless: thighs tipping down, leaning back, arms trailing, head on one side.
            case GhostsSet.SLIP->{seatLegs();leftLeg.xRot=rightLeg.xRot=-1.15F;leftKnee.xRot=rightKnee.xRot=1.05F;body.xRot=-.3F;head.xRot=.3F;head.zRot=.28F;leftArm.xRot=rightArm.xRot=.15F;leftArm.zRot=-.18F;rightArm.zRot=.18F;leftElbow.xRot=rightElbow.xRot=-.1F;}
            case GhostsSet.HELP->{head.xRot=-.35F;body.xRot=-.14F;leftArm.xRot=-2.25F;rightArm.xRot=-2.4F;leftElbow.xRot=-.4F;rightElbow.xRot=-.45F;}
            case GhostsSet.FALL->{body.xRot=.16F;head.xRot=-.2F;leftArm.zRot=-.8F;rightArm.zRot=.8F;leftLeg.xRot=-.18F;rightLeg.xRot=.15F;leftKnee.xRot=.6F;rightKnee.xRot=.5F;}
            case GhostsSet.IMPACT->{body.xRot=.45F;head.xRot=.2F;leftLeg.xRot=rightLeg.xRot=-.7F;leftKnee.xRot=rightKnee.xRot=1.4F;leftArm.xRot=-.4F;rightArm.xRot=-.5F;}
            default->{leftElbow.xRot=-.12F;rightElbow.xRot=-.12F;}
        }
        living(phase,age,stillOne);
        hair.visible=currentRole==LiteraryActor.GHOST_MARJORIE;
        if(hair.visible){
            hairEnds.xRot=.04F+(float)Math.sin(age*.035F)*.025F;hairLeft.xRot=-.035F;hairRight.xRot=.04F;
            if(phase==GhostsSet.CRAWL){hairEnds.xRot=1.4F;hairLeft.xRot=1.05F;hairRight.xRot=1.05F;}
            else if(phase==GhostsSet.BED){hairEnds.xRot=-.75F;hairLeft.zRot=-.18F;hairRight.zRot=.18F;}
        }
        ponytail.visible=currentRole==LiteraryActor.GHOST_MERRY;
        if(ponytail.visible){
            ponyEnds.xRot=.12F+hairMotion*.18F;ponyEnds.zRot=step*hairMotion*.07F+(float)Math.sin(age*.035F)*.015F;
            if(phase==GhostsSet.FLINCH)ponyEnds.xRot+=.16F;
            else if(phase==GhostsSet.BED)ponyEnds.xRot=-.7F;
        }
        if(phase!=GhostsSet.CRAWL)HumanoidMotion.connect(this);hat.copyFrom(head);
    }
    /** Marjorie from the second day, seated at dinner or staring: the one body in the room that does not breathe, drift or look about. */
    private boolean stillAtDinner(int phase){return currentRole==LiteraryActor.GHOST_MARJORIE&&day()>=2&&(phase==GhostsSet.SIT||phase==GhostsSet.VOMIT);}
    /**
     * 0.4.309: the living layer over every living pose. Breath at each person's own rate (the chest and shoulders, the head with them),
     * the head drifting and now and then glancing aside; the jaw and a nod while a line is spoken, and an adult's hand with it; the fall's
     * flailing, the landing's settle, the wall's grip shifts, the flinch's tremble. The dead do none of it, and neither does Marjorie at
     * dinner, which is the point.
     */
    private void living(int phase,float age,boolean still){
        if(phase==GhostsSet.DEAD||still)return;
        int r=currentRole;float rate=r==LiteraryActor.GHOST_MERRY?.11F:r==LiteraryActor.GHOST_JOHN?.065F:r==LiteraryActor.GHOST_SARAH?.08F:.075F;
        boolean strained=phase==GhostsSet.CROUCH||phase==GhostsSet.WALL||phase==GhostsSet.HELP||phase==GhostsSet.FLINCH||phase==GhostsSet.CHOKING||phase==GhostsSet.WARY;
        if(strained)rate*=1.7F;
        float breath=(float)Math.sin(age*rate+r*1.7F);
        if(phase!=GhostsSet.CHOKING&&phase!=GhostsSet.LEAP&&phase!=GhostsSet.FALL){
            body.xRot-=breath*.018F;
            for(var arm:new ModelPart[]{leftArm,rightArm})arm.y-=breath*.22F;
            head.y-=breath*.18F;
        }
        // Day one at the table: John and Sarah now and then drink or take a bite, each on their own slow cycle, never in step, never mid-line.
        if(phase==GhostsSet.SIT&&day()==1&&speech==0&&(r==LiteraryActor.GHOST_JOHN||r==LiteraryActor.GHOST_SARAH)){
            float period=r==LiteraryActor.GHOST_JOHN?520:610,t=(age+(r==LiteraryActor.GHOST_JOHN?90:340))%period;
            if(t<80){
                float up=t<20?ease(t/20F):t<50?1:1-ease((t-50)/30F);
                rightArm.xRot=net.minecraft.util.Mth.lerp(up,rightArm.xRot,-1.8F);rightArm.yRot=net.minecraft.util.Mth.lerp(up,rightArm.yRot,-.42F);rightArm.zRot=net.minecraft.util.Mth.lerp(up,rightArm.zRot,0);
                rightElbow.xRot=net.minecraft.util.Mth.lerp(up,rightElbow.xRot,-1.6F);
                if(t>=22&&t<44)head.xRot-=.12F*bump(t-22,22);
                if(t>=46)jaw.xRot=Math.max(jaw.xRot,.04F+Math.max(0,(float)Math.sin(age*.6F))*.12F*(1-(t-46)/34F));
                return;
            }
        }
        // The head drifts and glances, unless the pose has the head doing something.
        boolean headBusy=phase==GhostsSet.HAND||phase==GhostsSet.VOMIT||phase==GhostsSet.PRAY||phase==GhostsSet.EATING||phase==GhostsSet.CHOKING||phase==GhostsSet.SLUMP||phase==GhostsSet.LEAP;
        if(!headBusy&&!moving){
            head.yRot+=(float)Math.sin(age*.013F+r*2.3F)*.1F+.32F*bump((age+r*71)%260F,30);
            head.xRot+=(float)Math.sin(age*.019F+r)*.035F;
        }
        // Speaking: the jaw in syllables, a small nod, and an adult's hand.
        if(speech>0){
            // Big enough to see across a table which of them is talking: the jaw drops on each syllable, with pauses between phrases.
            float phrase=(float)Math.sin(age*.19F+r)>-.45F?1:.15F;
            float syllable=Math.max(0,(float)Math.sin(age*1.05F))*(.55F+.45F*(float)Math.sin(age*.31F+r))*phrase;
            jaw.xRot=Math.max(jaw.xRot,.08F+syllable*.42F);
            head.xRot+=(float)Math.sin(age*.42F)*.05F;head.yRot+=(float)Math.sin(age*.27F+r)*.04F;
            boolean adult=r==LiteraryActor.GHOST_JOHN||r==LiteraryActor.GHOST_SARAH||r==LiteraryActor.GHOST_PRODUCER||r==LiteraryActor.GHOST_PRIEST;
            if(adult&&(phase==GhostsSet.STAND||phase==GhostsSet.SIT)&&!moving){float g=.5F+.5F*(float)Math.sin(age*.14F+r);rightArm.xRot-=.35F*g;rightElbow.xRot-=.5F*g;rightArm.yRot-=.15F*g;}
        }
        switch(phase){
            case GhostsSet.FALL->{float f=age*.85F;leftArm.zRot+=(float)Math.sin(f)*.45F;rightArm.zRot+=(float)Math.sin(f+1.9F)*.45F;leftArm.xRot+=(float)Math.sin(f*.7F)*.4F;rightArm.xRot+=(float)Math.sin(f*.7F+2)*.4F;leftLeg.xRot+=(float)Math.sin(f*.9F)*.25F;rightLeg.xRot+=(float)Math.sin(f*.9F+1.5F)*.25F;}
            case GhostsSet.IMPACT->{float k=(float)Math.exp(-since/7F);body.xRot+=.35F*k;head.xRot+=.3F*k;leftKnee.xRot+=.4F*k;rightKnee.xRot+=.4F*k;}
            // On the wall she climbs hand over hand: one arm reaches high while the other pulls down, the opposite knee drives up, the head
            // follows the reaching hand. Slow and deliberate, with a tremor in the hold.
            case GhostsSet.WALL->{float g=(float)Math.sin(age*.13F+r);leftArm.xRot+=g*.42F;rightArm.xRot-=g*.42F;leftElbow.xRot-=Math.max(0,-g)*.6F;rightElbow.xRot-=Math.max(0,g)*.6F;
                leftLeg.xRot-=Math.max(0,-g)*.45F;rightLeg.xRot-=Math.max(0,g)*.45F;leftKnee.xRot+=Math.max(0,-g)*.55F;rightKnee.xRot+=Math.max(0,g)*.55F;head.yRot+=g*.18F;head.xRot-=.15F*Math.abs(g);
                float shake=(float)Math.sin(age*1.7F)*.015F;leftArm.zRot+=shake;rightArm.zRot-=shake;}
            // A flinch: a jolt back with the arms coming up, then a tremble that eases off.
            case GhostsSet.FLINCH->{float jolt=(float)Math.exp(-since/5F);body.xRot-=.28F*jolt;head.xRot-=.25F*jolt;rightArm.xRot-=.45F*jolt;leftArm.xRot-=.35F*jolt;
                float tremble=(float)Math.sin(age*2.1F)*.05F*(1-net.minecraft.util.Mth.clamp(since/60F,0,.7F));body.zRot+=tremble;rightArm.xRot+=tremble*2;leftArm.zRot-=tremble;}
            // Helping: both hands up and grabbing at nothing, reaching higher in turn.
            case GhostsSet.HELP->{float reach=(float)Math.sin(age*.35F);leftArm.xRot+=reach*.3F;rightArm.xRot-=reach*.3F;leftElbow.xRot+=Math.max(0,reach)*.3F;rightElbow.xRot+=Math.max(0,-reach)*.3F;body.xRot-=Math.abs(reach)*.06F;}
            // The slide off the chair: boneless and slow, the thighs tipping, the body sinking and leaning back, the head lolling.
            case GhostsSet.SLIP->{float t=ease(since/45F);body.xRot-=.3F*t;head.zRot+=.25F*t;head.xRot+=.15F*t;leftLeg.xRot+=.45F*t;rightLeg.xRot+=.42F*t;
                for(var part:new ModelPart[]{head,body,leftArm,rightArm,leftLeg,rightLeg})part.y+=3.5F*t;leftArm.xRot+=.3F*t;rightArm.xRot+=.25F*t;}
            // The crawl, wrong: the head cocked over, the body bobbing with each placed hand.
            case GhostsSet.CRAWL->{if(r==LiteraryActor.GHOST_MARJORIE){head.zRot+=.32F;head.xRot-=.12F;float bob=(float)Math.sin(age*.5F);body.y+=bob*.35F;head.y+=bob*.45F;}}
            // Standing between lines: the weight shifting from one leg to the other, a knee easing, the arms a little bent and swaying.
            case GhostsSet.STAND->{if(!moving){float w=(float)Math.sin(age*.025F+r*1.9F);body.zRot+=w*.035F;head.zRot-=w*.02F;
                (w>0?leftKnee:rightKnee).xRot+=Math.abs(w)*.18F;(w>0?leftLeg:rightLeg).xRot-=Math.abs(w)*.06F;
                float sway=(float)Math.sin(age*.04F+r);leftElbow.xRot-=.12F+sway*.04F;rightElbow.xRot-=.12F-sway*.04F;leftArm.zRot-=.03F+w*.02F;rightArm.zRot+=.03F-w*.02F;}}
            default->{}
        }
    }
    /**
     * 0.4.242 (owner: "Shifting facial expressions"): the brows lift, draw together or knit and the mouth's corners turn, by what a
     * person is doing, by how far into the filming the family is, and now and then on their own between lines.
     */
    private void expression(int phase,float age){
        float lift=0,tilt=0,mouth=0;int day=day();
        boolean marjorie=currentRole==LiteraryActor.GHOST_MARJORIE,family=currentRole==LiteraryActor.GHOST_JOHN||currentRole==LiteraryActor.GHOST_SARAH||currentRole==LiteraryActor.GHOST_MERRY;
        float worn=net.minecraft.util.Mth.clamp((day-1)/6F,0,1);
        if(family){tilt=.45F*worn;mouth=-.3F*worn;}
        if(marjorie&&day>=2){lift=-.2F;tilt=-.25F;}
        // The passing looks between lines hold for a few seconds once they come, and come seldom enough to read as reactions.
        lift+=.55F*hold((age+currentRole*37)%360F,110);mouth-=.45F*hold((age+currentRole*53)%430F,130);
        if(marjorie)mouth+=.9F*hold((age+11)%520F,150);
        switch(phase){
            case GhostsSet.PRAY->{lift=.2F;tilt=.55F;mouth=-.25F;}
            case GhostsSet.WARY->{lift=.7F;tilt=.85F;mouth=-.65F;}
            case GhostsSet.SLIP,GhostsSet.CRAWL->{lift=-.45F;tilt=-.7F;mouth=.55F;}
            case GhostsSet.VOMIT->{lift=-.25F;tilt=-.4F;mouth=0;}
            case GhostsSet.FLINCH->{lift=1;tilt=.6F;mouth=-.5F;}
            case GhostsSet.HAND->{lift=.3F;tilt=.95F;mouth=-.85F;}
            case GhostsSet.CHOKING->{lift=.85F;tilt=.9F;mouth=-.45F;}
            case GhostsSet.DEAD->{lift=-.35F;tilt=.1F;mouth=-.35F;}
            case GhostsSet.SLUMP->{lift=-.2F;tilt=.5F;mouth=-.6F;}
            case GhostsSet.HELP->{lift=1;tilt=1;mouth=-.75F;}
            case GhostsSet.WALL->{if(marjorie){lift=-.3F;tilt=-.8F;mouth=.5F;}}
            case GhostsSet.BED->{if(marjorie){lift=.8F;tilt=.95F;mouth=-.8F;}}
            case GhostsSet.LEAP,GhostsSet.FALL,GhostsSet.IMPACT->{if(marjorie){lift=-.4F;tilt=-.6F;mouth=.3F;}else{lift=.9F;tilt=.35F;mouth=-.3F;}}
            default->{}
        }
        face[0]=net.minecraft.util.Mth.clamp(lift,-1,1);face[1]=net.minecraft.util.Mth.clamp(tilt,-1,1);face[2]=net.minecraft.util.Mth.clamp(mouth,-1,1);
    }
    /** A held expression: eases in over a fifth of its length, holds, and eases out over the last quarter. */
    private static float hold(float x,float length){if(x<=0||x>=length)return 0;float in=length*.2F,out=length*.25F;return x<in?ease(x/in):x>length-out?ease((length-x)/out):1;}
    private static float ease(float t){t=net.minecraft.util.Mth.clamp(t,0,1);return t*t*(3-2*t);}
    private static float bump(float x,float length){float t=x/length;return t<=0||t>=1?0:(float)Math.sin(Math.PI*t);}
    float[] face(){return face.clone();}
    public static ResourceLocation skin(LiteraryActor a){return skin(a.role(),a.phase());}
    public static ResourceLocation skin(int role,int phase){String name=GhostsSet.PEOPLE[Math.max(0,Math.min(5,role-LiteraryActor.GHOST_MARJORIE))].toLowerCase(java.util.Locale.ROOT).replace(' ','_');if(role==LiteraryActor.GHOST_MARJORIE&&(phase==GhostsSet.VOMIT||phase==GhostsSet.EATING||phase==GhostsSet.CHOKING||phase==GhostsSet.DEAD))name+="_dinner";if(role==LiteraryActor.GHOST_JOHN&&(phase==GhostsSet.FLINCH||phase==GhostsSet.HAND))name+="_hand";return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/ghosts_"+name+".png");}
    public static final class Renderer extends MobRenderer<LiteraryActor,GhostsCastModel>{
        public Renderer(EntityRendererProvider.Context c){super(c,new GhostsCastModel(c.bakeLayer(LAYER)),.2F);addLayer(new GhostsMouthLayer(this));
            // 0.4.247: what someone is carrying between takes (a plate to the sink, a mug back to the table) is drawn in their hand.
            addLayer(new net.minecraft.client.renderer.entity.layers.ItemInHandLayer<>(this,c.getItemInHandRenderer()));}
        @Override public ResourceLocation getTextureLocation(LiteraryActor a){return skin(a);}
        /**
         * 0.4.324: where the body is drawn while someone sits down or gets up. The server moves it between the seat and the floor beside
         * it in one tick; from where it was last drawn, the body is carried there, and turned to its new facing, over the same time as
         * the model's bend (GhostsMotion.SIT_TICKS and RISE_TICKS), so it lowers onto the seat or rises off it instead of jumping.
         */
        private static final java.util.WeakHashMap<LiteraryActor,Glide> GLIDES=new java.util.WeakHashMap<>();
        private static final class Glide {boolean known;int phase;net.minecraft.world.phys.Vec3 last,anchor;float lastYaw,anchorYaw,start,length,seen=-1000,k;}
        /** How far through carrying the body from its old place it is, 0 when it is where the server has it (for the native proofs). */
        public static float carry(LiteraryActor a){var g=GLIDES.get(a);return g==null||g.anchor==null?0:g.k;}
        @Override protected void setupRotations(LiteraryActor a,PoseStack p,float age,float yaw,float partial,float scale){
            var g=GLIDES.get(a);float body=g!=null&&g.anchor!=null?yaw+net.minecraft.util.Mth.wrapDegrees(g.anchorYaw-yaw)*g.k:yaw;if(g!=null)g.lastYaw=body;
            super.setupRotations(a,p,age,body,partial,scale);if(a.phase()==GhostsSet.BED)LiteraryRenderers.bedPose(p,1);
        }
        @Override public void render(LiteraryActor a,float yaw,float partial,PoseStack p,MultiBufferSource out,int light){
            var g=GLIDES.computeIfAbsent(a,k->new Glide());float now=GhostsTakeClient.poseAge(a,a.tickCount+partial);var at=a.getPosition(partial);
            if(g.known&&g.phase!=a.phase()){int change=seatChange(a.role(),g.phase,a.phase());
                if(Math.abs(change)==1&&g.last!=null&&now-g.seen<3&&g.last.distanceToSqr(at)<9){g.anchor=g.last;g.anchorYaw=g.lastYaw;g.start=now;g.length=seatTicks(change);}}
            g.known=true;g.phase=a.phase();g.k=0;
            if(g.anchor!=null){float t=net.minecraft.util.Mth.clamp((now-g.start)/g.length,0,1);g.k=1-t*t*(3-2*t);if(t>=1)g.anchor=null;}
            var shift=g.anchor==null?net.minecraft.world.phys.Vec3.ZERO:g.anchor.subtract(at).scale(g.k);g.last=at.add(shift);g.seen=now;
            p.pushPose();p.translate(shift.x,shift.y,shift.z);try{super.render(a,yaw,partial,p,out,light);}finally{p.popPose();}
        }
    }
}
