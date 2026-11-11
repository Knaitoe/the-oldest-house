package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.util.TriState;

/**
 * 0.4.429 (owner, of Borrasca's lines running off the screen: "text still needs to be centered, framed in center of player's vision and
 * clear itself of obstacles"). What an actor says is no longer its nameplate, one unwrapped line over its head that ran off the screen up
 * close and went behind whatever stood in between. It is a caption: wrapped, centred, on a dark frame below the middle of the view, in
 * front of everything. The nearest speaker's line sits lowest, and up to {@link #MOST} speak at once. A speaker outside the middle of the
 * view gets a mark on the side the voice comes from. Lines fade a little with distance and are not shown past {@link #HEARD} blocks. An
 * actor another reader's scene keeps private is never sent to this client, so its lines are not either.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class SpeechCaptions {
    /** How far a line carries, in blocks. */
    public static final double HEARD=24;
    /** At most this many speakers at once, each at most this many wrapped lines. */
    public static final int MOST=3,LINES=5;
    private SpeechCaptions(){}

    /** An actor's line is a caption now, not a nameplate. */
    @SubscribeEvent public static void nameTag(RenderNameTagEvent e){if(e.getEntity() instanceof LiteraryActor)e.setCanRender(TriState.FALSE);}

    /** Who is speaking where this client can hear it, nearest first. */
    public static List<LiteraryActor> speakers(Minecraft mc){
        if(mc.level==null||mc.player==null)return List.of();
        Vec3 eye=mc.gameRenderer.getMainCamera().getPosition();
        var list=new ArrayList<>(mc.level.getEntitiesOfClass(LiteraryActor.class,new AABB(eye,eye).inflate(HEARD),
            a->a.isAlive()&&!a.isInvisible()&&a.isCustomNameVisible()&&a.hasCustomName()&&a.getEyePosition().distanceToSqr(eye)<HEARD*HEARD));
        list.sort(Comparator.comparingDouble(a->a.getEyePosition().distanceToSqr(eye)));
        return list.size()>MOST?list.subList(0,MOST):list;
    }

    /** The caption's words, after the speaker's name when the scene gives one. */
    public static Component line(LiteraryActor a){
        var words=a.getCustomName()==null?Component.empty():a.getCustomName();String who=a.speaker();
        return who.isEmpty()?words:Component.literal(who+": ").withStyle(ChatFormatting.GRAY).append(words.copy().withStyle(ChatFormatting.WHITE));
    }
    /** Where the lowest caption's frame ends: below the crosshair, above the action bar and the hotbar. */
    public static int bottom(int height){return Math.max(height/2+44,height-84);}

    static void render(GuiGraphics g,DeltaTracker delta){render(g,delta,bottom(g.guiHeight()));}
    /**
     * 0.4.446: the same captions with the lowest frame ending at a given height, for a screen that covers the view while people
     * about the reader go on talking (the drugstore's cabinets, with Patrick and Kendra at the reader's shoulder).
     */
    static void render(GuiGraphics g,DeltaTracker delta,int lowest){
        var mc=Minecraft.getInstance();if(mc.options.hideGui)return;
        var list=speakers(mc);if(list.isEmpty())return;
        var font=mc.font;int w=g.guiWidth(),h=g.guiHeight(),wrap=Math.max(120,Math.min(w-64,300));
        var cam=mc.gameRenderer.getMainCamera();Vec3 eye=cam.getPosition(),look=new Vec3(cam.getLookVector()),left=new Vec3(cam.getLeftVector());
        int y=lowest;
        for(var a:list){
            List<FormattedCharSequence> lines=font.split(line(a),wrap);if(lines.size()>LINES)lines=lines.subList(0,LINES);
            int width=0;for(var line:lines)width=Math.max(width,font.width(line));
            Vec3 to=a.getEyePosition().subtract(eye);double d=to.length();to=d>1e-4?to.scale(1/d):look;
            float fade=(float)Mth.clamp(1-(d-6)/(HEARD-6)*.5,.5,1);
            int top=y-lines.size()*10-4,x0=w/2-width/2-4,x1=w/2+width/2+4;
            g.fill(x0,top,x1,y,(int)(fade*150)<<24);
            int colour=((int)(fade*255)<<24)|0xFFFFFF;float shake=BorrascaNativeArt.scream(a),age=a.tickCount+delta.getGameTimeDeltaPartialTick(false);
            for(int i=0;i<lines.size();i++){int x=w/2-font.width(lines.get(i))/2,y0=top+3+i*10;
                if(shake>0)shaking(g,lines.get(i),x,y0,colour,shake,age,i);else g.drawString(font,lines.get(i),x,y0,colour,false);}
            // A voice from outside the middle of the view is marked on its side.
            if(to.dot(look)<.8){boolean onLeft=to.dot(left)>0;String mark=onLeft?"◂":"▸";g.drawString(font,mark,onLeft?x0-3-font.width(mark):x1+3,top+3+(lines.size()-1)*5,colour,true);}
            y=top-3;
        }
    }

    /**
     * 0.4.439 (owner, of Dana at the machine: "text should be garbled and wobbly like marjorie's"). The line is drawn a letter at a time,
     * each bobbing and tipping on its own as Marjorie's voice is drawn in the Ghosts set (GhostsTakeClient.renderCaption), harder the
     * harder she screams; past that, the letters jump about, and some leave a dark red double behind them.
     */
    static void shaking(GuiGraphics g,FormattedCharSequence line,int x,int y,int colour,float shake,float age,int row){
        var font=Minecraft.getInstance().font;final float[] pen={x};
        line.accept((index,style,codepoint)->{
            var glyph=Component.literal(new String(Character.toChars(codepoint))).withStyle(style);var pose=g.pose();pose.pushPose();
            int seed=index*31+(int)(age/2)*17+row*7;
            float bob=Mth.sin(age*(.62F+.3F*shake)+index*.41F+row)*.75F*shake;
            float jx=shake>1?(Math.floorMod(seed*1103515245>>>16,5)-2)*.3F*(shake-1):0,jy=shake>1?(Math.floorMod(seed*214013>>>16,5)-2)*.3F*(shake-1):0;
            pose.translate(pen[0]+jx,y+bob+jy,0);pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(Mth.sin(age*.24F*(1+shake)+index*.37F)*3*shake));
            if(shake>.9F&&Math.floorMod(seed,5)==0)g.drawString(font,glyph,1,1,((colour>>>24)*3/4)<<24|0x8A1C16,false);
            g.drawString(font,glyph,0,0,colour,false);
            pose.popPose();pen[0]+=font.width(glyph);return true;});
    }

    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Layer{
        private Layer(){}
        @SubscribeEvent public static void register(RegisterGuiLayersEvent e){
            e.registerBelow(VanillaGuiLayers.CHAT,ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"speech_captions"),SpeechCaptions::render);
        }
    }
}
