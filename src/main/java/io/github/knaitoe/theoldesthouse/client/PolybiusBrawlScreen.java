package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.labyrinth.PolybiusBrawl;
import io.github.knaitoe.theoldesthouse.network.PolybiusBrawlKeysPayload;
import io.github.knaitoe.theoldesthouse.network.PolybiusBrawlPayload;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * 0.4.221, Block Party: the four-player cabinet's screen. A street at night on a 200 by 150 field: lit windows going by overhead,
 * a sidewalk to fight along. Each player is drawn from their own skin, face, shirt, sleeves and trousers, a paper doll as tall as a
 * Minecraft player is pixels; the foes are drawn the same way from the game's own pictures of them. The server runs the game
 * ({@link PolybiusBrawl}); this only sends the keys held and draws what comes back.
 *
 * <p>0.4.224: the class each fighter picked (left and right while the game waits), with what they hold; the chain of hits and
 * the finisher; the special and its wait; the items lying in the street and what is in effect; arrows, potions and TNT in the
 * air, and the rings of their splashes; spiders and endermen. Nothing flashes: what changes, changes steadily.
 */
public final class PolybiusBrawlScreen extends Screen {
    static final int W=200,H=150;
    static final int[] COLOURS={0xFF60D0FF,0xFFFF7060,0xFF80E060,0xFFF0D050};
    static ResourceLocation vanilla(String path){return ResourceLocation.withDefaultNamespace("textures/"+path+".png");}
    static final ResourceLocation ZOMBIE=vanilla("entity/zombie/zombie"),SKELETON=vanilla("entity/skeleton/skeleton"),CREEPER=vanilla("entity/creeper/creeper"),
            SPIDER=vanilla("entity/spider/spider"),SPIDER_EYES=vanilla("entity/spider_eyes"),ENDERMAN=vanilla("entity/enderman/enderman"),ENDER_EYES=vanilla("entity/enderman/enderman_eyes");
    /** What each class holds and is shown by, from the game's own item pictures. */
    static final ResourceLocation[] CLASS_ICON={vanilla("item/leather"),vanilla("item/iron_sword"),vanilla("item/bow"),vanilla("item/splash_potion")};
    static final ResourceLocation POTION=vanilla("item/potion"),POTION_LIQUID=vanilla("item/potion_overlay"),SPLASH=vanilla("item/splash_potion"),SPLASH_LIQUID=POTION_LIQUID,
            ARROW=vanilla("item/arrow"),TNT=vanilla("block/tnt_side"),SHIELD=vanilla("entity/shield_base_nopattern");
    static final ResourceLocation[] DROP_ICON={vanilla("item/cooked_beef"),vanilla("item/golden_apple"),null,null,vanilla("block/tnt_side"),vanilla("item/totem_of_undying")};
    static final int SWIFT_TINT=0xFF7CAFC6,STRENGTH_TINT=0xFF932423,HARM_TINT=0xFF8E3FB4;
    final Set<Integer> keys=new HashSet<>();
    int seat;CompoundTag state=new CompoundTag();int sent=-1,ticks;
    /** How faded the next item picture is drawn (an item lying long in the street fades). */
    float fade=1;
    PolybiusBrawlScreen(int seat){super(Component.literal("BLOCK PARTY"));this.seat=seat;}
    public static void accept(PolybiusBrawlPayload p){
        var mc=Minecraft.getInstance();
        if(p.seat()<0){if(mc.screen instanceof PolybiusBrawlScreen)mc.setScreen(null);return;}
        PolybiusBrawlScreen s;if(mc.screen instanceof PolybiusBrawlScreen open)s=open;else{s=new PolybiusBrawlScreen(p.seat());mc.setScreen(s);}
        s.seat=p.seat();var before=s.state;s.state=p.state();s.sounds(before);
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){PacketDistributor.sendToServer(new PolybiusBrawlKeysPayload(PolybiusBrawl.LEAVE));super.onClose();}
    @Override public boolean keyPressed(int key,int scan,int mods){keys.add(key);return super.keyPressed(key,scan,mods)||key!=GLFW.GLFW_KEY_ESCAPE;}
    @Override public boolean keyReleased(int key,int scan,int mods){keys.remove(key);return true;}
    boolean held(int... codes){for(int c:codes)if(keys.contains(c))return true;return false;}
    int mask(){
        int k=0;if(held(GLFW.GLFW_KEY_W,GLFW.GLFW_KEY_UP))k|=PolybiusBrawl.UP;if(held(GLFW.GLFW_KEY_S,GLFW.GLFW_KEY_DOWN))k|=PolybiusBrawl.DOWN;
        if(held(GLFW.GLFW_KEY_A,GLFW.GLFW_KEY_LEFT))k|=PolybiusBrawl.LEFT;if(held(GLFW.GLFW_KEY_D,GLFW.GLFW_KEY_RIGHT))k|=PolybiusBrawl.RIGHT;
        if(held(GLFW.GLFW_KEY_SPACE,GLFW.GLFW_KEY_J,GLFW.GLFW_KEY_ENTER))k|=PolybiusBrawl.PUNCH;
        if(held(GLFW.GLFW_KEY_K,GLFW.GLFW_KEY_LEFT_SHIFT,GLFW.GLFW_KEY_RIGHT_SHIFT))k|=PolybiusBrawl.SPECIAL;return k;
    }
    @Override public void tick(){ticks++;int k=mask();if(k!=sent||ticks%10==0){sent=k;PacketDistributor.sendToServer(new PolybiusBrawlKeysPayload(k));}}
    /** A knock or a pop when something goes down, a blip when a wave starts, a chime for an item, a thump for a blast. */
    void sounds(CompoundTag before){
        int foes=before.getList("Foes",Tag.TAG_COMPOUND).size(),now=state.getList("Foes",Tag.TAG_COMPOUND).size();
        if(now<foes)beep(SoundEvents.NOTE_BLOCK_BASEDRUM.value(),1.2F);
        if(state.getInt("Wave")>before.getInt("Wave"))beep(SoundEvents.NOTE_BLOCK_BIT.value(),1.8F);
        if(state.getList("Drops",Tag.TAG_COMPOUND).size()<before.getList("Drops",Tag.TAG_COMPOUND).size())beep(SoundEvents.NOTE_BLOCK_CHIME.value(),1.5F);
        for(Tag t:state.getList("Rings",Tag.TAG_COMPOUND))if(((CompoundTag)t).getInt("Age")==1)beep(((CompoundTag)t).getInt("Kind")==2?SoundEvents.NOTE_BLOCK_BASEDRUM.value():SoundEvents.NOTE_BLOCK_HARP.value(),((CompoundTag)t).getInt("Kind")==2?.6F:1.9F);
        var swung=new HashMap<Integer,Integer>();for(Tag t:before.getList("Fighters",Tag.TAG_COMPOUND))swung.put(((CompoundTag)t).getInt("Seat"),((CompoundTag)t).getInt("Punch"));
        for(Tag t:state.getList("Fighters",Tag.TAG_COMPOUND)){var c=(CompoundTag)t;if(c.getInt("Punch")>swung.getOrDefault(c.getInt("Seat"),0))beep(c.getBoolean("Finish")?SoundEvents.NOTE_BLOCK_BELL.value():SoundEvents.NOTE_BLOCK_SNARE.value(),c.getBoolean("Finish")?1.2F:1.4F);}
    }
    void beep(net.minecraft.sounds.SoundEvent s,float pitch){Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(s,pitch,.3F));}

    @Override public void renderBackground(GuiGraphics g,int mx,int my,float partial){g.fill(0,0,width,height,0xF0000000);}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        renderBackground(g,mx,my,partial);
        float scale=Math.max(1,Math.min((width-24)/(float)W,(height-48)/(float)H));
        int sw=Math.round(W*scale),sh=Math.round(H*scale),x0=(width-sw)/2,y0=(height-sh)/2+8;
        g.fill(x0-6,y0-6,x0+sw+6,y0+sh+6,0xFF241A10);g.fill(x0-2,y0-2,x0+sw+2,y0+sh+2,0xFF000000);
        String title="BLOCK PARTY";g.drawString(font,title,(width-font.width(title))/2,y0-20,0xFFFFB040,false);
        g.enableScissor(x0,y0,x0+sw,y0+sh);
        g.pose().pushPose();g.pose().translate(x0,y0,0);g.pose().scale(scale,scale,1);
        street(g);
        for(Tag t:state.getList("Drops",Tag.TAG_COMPOUND))drop(g,(CompoundTag)t);
        // Everyone on the sidewalk, drawn back to front.
        var bodies=new ArrayList<CompoundTag>();
        for(Tag t:state.getList("Foes",Tag.TAG_COMPOUND))bodies.add((CompoundTag)t);
        for(Tag t:state.getList("Fighters",Tag.TAG_COMPOUND))bodies.add((CompoundTag)t);
        bodies.sort(Comparator.comparingInt(c->c.getInt("Y")));
        for(var c:bodies){if(c.contains("Seat"))fighter(g,c);else foe(g,c);}
        for(Tag t:state.getList("Shots",Tag.TAG_COMPOUND)){var c=(CompoundTag)t;float x=c.getInt("X")/4F,y=c.getInt("Y")/4F;g.fill(Math.round(x-3),Math.round(y-14),Math.round(x+3),Math.round(y-13),0xFFD8D0C0);}
        for(Tag t:state.getList("Bolts",Tag.TAG_COMPOUND))bolt(g,(CompoundTag)t);
        for(Tag t:state.getList("Rings",Tag.TAG_COMPOUND))ring(g,(CompoundTag)t);
        hud(g);
        g.pose().popPose();
        g.disableScissor();
        String hint=state.getInt("Over")>=0?"GAME OVER":state.getInt("Lobby")>0||waiting()!=null?"[A][D] pick who you are   [Esc] leave":"[W][A][S][D] move   [Space] attack   [K] special   [Esc] leave";
        g.drawString(font,hint,(width-font.width(hint))/2,y0+sh+10,0xFF9A8A70,false);
    }
    /** The street: night sky, a row of houses with their windows lit going by as the waves go on, the sidewalk and the kerb. */
    void street(GuiGraphics g){
        g.fill(0,0,W,PolybiusBrawl.TOP-14,0xFF0A0C1E);
        int shift=(state.getInt("Wave")*37)%60;
        for(int i=-1;i<6;i++){int x=i*44-shift%44,top=22+((i+state.getInt("Wave"))*13&15);
            g.fill(x,top,x+40,PolybiusBrawl.TOP-14,0xFF161424);
            for(int wy=top+6;wy<PolybiusBrawl.TOP-20;wy+=10)for(int wx=x+5;wx<x+36;wx+=10){boolean lit=((wx*7+wy*13+i*5)&3)!=0;g.fill(wx,wy,wx+5,wy+6,lit?0xFFE0B860:0xFF0E0C18);}}
        g.fill(0,PolybiusBrawl.TOP-14,W,PolybiusBrawl.TOP-10,0xFF3A3A40);
        g.fill(0,PolybiusBrawl.TOP-10,W,H,0xFF2C2C30);
        for(int x=0;x<W;x+=20)g.fill(x,PolybiusBrawl.TOP-10,x+1,H,0xFF242428);
        g.fill(0,H-6,W,H,0xFF55504A);
    }
    /** Where a body's parts are in its picture: the picture's height, then each leg's and arm's front face and their widths. */
    record Rig(int texH,int legW,int legRU,int legRV,int legLU,int legLV,int armW,int armRU,int armRV,int armLU,int armLV,boolean overlay){}
    static final Rig PLAYER=new Rig(64,4,4,20,20,52,4,44,20,36,52,true),SKELETON_RIG=new Rig(32,2,2,18,2,18,2,42,18,42,18,false),
            // 0.4.223: a zombie's picture has no left arm or leg of its own (the game mirrors the right ones), so they were drawn blank.
            ZOMBIE_RIG=new Rig(64,4,4,20,4,20,4,44,20,44,20,false);
    /** A paper doll: head, body, arms and legs, front on, turned by the way it faces; walking swings a leg and an arm. */
    void doll(GuiGraphics g,ResourceLocation tex,Rig r,float x,float y,int facing,int step,boolean punching){
        float top=y-32,left=x-4;int swing=step==0?0:(step%16<8?1:-1);
        g.pose().pushPose();
        // 0.4.223 (owner: "rendering is messed up on brawler"): turned to the left the doll is drawn mirrored, which turns its
        // faces inside out, and the screen culled them: everything walking left was invisible but its shadow.
        if(facing<0){g.pose().translate(x*2,0,0);g.pose().scale(-1,1,1);RenderSystem.disableCull();}
        g.blit(tex,Math.round(left+4-r.legW()),Math.round(top+20+swing),r.legRU(),r.legRV(),r.legW(),12,64,r.texH());
        g.blit(tex,Math.round(left+4),Math.round(top+20-swing),r.legLU(),r.legLV(),r.legW(),12,64,r.texH());
        g.blit(tex,Math.round(left),Math.round(top+8),20,20,8,12,64,r.texH());
        g.blit(tex,Math.round(left-r.armW()),Math.round(top+8),r.armRU(),r.armRV(),r.armW(),12,64,r.texH());
        if(punching){g.pose().pushPose();g.pose().translate(left+8,top+8+r.armW(),0);g.pose().mulPose(Axis.ZP.rotationDegrees(-90));g.blit(tex,0,0,r.armLU(),r.armLV(),r.armW(),12,64,r.texH());g.pose().popPose();}
        else g.blit(tex,Math.round(left+8),Math.round(top+8-swing),r.armLU(),r.armLV(),r.armW(),12,64,r.texH());
        g.blit(tex,Math.round(left),Math.round(top),8,8,8,8,64,r.texH());
        if(r.overlay())g.blit(tex,Math.round(left),Math.round(top),40,8,8,8,64,r.texH());
        if(facing<0)RenderSystem.enableCull();
        g.pose().popPose();
    }
    ResourceLocation skin(UUID id){
        var mc=Minecraft.getInstance();var info=mc.getConnection()==null?null:mc.getConnection().getPlayerInfo(id);
        return info!=null?info.getSkin().texture():DefaultPlayerSkin.get(id).texture();
    }
    /** A sixteen-pixel item picture at a size, centred, turned. */
    void icon(GuiGraphics g,ResourceLocation tex,float x,float y,float size,float degrees,int tint){
        g.pose().pushPose();g.pose().translate(x,y,0);if(degrees!=0)g.pose().mulPose(Axis.ZP.rotationDegrees(degrees));
        float s=size/16F;g.pose().scale(s,s,1);
        if(tint!=-1||fade<1)RenderSystem.setShaderColor(tint==-1?1:(tint>>16&255)/255F,tint==-1?1:(tint>>8&255)/255F,tint==-1?1:(tint&255)/255F,fade);
        RenderSystem.enableBlend();g.blit(tex,-8,-8,0,0,16,16,16,16);
        if(tint!=-1||fade<1)RenderSystem.setShaderColor(1,1,1,1);
        g.pose().popPose();
    }
    /** The game's item pictures point one way (the sword and the arrow up and to the right, the bow's arc up and to the left): the
     *  turn that points one at {@code a} degrees below straight ahead, for a body facing right or left. */
    static final float POINTS_UP_RIGHT=-45,POINTS_UP_LEFT=-135;
    static float aim(int facing,float a,float picture){return facing>0?a-picture:180-a-picture;}
    /** A potion bottle with its liquid in a colour. */
    void bottle(GuiGraphics g,boolean splash,float x,float y,float size,float degrees,int tint){icon(g,splash?SPLASH_LIQUID:POTION_LIQUID,x,y,size,degrees,tint);icon(g,splash?SPLASH:POTION,x,y,size,degrees,-1);}
    void fighter(GuiGraphics g,CompoundTag c){
        if(c.getInt("Respawn")>0)return;
        // Nothing on these screens flashes: a hurt fighter, or one just back, is tinted while it lasts instead of blinking.
        int hurt=c.getInt("Hurt");if(hurt>0)RenderSystem.setShaderColor(1,.62F,.62F,1);
        float x=c.getInt("X")/4F,y=c.getInt("Y")/4F;int seatOf=c.getInt("Seat"),cls=c.getInt("Class"),facing=c.getInt("Facing");
        g.fill(Math.round(x-6),Math.round(y-1),Math.round(x+6),Math.round(y+1),0x60000000);
        if(c.getInt("Dash")>0)for(int i=1;i<=3;i++)g.fill(Math.round(x-facing*(6+i*4)),Math.round(y-20+i*4),Math.round(x-facing*(2+i*4)),Math.round(y-19+i*4),0x90D0D0D0);
        boolean swinging=c.getInt("Punch")>2;
        doll(g,skin(c.getUUID("Id")),PLAYER,x,y,facing,c.getBoolean("Moving")?ticks:0,swinging&&cls==PolybiusBrawl.BRAWLER);
        RenderSystem.setShaderColor(1,1,1,1);
        held(g,c,x,y,facing,cls,swinging);
        String name=c.getString("Name");if(name.length()>8)name=name.substring(0,8);
        g.pose().pushPose();g.pose().translate(x,y-38,0);g.pose().scale(.5F,.5F,1);g.drawString(font,name,-font.width(name)/2,0,COLOURS[seatOf%4],false);
        if(state.getInt("Lobby")>0){String pick="< "+PolybiusBrawl.CLASS_NAMES[cls]+" >";g.drawString(font,pick,-font.width(pick)/2,-10,0xFFFFE060,false);}
        else if(c.getInt("Combo")>=2){String chain="x"+c.getInt("Combo")+(c.getBoolean("Finish")?" FINISH":"");g.drawString(font,chain,-font.width(chain)/2,-10,0xFFFFE060,false);}
        g.pose().popPose();
    }
    /** What a fighter holds: the Knight's sword (and shield, raised), the Archer's bow, the Alchemist's next potion, TNT if carried. */
    void held(GuiGraphics g,CompoundTag c,float x,float y,int facing,int cls,boolean swinging){
        float hx=x+facing*6,hy=y-17;
        if(c.getInt("Tnt")>0){icon(g,TNT,hx,hy,6,0,-1);return;}
        switch(cls){
            case PolybiusBrawl.KNIGHT->{
                // At rest the blade is up and forward; a swing brings it down through the front; a finisher spins it right round.
                int punch=c.getInt("Punch");float a=!swinging?-45:c.getBoolean("Finish")?punch*45:-60+(8-punch)*15;
                icon(g,CLASS_ICON[cls],hx,hy-2,10,aim(facing,a,POINTS_UP_RIGHT),-1);
                if(c.getInt("Shield")>0){g.pose().pushPose();g.pose().translate(x+facing*7,y-24,0);g.blit(SHIELD,-3,0,6,14,1,1,12,22,64,64);g.pose().popPose();}
            }
            case PolybiusBrawl.ARCHER->icon(g,CLASS_ICON[cls],hx,hy,9,aim(facing,0,POINTS_UP_LEFT),-1);
            case PolybiusBrawl.ALCHEMIST->bottle(g,true,hx,hy,7,0,HARM_TINT);
            default->{}
        }
    }
    void foe(GuiGraphics g,CompoundTag c){
        float x=c.getInt("X")/4F,y=c.getInt("Y")/4F;int type=c.getInt("Type");int hurt=c.getInt("Hurt"),fuse=c.getInt("Fuse");
        g.fill(Math.round(x-6),Math.round(y-1),Math.round(x+6),Math.round(y+1),0x60000000);
        // A lit creeper whitens steadily as its fuse burns down, rather than flashing; a slowed foe is tinted the potion's colour.
        if(hurt>0)RenderSystem.setShaderColor(1,.45F,.45F,1);else if(fuse>0){float w=1+fuse/30F*.9F;RenderSystem.setShaderColor(w,w,w,1);}
        else if(c.getInt("Slow")>0)RenderSystem.setShaderColor(.85F,.7F,1,1);
        switch(type){
            case PolybiusBrawl.ZOMBIE->doll(g,ZOMBIE,ZOMBIE_RIG,x,y,c.getInt("Facing"),c.getInt("Walk"),false);
            case PolybiusBrawl.SKELETON->doll(g,SKELETON,SKELETON_RIG,x,y,c.getInt("Facing"),c.getInt("Walk"),false);
            case PolybiusBrawl.SPIDER->spider(g,x,y,c.getInt("Walk"));
            case PolybiusBrawl.ENDERMAN->enderman(g,x,y,c.getInt("Walk"));
            default->creeper(g,x,y,c.getInt("Walk"));
        }
        RenderSystem.setShaderColor(1,1,1,1);
        int tall=switch(type){case PolybiusBrawl.ENDERMAN->54;case PolybiusBrawl.SPIDER->16;case PolybiusBrawl.CREEPER->30;default->36;};
        if(c.getInt("Stun")>0){g.pose().pushPose();g.pose().translate(x,y-tall,0);g.pose().scale(.5F,.5F,1);g.drawString(font,"* *",-font.width("* *")/2,0,0xFFFFE060,false);g.pose().popPose();}
    }
    /** A creeper: head, body and four short legs, from its own picture. */
    void creeper(GuiGraphics g,float x,float y,int walk){
        int s=walk%12<6?1:0;
        g.blit(CREEPER,Math.round(x-4),Math.round(y-6+s),4,20,4,6,64,32);g.blit(CREEPER,Math.round(x),Math.round(y-6-s+1),4,20,4,6,64,32);
        g.blit(CREEPER,Math.round(x-4),Math.round(y-18),20,20,8,12,64,32);
        g.blit(CREEPER,Math.round(x-4),Math.round(y-26),8,8,8,8,64,32);
    }
    /** A spider, low to the street: its body behind, its head and red eyes in front, its legs splayed either side. */
    void spider(GuiGraphics g,float x,float y,int walk){
        int s=walk%8<4?1:0;
        for(int i=0;i<4;i++){int ly=Math.round(y-9+i*2+((i+s)%2));g.blit(SPIDER,Math.round(x-15),ly,20,2,10,2,64,32);g.blit(SPIDER,Math.round(x+5),ly,20,2,10,2,64,32);}
        g.blit(SPIDER,Math.round(x-5),Math.round(y-12),12,24,10,8,64,32);
        g.blit(SPIDER,Math.round(x-4),Math.round(y-11),40,12,8,8,64,32);
        RenderSystem.enableBlend();g.blit(SPIDER_EYES,Math.round(x-4),Math.round(y-11),40,12,8,8,64,32);
    }
    /** An enderman, tall and thin, its long arms hanging, its purple eyes. */
    void enderman(GuiGraphics g,float x,float y,int walk){
        int s=walk%16<8?1:0;float top=y-50;
        g.blit(ENDERMAN,Math.round(x-2),Math.round(top+20+s),58,2,2,30,64,32);g.blit(ENDERMAN,Math.round(x),Math.round(top+20-s),58,2,2,30,64,32);
        g.blit(ENDERMAN,Math.round(x-4),Math.round(top+8),36,20,8,12,64,32);
        g.blit(ENDERMAN,Math.round(x-6),Math.round(top+8),58,2,2,28,64,32);g.blit(ENDERMAN,Math.round(x+4),Math.round(top+8),58,2,2,28,64,32);
        g.blit(ENDERMAN,Math.round(x-4),Math.round(top),8,8,8,8,64,32);
        RenderSystem.enableBlend();g.blit(ENDER_EYES,Math.round(x-4),Math.round(top),8,8,8,8,64,32);
    }
    /** An item lying in the street, fading as it gets old rather than blinking. */
    void drop(GuiGraphics g,CompoundTag c){
        float x=c.getInt("X")/4F,y=c.getInt("Y")/4F;int type=c.getInt("Type"),age=c.getInt("Age");float bob=(float)Math.sin((ticks+x)*.15)*1.2F;
        g.fill(Math.round(x-4),Math.round(y-1),Math.round(x+4),Math.round(y),0x50000000);
        fade=age>300?Math.max(.3F,1-(age-300)/140F):1;
        if(type==PolybiusBrawl.SWIFTNESS)bottle(g,false,x,y-6+bob,9,0,SWIFT_TINT);
        else if(type==PolybiusBrawl.STRENGTH)bottle(g,false,x,y-6+bob,9,0,STRENGTH_TINT);
        else icon(g,DROP_ICON[type],x,y-6+bob,9,0,-1);
        fade=1;
    }
    /** An arrow flying, a potion or TNT arcing to where it will land. */
    void bolt(GuiGraphics g,CompoundTag c){
        float x=c.getInt("X")/4F,y=c.getInt("Y")/4F;int kind=c.getInt("Kind"),dir=c.getInt("Dir"),t=c.getInt("T");
        if(kind==PolybiusBrawl.ARROW){icon(g,ARROW,x,y-16,9,aim(dir,0,POINTS_UP_RIGHT),-1);return;}
        float lift=t*(24-t)/8F;
        if(kind==PolybiusBrawl.POTION)bottle(g,true,x,y-16-lift,7,ticks*24%360,HARM_TINT);else icon(g,TNT,x,y-16-lift,7,ticks*18%360,-1);
    }
    /** A splash, a blast or an enderman's step, a ring that widens and fades over half a second. */
    void ring(GuiGraphics g,CompoundTag c){
        float x=c.getInt("X")/4F,y=c.getInt("Y")/4F;int age=c.getInt("Age"),kind=c.getInt("Kind");float r=c.getInt("R")*Math.min(1,age/6F);
        int colour=switch(kind){case 1->0xF070A0;case 2->0xFF9030;case 3->0xA040FF;default->0xC050E0;};int alpha=Math.max(0,220-age*20);
        for(int i=0;i<36;i++){double a=Math.PI*2*i/36;int px=Math.round((float)(x+Math.cos(a)*r)),py=Math.round((float)(y-8+Math.sin(a)*r/1.5));g.fill(px,py,px+1,py+1,alpha<<24|colour);}
    }
    /** This screen's own fighter while it waits to come back in with lives left, or null. */
    CompoundTag waiting(){
        for(Tag t:state.getList("Fighters",Tag.TAG_COMPOUND)){var c=(CompoundTag)t;if(c.getInt("Seat")==seat&&c.getInt("Respawn")>0)return c;}
        return null;
    }
    /** Along the top, a panel per stick: the class's picture, the name and score, health and the special's wait, lives, and
     *  what is in effect (swiftness, strength, TNT carried). */
    void hud(GuiGraphics g){
        g.fill(0,0,W,19,0xC0000000);
        var fighters=state.getList("Fighters",Tag.TAG_COMPOUND);
        for(int i=0;i<PolybiusBrawl.SEATS;i++){int x=2+i*50;CompoundTag c=null;
            for(Tag t:fighters)if(((CompoundTag)t).getInt("Seat")==i)c=(CompoundTag)t;
            if(c==null){g.pose().pushPose();g.pose().translate(x+9,2,0);g.pose().scale(.5F,.5F,1);
                g.drawString(font,(state.getInt("Lobby")>0||state.getInt("Over")<0)?"PRESS START":"",0,0,0xFF505050,false);g.pose().popPose();continue;}
            if(c.getInt("Class")==PolybiusBrawl.ALCHEMIST)bottle(g,true,x+4,7,8,0,HARM_TINT);else icon(g,CLASS_ICON[c.getInt("Class")],x+4,7,8,0,-1);
            String name=c.getString("Name");if(name.length()>7)name=name.substring(0,7);
            g.pose().pushPose();g.pose().translate(x+9,2,0);g.pose().scale(.5F,.5F,1);
            g.drawString(font,name,0,0,COLOURS[i],false);g.drawString(font,Integer.toString(c.getInt("Score")),0,10,0xFFE0E0E0,false);
            g.pose().popPose();
            int max=Math.max(1,c.getInt("Max")),wait=Math.max(1,c.getInt("Wait"));
            g.fill(x+31,3,x+48,5,0xFF401010);g.fill(x+31,3,x+31+17*Math.max(0,c.getInt("Hp"))/max,5,0xFFE04030);
            g.fill(x+31,6,x+48,7,0xFF102040);g.fill(x+31,6,x+31+17*(wait-c.getInt("Special"))/wait,7,0xFF40A0F0);
            for(int l=0;l<Math.min(4,c.getInt("Lives"));l++)g.fill(x+31+l*4,9,x+34+l*4,12,COLOURS[i]);
            float bx=x+11;
            if(c.getInt("Swift")>0){bottle(g,false,bx,15.5F,5,0,SWIFT_TINT);bx+=5;}
            if(c.getInt("Strong")>0){bottle(g,false,bx,15.5F,5,0,STRENGTH_TINT);bx+=5;}
            for(int n=0;n<c.getInt("Tnt");n++){icon(g,TNT,bx,15.5F,4,0,-1);bx+=5;}
        }
        int lobby=state.getInt("Lobby"),banner=state.getInt("Banner"),over=state.getInt("Over");
        if(lobby>0)centred(g,"STARTING IN "+(lobby/20+1),60,0xFFFFE060);
        else if(over>=0)centred(g,"GAME OVER",60,0xFFFF6050);
        else if(waiting()!=null)centred(g,"BACK IN "+(waiting().getInt("Respawn")/20+1)+"   < "+PolybiusBrawl.CLASS_NAMES[waiting().getInt("Class")]+" >",60,0xFFFFE060);
        else if(banner>0)centred(g,"WAVE "+state.getInt("Wave")+(state.getInt("Wave")%5==0?"  -  SOMETHING TALL":""),60,0xFFFFE060);
    }
    void centred(GuiGraphics g,String s,int y,int colour){g.drawString(font,s,(W-font.width(s))/2,y,colour,true);}
}
