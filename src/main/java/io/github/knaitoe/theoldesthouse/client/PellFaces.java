package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import io.github.knaitoe.theoldesthouse.labyrinth.PellPeople;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * 0.4.569. The owner: "npcs should have wired faces/mouths/blinks/emotes as in ghosts set, too", and then: "You can try to paint unique
 * expressions - a character who defaults to a grimace might have a harsher smile, etc".
 *
 * <p>Each of the Pell house's people has their own looks, painted on their skin's own face grid by tools/draw_pell_faces.py (their sheet,
 * pell_faces_<name>.png): for each look, the upper half of the face with the eyes open and shut, and the lower half with the mouth closed,
 * a little open and wide open. The {@link Layer} draws the upper half of one and the lower half of another over the face, so a look can
 * blink and speak:
 *
 * <ul>
 *   <li><b>Blinks.</b> Each blinks on a clock of their own, every four to eight seconds. Asleep, or Nessa under the lid, the eyes stay
 *       shut.</li>
 *   <li><b>Speaking.</b> While a line of theirs has just gone up the mouth opens and closes on its syllables, a little and wide, with
 *       pauses between the phrases, for about as long as it takes to say. Callum chews while he eats.</li>
 *   <li><b>Looks.</b> Each has a face they rest in and one for what they are doing, and now and then, for a few seconds, a passing look.</li>
 * </ul>
 *
 * Who they are: June rests in a small closed smile that does not reach her eyes, tender over Tilly's hair or holding her in the water,
 * set at the line at night and pressing her under, blank in the water after, lost with her hand in a reader's, grief on the stool by the
 * freezer; now and then the smile is simply not there, and with her hand wrapped it hurts her now and then. Tilly's face is open; now and
 * then she listens, or smiles to herself; her grin at the window at first light is a child's, a tooth out. Callum's rests drawn and turned
 * down, so his smile on the step at first light is a harsh one, up on one side only; he looks up now and then; his anger at the knife
 * bares his teeth; mimicking a reader's crouch his face is empty. Gail's smile is broad and easy, pressed flat now and then by concern,
 * earnest when she holds a page out. Nessa's does not change.
 */
final class PellFaces {
    private PellFaces(){}
    /** Each person's looks, in the order tools/draw_pell_faces.py paints them on their sheet: June, Tilly, Callum, Gail, Nessa. */
    static final String[][] LOOKS={
        {"warm","tender","set","slip","grief","lost","pain","sleep"},
        {"open","listening","bright","content","unsure","sleep"},
        {"drawn","up","fury","eating","blank","wan","calm"},
        {"pleasant","concern","earnest"},
        {"slack"}};
    private static final String[] NAMES={"june","tilly","callum","gail","nessa"};
    private static final ResourceLocation[] SHEETS=new ResourceLocation[NAMES.length];
    static{for(int i=0;i<NAMES.length;i++)SHEETS[i]=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/pell_faces_"+NAMES[i]+".png");}
    private static final class Seen{String line;float lineAt=-1e6F,said;boolean met;}
    private static final Map<LiteraryActor,Seen> SEEN=new WeakHashMap<>();

    /** Chooses the face of a Pell person about to be drawn, onto the model that draws them. */
    static void apply(LiteraryRenderers.CastModel m,LiteraryActor a,float age){
        int who=PellPeople.who(a);if(who<0||who>=LOOKS.length)return;
        var s=SEEN.computeIfAbsent(a,k->new Seen());int id=a.getId(),gesture=a.gesture();
        // A line goes up over them as it is said, and is put away when it has been read; one already up when they are first seen was
        // said before they were, and is not said again.
        String line=a.hasCustomName()&&a.isCustomNameVisible()?a.getCustomName().getString():null;
        if(line==null)s.line=null;
        else if(!line.equals(s.line)){s.line=line;if(s.met){s.lineAt=age;s.said=Math.min(90,12+line.length()*1.6F);}}
        s.met=true;
        boolean speaking=who!=PellPeople.NESSA&&(a.speaking()>0||age-s.lineAt>=0&&age-s.lineAt<s.said);
        int look=look(who,gesture,age,id,(a.marks()&PellPeople.WRAPPED)!=0);
        int mouth=0;
        if(speaking){float beat=beat(age,id);mouth=beat<.2F?0:beat<.62F?1:2;}
        if(gesture==PellPeople.EATING&&Mth.sin(age*.9F+id)>.2F)mouth=1;
        m.face(who,2*look+(blink(who,gesture,age,id)?1:0),3*look+mouth);
    }

    /** The look a person has now, by who they are and what they are doing, and the passing looks. */
    static int look(int who,int gesture,float age,int id,boolean wrapped){
        String name=switch(who){
            case PellPeople.JUNE->switch(gesture){
                case PellPeople.BRUSH,PellPeople.HOLD->"tender";
                case PellPeople.PAINT,PellPeople.PRESS,PellPeople.SHOWING,PellPeople.SWIM->"set";
                case PellPeople.WADE->"slip";
                case PellPeople.JUNE_LED->"lost";
                case PellPeople.BY_HER->"grief";
                case PellPeople.ASLEEP->"sleep";
                default->wrapped&&passing(age+id*29,380,30)?"pain":passing(age+id*41,620,70)?"slip":"warm";
            };
            case PellPeople.TILLY->switch(gesture){
                case PellPeople.BRUSHED->"content";
                case PellPeople.LED->"unsure";
                case PellPeople.WAVE->"bright";
                case PellPeople.ASLEEP->"sleep";
                default->passing(age+id*23,400,80)?"listening":passing(age+id*61,530,60)?"content":"open";
            };
            case PellPeople.CALLUM->switch(gesture){
                case PellPeople.CROUCH->"blank";
                case PellPeople.STRUGGLE,PellPeople.LUNGE->"fury";
                case PellPeople.EATING->"eating";
                case PellPeople.SITTING->"calm";
                case PellPeople.STEP->"wan";
                default->passing(age+id*47,460,60)?"up":"drawn";
            };
            case PellPeople.GAIL->gesture==PellPeople.SHOW?"earnest":passing(age+id*31,500,60)?"concern":"pleasant";
            default->"slack";
        };
        return index(who,name);
    }
    static int index(int who,String name){var looks=LOOKS[who];for(int i=0;i<looks.length;i++)if(looks[i].equals(name))return i;throw new IllegalArgumentException(NAMES[who]+" has no look "+name);}
    /** A passing look: {@code length} ticks of every {@code period}. */
    private static boolean passing(float t,float period,float length){return t%period<length;}

    /** Whether the eyes are shut: a blink on each one's own clock, every four to eight seconds, three ticks long; asleep, or Nessa, always. */
    static boolean blink(int who,int gesture,float age,int id){
        if(who==PellPeople.NESSA||gesture==PellPeople.ASLEEP||gesture==PellPeople.STILL)return true;
        float period=80+Math.floorMod(id*53,80);
        return (age+id*37)%period<3;
    }
    /** As the Ghosts set's jaw: how far the mouth is open, 0 to 1, on each syllable, with pauses between the phrases. */
    static float beat(float age,int id){
        float phrase=Mth.sin(age*.19F+id)>-.45F?1:.15F;
        return Math.max(0,Mth.sin(age*1.05F))*(.55F+.45F*Mth.sin(age*.31F+id))*phrase;
    }

    /** Draws the face the model was given (CastModel.face) over its head: the upper half of one face on the sheet, the lower of another. */
    static void draw(LiteraryRenderers.CastModel m,PoseStack p,MultiBufferSource out,int light,int overlay){
        int who=m.faceWho();if(who<0||!m.head.visible||m.alpha<.99F)return;
        var v=out.getBuffer(RenderType.entityCutoutNoCull(SHEETS[who]));float width=3*LOOKS[who].length*32F;
        p.pushPose();m.head.translateAndRotate(p);
        // Rows 0 to 9 of the face (the hair, brows and eyes) and rows 10 to 15 (the mouth and chin), a hair in front of it.
        half(p,v,light,overlay,-8,-3,m.faceUpper()*32/width,0,(m.faceUpper()+1)*32/width,20/64F);
        half(p,v,light,overlay,-3,0,m.faceLower()*32/width,52/64F,(m.faceLower()+1)*32/width,1);
        p.popPose();
    }
    private static void half(PoseStack p,VertexConsumer v,int light,int overlay,float top,float bottom,float u0,float v0,float u1,float v1){
        var pose=p.last();float z=-4.01F/16,x0=-4/16F,x1=4/16F;
        v.addVertex(pose,x0,bottom/16,z).setColor(-1).setUv(u0,v1).setOverlay(overlay).setLight(light).setNormal(pose,0,0,-1);
        v.addVertex(pose,x1,bottom/16,z).setColor(-1).setUv(u1,v1).setOverlay(overlay).setLight(light).setNormal(pose,0,0,-1);
        v.addVertex(pose,x1,top/16,z).setColor(-1).setUv(u1,v0).setOverlay(overlay).setLight(light).setNormal(pose,0,0,-1);
        v.addVertex(pose,x0,top/16,z).setColor(-1).setUv(u0,v0).setOverlay(overlay).setLight(light).setNormal(pose,0,0,-1);
    }

    /** The face over a Pell person, after their body. */
    static final class Layer extends RenderLayer<LiteraryActor,LiteraryRenderers.CastModel>{
        Layer(RenderLayerParent<LiteraryActor,LiteraryRenderers.CastModel> parent){super(parent);}
        @Override public void render(PoseStack p,MultiBufferSource out,int light,LiteraryActor a,float limb,float amount,float partial,float age,float yaw,float pitch){
            if(a.isInvisible())return;
            draw(getParentModel(),p,out,light,LivingEntityRenderer.getOverlayCoords(a,0));
        }
    }
}
