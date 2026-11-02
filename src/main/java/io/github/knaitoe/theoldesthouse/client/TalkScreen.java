package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.network.TalkPayload;
import io.github.knaitoe.theoldesthouse.network.TalkReplyPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * 0.4.257, the conversation panel (docs/A_PLACE_FOR_YOU_0_4_257.md): who is speaking, the exchange so far with the reader's own
 * replies, and the replies on offer, at the foot of the screen with the room still visible above it. A reply is clicked or
 * chosen by its number. Escape closes the panel and the conversation waits; nothing here decides anything.
 */
public final class TalkScreen extends Screen {
    private static final int PAD=8,GAP=3,PANEL=0xD8141210,EDGE=0xFF6a5c48,WHO=0xFFE2C98E,SAID=0xFFE8E2D6,YOU=0xFF9FB4C2,REPLY=0xFFD8D0C0,HOVER=0xFFFFF2C8,FAINT=0xFF8a8070;
    private CompoundTag state;
    private boolean sent;
    private int scroll,px,py,pw,ph;
    private final List<int[]> replyBoxes=new ArrayList<>();

    private TalkScreen(CompoundTag state){super(Component.literal(state.getString("Who")));this.state=state;}

    /** A state from the server: open the panel, bring an open one up to date, or (with no token) close it. */
    public static void accept(TalkPayload payload){
        var mc=Minecraft.getInstance();var state=payload.state();
        if(!state.contains("Token")){if(mc.screen instanceof TalkScreen)mc.setScreen(null);return;}
        if(mc.screen instanceof TalkScreen talk){talk.state=state;talk.sent=false;talk.scroll=0;}
        else if(payload.open())mc.setScreen(new TalkScreen(state));
    }

    @Override public boolean isPauseScreen(){return false;}
    @Override public void renderBackground(GuiGraphics g,int mouseX,int mouseY,float partial){}

    private List<String> replies(){var out=new ArrayList<String>();for(Tag t:state.getList("Replies",Tag.TAG_STRING))out.add(t.getAsString());return out;}

    /** Every line of the exchange, wrapped to the panel, with its colour: speakers by name, the reader's own replies indented. */
    private List<Object[]> wrapped(int width){
        var out=new ArrayList<Object[]>();
        for(Tag tag:state.getList("Lines",Tag.TAG_COMPOUND)){
            var line=(CompoundTag)tag;String speaker=line.getString("S"),text=line.getString("T");boolean you=speaker.isEmpty();
            String shown=you?"— "+text:speaker+": "+text;
            for(var seq:font.split(Component.literal(shown),width-(you?12:0)))out.add(new Object[]{seq,you?YOU:SAID,you?12:0});
        }
        return out;
    }

    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        super.render(g,mouseX,mouseY,partial);
        pw=Math.min(380,width-32);int inner=pw-PAD*2;var replies=replies();
        var replyLines=new ArrayList<List<FormattedCharSequence>>();int replyHeight=0;
        for(int i=0;i<replies.size();i++){var lines=font.split(Component.literal((i+1)+". "+replies.get(i)),inner-6);replyLines.add(lines);replyHeight+=lines.size()*(font.lineHeight+1)+GAP+2;}
        boolean ended=replies.isEmpty();if(ended)replyHeight=font.lineHeight+GAP;
        var lines=wrapped(inner);int historyRows=Mth.clamp(lines.size(),3,9),historyHeight=historyRows*(font.lineHeight+1);
        ph=PAD+font.lineHeight+GAP+historyHeight+GAP*2+replyHeight+PAD;px=(width-pw)/2;py=height-ph-12;
        g.fill(px,py,px+pw,py+ph,PANEL);g.renderOutline(px,py,pw,ph,EDGE);
        int y=py+PAD;g.drawString(font,state.getString("Who"),px+PAD,y,WHO,false);y+=font.lineHeight+GAP;
        // The exchange, its latest lines showing; the wheel scrolls back through it.
        int maxScroll=Math.max(0,lines.size()-historyRows);scroll=Mth.clamp(scroll,0,maxScroll);int first=Math.max(0,lines.size()-historyRows-scroll);
        for(int i=first;i<Math.min(lines.size(),first+historyRows);i++){var l=lines.get(i);g.drawString(font,(FormattedCharSequence)l[0],px+PAD+(int)l[2],y,(int)l[1],false);y+=font.lineHeight+1;}
        if(maxScroll>0)g.drawString(font,scroll<maxScroll?"▲":" ",px+pw-PAD-6,py+PAD,FAINT,false);
        y=py+PAD+font.lineHeight+GAP+historyHeight+GAP;g.fill(px+PAD,y,px+pw-PAD,y+1,EDGE);y+=GAP+1;
        replyBoxes.clear();
        if(ended){g.drawString(font,"Esc to step away",px+PAD,y,FAINT,false);return;}
        for(int i=0;i<replyLines.size();i++){
            int h=replyLines.get(i).size()*(font.lineHeight+1)+2;boolean over=!sent&&mouseX>=px+PAD&&mouseX<px+pw-PAD&&mouseY>=y&&mouseY<y+h;
            if(over)g.fill(px+PAD-2,y-1,px+pw-PAD+2,y+h,0x30FFF2C8);
            int ly=y+1;for(var seq:replyLines.get(i)){g.drawString(font,seq,px+PAD+3,ly,sent?FAINT:over?HOVER:REPLY,false);ly+=font.lineHeight+1;}
            replyBoxes.add(new int[]{y,y+h});y+=h+GAP;
        }
    }

    private void choose(int index){
        if(sent||index<0||index>=replies().size())return;sent=true;
        PacketDistributor.sendToServer(new TalkReplyPayload(state.getInt("Token"),index));
    }
    @Override public boolean mouseClicked(double mouseX,double mouseY,int button){
        if(button==0)for(int i=0;i<replyBoxes.size();i++){var box=replyBoxes.get(i);if(mouseX>=px+PAD&&mouseX<px+pw-PAD&&mouseY>=box[0]&&mouseY<box[1]){choose(i);return true;}}
        return super.mouseClicked(mouseX,mouseY,button);
    }
    @Override public boolean mouseScrolled(double mouseX,double mouseY,double scrollX,double scrollY){scroll+=scrollY>0?1:scrollY<0?-1:0;return true;}
    @Override public boolean keyPressed(int key,int scan,int modifiers){
        if(key>=GLFW.GLFW_KEY_1&&key<=GLFW.GLFW_KEY_9){choose(key-GLFW.GLFW_KEY_1);return true;}
        return super.keyPressed(key,scan,modifiers);
    }
}
