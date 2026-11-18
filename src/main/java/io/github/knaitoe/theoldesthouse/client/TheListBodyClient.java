package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.TheListBody;
import io.github.knaitoe.theoldesthouse.labyrinth.TheListSack;
import io.github.knaitoe.theoldesthouse.network.TheListBodyPayload;
import javax.annotation.Nullable;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * 0.4.705, The list, part 2 (docs/THE_LIST_0_4_705.md): the slasher's body on its own reader's client.
 * <ul>
 * <li><b>The sack, from inside</b> ({@link #sack}): burlap close round the view and two crooked eyeholes cut in it, drawn whenever the
 * local reader wears the sack in first person. Like the blindfold, what the sack hides is not cosmetic and does not follow the comfort
 * settings.</li>
 * <li><b>The pull</b> ({@link #overlay}): a dark weight at the edges of the screen, heaviest on the side the nearest name is on, beating with a
 * heart. It is a feeling, so it follows Distortion Effects, from nothing at 0. It never moves the camera.</li>
 * <li><b>Down</b>: the body lies prone for the server's count, the view dims (also by Distortion Effects), and comes back up.</li>
 * <li><b>The walk</b>: the body's walk is a sprint's pace, so the game would widen the view as it does for a sprint. It does not: the
 * walk's share is taken back out of the field of view, and the stillness's too ({@link #fov}).</li>
 * </ul>
 * The sprint itself is never started (client/mixin/ListWalkMixin).
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class TheListBodyClient {
    private TheListBodyClient(){}
    private static final ResourceLocation INSIDE=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/gui/list_sack_inside.png");
    public static final ResourceLocation SACK_LAYER=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"list_sack"),
        PULL_LAYER=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"list_pull");
    private static boolean body;
    private static int down,lease;
    private static float pullYaw,pull,shown;
    private static boolean posed;
    private static @Nullable Pose before;

    public static void accept(TheListBodyPayload p){
        body=p.body();down=p.down();pullYaw=p.pullYaw();pull=p.pull();lease=body?40:0;
        if(!body){down=0;pull=0;}
    }
    /** Whether the local body has the slasher's walk (and so never sprints). */
    public static boolean walking(Player p){var a=p.getAttribute(Attributes.MOVEMENT_SPEED);return a!=null&&a.hasModifier(TheListBody.WALK_ID);}
    /** For a check: whether this client holds the body, how long it lies down, and how hard the pull draws as shown. */
    public static boolean body(){return body;}
    public static int downTicks(){return down;}
    public static float shownPull(){return shown;}

    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){body=false;down=0;lease=0;pull=0;shown=0;posed=false;before=null;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();var p=mc.player;
        if(p==null){body=false;down=0;posed=false;return;}
        if(lease>0&&--lease==0){body=false;down=0;pull=0;}
        if(down>0)down--;
        shown+=((body?pull:0)-shown)*.15F;
        if(walking(p)&&p.isSprinting())p.setSprinting(false);
        boolean lying=body&&down>0;
        if(lying&&!posed){before=p.getForcedPose();p.setForcedPose(Pose.SWIMMING);p.setPose(Pose.SWIMMING);posed=true;}
        else if(!lying&&posed){if(p.getForcedPose()==Pose.SWIMMING)p.setForcedPose(before);posed=false;before=null;}
    }

    /** The field of view as it would be without the body's walk or its stillness: the game's own sum, with those two left out. */
    @SubscribeEvent public static void fov(ComputeFovModifierEvent e){
        var a=e.getPlayer().getAttribute(Attributes.MOVEMENT_SPEED);if(a==null)return;
        if(!a.hasModifier(TheListBody.WALK_ID)&&!a.hasModifier(TheListBody.STILL_ID))return;
        float walking=e.getPlayer().getAbilities().getWalkingSpeed();if(walking<=0)return;
        float now=((float)a.getValue()/walking+1)/2,then=((float)without(a)/walking+1)/2;
        if(now>0)e.setNewFovModifier(e.getNewFovModifier()*then/now);
    }
    static double without(AttributeInstance a){
        double v=a.getBaseValue();
        for(var m:a.getModifiers())if(m.operation()==AttributeModifier.Operation.ADD_VALUE)v+=m.amount();
        double base=v;
        for(var m:a.getModifiers())if(m.operation()==AttributeModifier.Operation.ADD_MULTIPLIED_BASE)v+=base*m.amount();
        for(var m:a.getModifiers())
            if(m.operation()==AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL&&!m.id().equals(TheListBody.WALK_ID)&&!m.id().equals(TheListBody.STILL_ID))v*=1+m.amount();
        return a.getAttribute().value().sanitizeValue(v);
    }

    /** The sack from inside, over the whole screen as the game lays its pumpkin over it. */
    static void sack(GuiGraphics g,DeltaTracker t){
        var mc=Minecraft.getInstance();var p=mc.player;
        if(p==null||!TheListSack.worn(p)||!mc.options.getCameraType().isFirstPerson())return;
        RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        g.blit(INSIDE,0,0,g.guiWidth(),g.guiHeight(),0,0,128,64,128,64);
        RenderSystem.disableBlend();
    }
    /** How dark the view has gone while down: it closes over the first half second and lifts over the last second. */
    static float dim(){
        if(!body||down<=0)return 0;
        float d=down;float k=d>TheListBody.DOWN-10?(TheListBody.DOWN-d)/10F:d<20?d/20F:1F;
        return .6F*k*Comfort.effects();
    }
    /** A heart's beat over its period: the first stroke and the softer second. */
    static float beat(float ticks){
        float x=(ticks%TheListBody.CALM)/TheListBody.CALM;
        return Math.max((float)Math.exp(-Math.pow((x-.06)/.05,2)),.6F*(float)Math.exp(-Math.pow((x-.24)/.05,2)));
    }
    /** The pull at the screen's edges, and the dimming while down. */
    static void overlay(GuiGraphics g,DeltaTracker t){
        var mc=Minecraft.getInstance();var p=mc.player;if(p==null)return;
        int w=g.guiWidth(),h=g.guiHeight();
        float dark=dim();if(dark>.01F)g.fill(0,0,w,h,((int)(dark*255))<<24);
        float s=shown*Comfort.effects();if(s<.01F||!body)return;
        double rel=Math.toRadians(Mth.wrapDegrees(pullYaw-p.getYHeadRot()));
        float right=(float)Math.max(0,Math.sin(rel)),left=(float)Math.max(0,-Math.sin(rel)),behind=(float)Math.max(0,-Math.cos(rel));
        float a=s*(.55F+.45F*beat(p.tickCount+t.getGameTimeDeltaPartialTick(false)));
        int band=Math.max(16,w/6);
        edge(g,0,band,h,a*(.3F+.7F*left+.3F*behind),true);
        edge(g,w-band,band,h,a*(.3F+.7F*right+.3F*behind),false);
    }
    /** A band of the pull down one side of the screen, darkest at the edge, in twelve steps as the House draws everything. */
    static void edge(GuiGraphics g,int x,int band,int h,float alpha,boolean fromLeft){
        int steps=12;
        for(int i=0;i<steps;i++){
            float k=1-(i+.5F)/steps;k*=k;int a=(int)(Mth.clamp(alpha*k,0,1)*210);if(a<=0)continue;
            int x0=fromLeft?x+i*band/steps:x+band-(i+1)*band/steps,x1=fromLeft?x+(i+1)*band/steps:x+band-i*band/steps;
            g.fill(x0,0,x1,h,(a<<24)|0x3A0806);
        }
    }

    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Layers {
        private Layers(){}
        @SubscribeEvent public static void register(RegisterGuiLayersEvent e){
            e.registerBelowAll(SACK_LAYER,TheListBodyClient::sack);
            e.registerAbove(SACK_LAYER,PULL_LAYER,TheListBodyClient::overlay);
        }
    }
}
