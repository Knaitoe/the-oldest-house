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
 * Minecraft player is pixels; the zombies, skeletons and creepers are drawn the same way from the game's own pictures of them. The
 * server runs the game ({@link PolybiusBrawl}); this only sends the keys held and draws what comes back.
 */
public final class PolybiusBrawlScreen extends Screen {
    static final int W=200,H=150;
    static final int[] COLOURS={0xFF60D0FF,0xFFFF7060,0xFF80E060,0xFFF0D050};
    static final ResourceLocation ZOMBIE=ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png"),
            SKELETON=ResourceLocation.withDefaultNamespace("textures/entity/skeleton/skeleton.png"),CREEPER=ResourceLocation.withDefaultNamespace("textures/entity/creeper/creeper.png");
    final Set<Integer> keys=new HashSet<>();
    int seat;CompoundTag state=new CompoundTag();int sent=-1,ticks;
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
        if(held(GLFW.GLFW_KEY_SPACE,GLFW.GLFW_KEY_J,GLFW.GLFW_KEY_ENTER))k|=PolybiusBrawl.PUNCH;return k;
    }
    @Override public void tick(){ticks++;int k=mask();if(k!=sent||ticks%10==0){sent=k;PacketDistributor.sendToServer(new PolybiusBrawlKeysPayload(k));}}
    /** A knock or a pop when something goes down, a blip when a wave starts. */
    void sounds(CompoundTag before){
        int foes=before.getList("Foes",Tag.TAG_COMPOUND).size(),now=state.getList("Foes",Tag.TAG_COMPOUND).size();
        if(now<foes)beep(SoundEvents.NOTE_BLOCK_BASEDRUM.value(),1.2F);
        if(state.getInt("Wave")>before.getInt("Wave"))beep(SoundEvents.NOTE_BLOCK_BIT.value(),1.8F);
        for(Tag t:state.getList("Fighters",Tag.TAG_COMPOUND))if(((CompoundTag)t).getInt("Punch")==7)beep(SoundEvents.NOTE_BLOCK_SNARE.value(),1.4F);
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
        // Everyone on the sidewalk, drawn back to front.
        var bodies=new ArrayList<CompoundTag>();
        for(Tag t:state.getList("Foes",Tag.TAG_COMPOUND))bodies.add((CompoundTag)t);
        for(Tag t:state.getList("Fighters",Tag.TAG_COMPOUND))bodies.add((CompoundTag)t);
        bodies.sort(Comparator.comparingInt(c->c.getInt("Y")));
        for(var c:bodies){if(c.contains("Seat"))fighter(g,c);else foe(g,c);}
        for(Tag t:state.getList("Shots",Tag.TAG_COMPOUND)){var c=(CompoundTag)t;float x=c.getInt("X")/4F,y=c.getInt("Y")/4F;g.fill(Math.round(x-3),Math.round(y-14),Math.round(x+3),Math.round(y-13),0xFFD8D0C0);}
        hud(g);
        g.pose().popPose();
        g.disableScissor();
        String hint=state.getInt("Over")>=0?"GAME OVER":"[W][A][S][D] move   [Space] punch   [Esc] leave";
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
    void fighter(GuiGraphics g,CompoundTag c){
        if(c.getInt("Respawn")>0)return;
        // Nothing on these screens flashes: a hurt fighter, or one just back, is tinted while it lasts instead of blinking.
        int hurt=c.getInt("Hurt");if(hurt>0)RenderSystem.setShaderColor(1,.62F,.62F,1);
        float x=c.getInt("X")/4F,y=c.getInt("Y")/4F;int seatOf=c.getInt("Seat");
        g.fill(Math.round(x-6),Math.round(y-1),Math.round(x+6),Math.round(y+1),0x60000000);
        doll(g,skin(c.getUUID("Id")),PLAYER,x,y,c.getInt("Facing"),c.getBoolean("Moving")?ticks:0,c.getInt("Punch")>2);RenderSystem.setShaderColor(1,1,1,1);
        String name=c.getString("Name");if(name.length()>8)name=name.substring(0,8);
        g.pose().pushPose();g.pose().translate(x,y-38,0);g.pose().scale(.5F,.5F,1);g.drawString(font,name,-font.width(name)/2,0,COLOURS[seatOf%4],false);g.pose().popPose();
    }
    void foe(GuiGraphics g,CompoundTag c){
        float x=c.getInt("X")/4F,y=c.getInt("Y")/4F;int type=c.getInt("Type");int hurt=c.getInt("Hurt"),fuse=c.getInt("Fuse");
        g.fill(Math.round(x-6),Math.round(y-1),Math.round(x+6),Math.round(y+1),0x60000000);
        // A lit creeper whitens steadily as its fuse burns down, rather than flashing.
        if(hurt>0)RenderSystem.setShaderColor(1,.45F,.45F,1);else if(fuse>0){float w=1+fuse/30F*.9F;RenderSystem.setShaderColor(w,w,w,1);}
        switch(type){
            case PolybiusBrawl.ZOMBIE->doll(g,ZOMBIE,ZOMBIE_RIG,x,y,c.getInt("Facing"),c.getInt("Walk"),false);
            case PolybiusBrawl.SKELETON->doll(g,SKELETON,SKELETON_RIG,x,y,c.getInt("Facing"),c.getInt("Walk"),false);
            default->creeper(g,x,y,c.getInt("Walk"));
        }
        RenderSystem.setShaderColor(1,1,1,1);
    }
    /** A creeper: head, body and four short legs, from its own picture. */
    void creeper(GuiGraphics g,float x,float y,int walk){
        int s=walk%12<6?1:0;
        g.blit(CREEPER,Math.round(x-4),Math.round(y-6+s),4,20,4,6,64,32);g.blit(CREEPER,Math.round(x),Math.round(y-6-s+1),4,20,4,6,64,32);
        g.blit(CREEPER,Math.round(x-4),Math.round(y-18),20,20,8,12,64,32);
        g.blit(CREEPER,Math.round(x-4),Math.round(y-26),8,8,8,8,64,32);
    }
    void hud(GuiGraphics g){
        g.fill(0,0,W,13,0xC0000000);
        var fighters=state.getList("Fighters",Tag.TAG_COMPOUND);
        for(int i=0;i<PolybiusBrawl.SEATS;i++){int x=2+i*50;CompoundTag c=null;
            for(Tag t:fighters)if(((CompoundTag)t).getInt("Seat")==i)c=(CompoundTag)t;
            g.pose().pushPose();g.pose().translate(x,2,0);g.pose().scale(.5F,.5F,1);
            if(c==null)g.drawString(font,(state.getInt("Lobby")>0||state.getInt("Over")<0)?"PRESS START":"",0,0,0xFF505050,false);
            else{g.drawString(font,c.getString("Name"),0,0,COLOURS[i],false);g.drawString(font,Integer.toString(c.getInt("Score")),0,10,0xFFE0E0E0,false);}
            g.pose().popPose();
            if(c!=null){g.fill(x+22,3,x+22+20,5,0xFF401010);g.fill(x+22,3,x+22+c.getInt("Hp"),5,0xFFE04030);for(int l=0;l<c.getInt("Lives");l++)g.fill(x+22+l*4,7,x+25+l*4,10,COLOURS[i]);}
        }
        int lobby=state.getInt("Lobby"),banner=state.getInt("Banner"),over=state.getInt("Over");
        if(lobby>0)centred(g,"STARTING IN "+(lobby/20+1),60,0xFFFFE060);
        else if(over>=0)centred(g,"GAME OVER",60,0xFFFF6050);
        else if(banner>0)centred(g,"WAVE "+state.getInt("Wave"),60,0xFFFFE060);
    }
    void centred(GuiGraphics g,String s,int y,int colour){g.drawString(font,s,(W-font.width(s))/2,y,colour,true);}
}
