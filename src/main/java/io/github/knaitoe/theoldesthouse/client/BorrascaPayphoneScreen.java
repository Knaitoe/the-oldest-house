package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.labyrinth.PhoneSounds;
import io.github.knaitoe.theoldesthouse.network.BorrascaDialPayload;
import io.github.knaitoe.theoldesthouse.network.BorrascaPayphonePayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * 0.4.426: the Ridge Service payphone. A steel face with the card under the dial, the number as it is dialled, twelve keys and a hook;
 * seven digits (or 911) place the call. The screen shows only what the server sends: what is in the purse, how many rings, and what is
 * said, line by line, after the pick-up. Keys work from the keyboard too.
 */
public final class BorrascaPayphoneScreen extends Screen {
    private static final String[] KEYS={"1","2","3","4","5","6","7","8","9","*","0","#"};
    private static final int RING=60,LINE=44;
    private CompoundTag state;
    private String typed="";
    private final List<String> lines=new ArrayList<>();
    private int rings,callAge=-1,age,px,py;
    private boolean sent;

    public BorrascaPayphoneScreen(CompoundTag state){super(Component.literal("Payphone"));this.state=state;}
    public static void accept(BorrascaPayphonePayload payload){
        var mc=Minecraft.getInstance();
        if(mc.screen instanceof BorrascaPayphoneScreen phone)phone.update(payload.state());
        else if(payload.open())mc.setScreen(new BorrascaPayphoneScreen(payload.state()));
    }
    private void update(CompoundTag next){
        state=next;
        if(next.contains("Call")){var call=next.getCompound("Call");lines.clear();for(Tag t:call.getList("Lines",Tag.TAG_STRING))lines.add(t.getAsString());rings=call.getInt("Rings");callAge=0;}
    }
    @Override protected void init(){
        px=width/2-80;py=height/2-110;
        for(int i=0;i<KEYS.length;i++){String key=KEYS[i];addRenderableWidget(Button.builder(Component.literal(key),b->press(key)).bounds(px+38+(i%3)*30,py+104+(i/3)*22,26,18).build());}
        addRenderableWidget(Button.builder(Component.literal("Hang up"),b->hangUp()).bounds(px+38,py+196,86,18).build());
    }
    private void press(String key){
        if(sent||typed.length()>=11)return;play(PhoneSounds.BEEP.get(),1.4F+KEYS_INDEX(key)*.03F);
        if(!Character.isDigit(key.charAt(0)))return;typed+=key;
        if(typed.length()==7||typed.equals("911")){sent=true;PacketDistributor.sendToServer(new BorrascaDialPayload(typed));}
    }
    private static int KEYS_INDEX(String key){for(int i=0;i<KEYS.length;i++)if(KEYS[i].equals(key))return i;return 0;}
    private void hangUp(){if(sent||callAge>=0){typed="";sent=false;callAge=-1;lines.clear();play(PhoneSounds.PICKUP.get(),.8F);}else onClose();}
    private void play(SoundEvent sound,float pitch){Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound,pitch,.6F));}
    @Override public boolean keyPressed(int key,int scan,int mods){
        if(key>=GLFW.GLFW_KEY_0&&key<=GLFW.GLFW_KEY_9){press(String.valueOf(key-GLFW.GLFW_KEY_0));return true;}
        if(key>=GLFW.GLFW_KEY_KP_0&&key<=GLFW.GLFW_KEY_KP_9){press(String.valueOf(key-GLFW.GLFW_KEY_KP_0));return true;}
        if(key==GLFW.GLFW_KEY_BACKSPACE&&!sent&&!typed.isEmpty()){typed=typed.substring(0,typed.length()-1);return true;}
        return super.keyPressed(key,scan,mods);
    }
    @Override public void tick(){
        age++;if(callAge<0)return;callAge++;
        if(callAge<=rings*RING&&callAge%RING==1)play(PhoneSounds.RINGBACK.get(),1);
        if(rings>0&&callAge==rings*RING+1)play(PhoneSounds.PICKUP.get(),1);
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void renderBackground(GuiGraphics g,int mouseX,int mouseY,float partial){
        super.renderBackground(g,mouseX,mouseY,partial);
        // The steel face, its screws and its coin slot; the card under the dial; the number window.
        g.fill(px,py,px+160,py+222,0xff5c6266);g.fill(px+2,py+2,px+158,py+220,0xff8a9196);g.fill(px+4,py+4,px+156,py+218,0xff767d82);
        for(int[] s:new int[][]{{8,8},{148,8},{8,210},{148,210}})g.fill(px+s[0],py+s[1],px+s[0]+3,py+s[1]+3,0xff3a3e41);
        g.fill(px+128,py+20,px+132,py+38,0xff1e2022);g.drawString(font,state.getInt("Price")+"¢",px+122,py+42,0xffd8d4c4,false);
        g.fill(px+12,py+16,px+118,py+62,0xffece6d2);g.fill(px+12,py+16,px+118,py+18,0xffb03a2e);
        g.drawString(font,"RIDGE SERVICE",px+16,py+21,0xff2a2420,false);
        g.drawString(font,"Sheriff  555-0117",px+16,py+33,0xff2a2420,false);
        g.drawString(font,"Dr. Hale 555-0162",px+16,py+43,0xff2a2420,false);
        g.drawString(font,"Emergency     911",px+16,py+53,0xff6a2a22,false);
        g.fill(px+30,py+70,px+130,py+92,0xff1a1c1a);
        String shown=typed.length()>3&&!typed.equals("911")?typed.substring(0,3)+"-"+typed.substring(3):typed;
        g.drawString(font,shown.isEmpty()?(state.getBoolean("Grown")?"two quarters":"a quarter"):shown,px+36,py+77,shown.isEmpty()?0xff5a6a5a:0xffb8e8a8,false);
    }
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        super.render(g,mouseX,mouseY,partial);
        int cents=state.getInt("Cents");String purse=(state.getBoolean("Grown")?"Wallet: ":"Pocket: ")+(cents>=100?cents/100:"")+"."+String.format("%02d",cents%100);
        g.drawString(font,purse,px,py+226,0xffd0ccc0,true);
        // The call, to the right of the phone: the rings, then what is said, a line at a time.
        if(callAge<0)return;int x=px+172,y=py+8,w=Math.max(80,width-x-12);
        int answered=rings*RING;
        if(callAge<=answered){g.drawString(font,"Ringing"+".".repeat(Math.min(3,callAge%RING/15)),x,y,0xffbfbfbf,true);return;}
        int shown=Math.min(lines.size(),1+(callAge-answered)/LINE);
        for(int i=0;i<shown;i++){for(FormattedCharSequence part:font.split(Component.literal(lines.get(i)),w)){g.drawString(font,part,x,y,lines.get(i).startsWith("…")||lines.get(i).equals("...")?0xff8a8a8a:0xffe8e4d8,true);y+=10;}y+=4;}
    }
}
