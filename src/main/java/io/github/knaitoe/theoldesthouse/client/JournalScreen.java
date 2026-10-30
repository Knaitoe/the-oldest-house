package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.journal.JournalPage;
import io.github.knaitoe.theoldesthouse.journal.Journals;
import io.github.knaitoe.theoldesthouse.network.JournalEditPayload;
import io.github.knaitoe.theoldesthouse.network.JournalViewPayload;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The reader's journal open in their hands: two pages at a time, written straight onto the paper. The reader only writes:
 * lines turned, mirrored, wound into spirals, cascaded, narrowed to columns or boxed are the House's to set on a page, and
 * are drawn where they lie (0.4.185, owner: "the player shouldn't have fancy formatting options; those should only exist
 * by the game"). The pages go back to the server as they are left; nothing is ever signed. There are no instructions here.
 */
public final class JournalScreen extends Screen {
    private final String name;
    private final int wear;
    private final boolean proof;
    private final Map<Integer,JournalPage> pages=new HashMap<>();
    private final Set<Integer> dirty=new HashSet<>();
    private int spread;
    private final JournalTextField[] editors=new JournalTextField[2];
    private final int[] editing={-1,-1};
    private int focused=-1;
    private long lastScratch;
    /** The spread the server was last told is open, so it is told only when that changes. */
    private int told=JournalViewPayload.SHUT;

    JournalScreen(CompoundTag journal,int spread,boolean proof){
        super(Component.literal("Journal"));
        name=journal.getString("Name");wear=journal.getInt("Wear");this.proof=proof;this.spread=Mth.clamp(spread,0,Journals.PAGES/2);
        var all=journal.getCompound("Pages");for(String key:all.getAllKeys()){try{int i=Integer.parseInt(key);if(i>=1&&i<=Journals.PAGES)pages.put(i,JournalPage.load(all.getCompound(key)));}catch(NumberFormatException ignored){}}
    }
    public static void open(CompoundTag journal){Minecraft.getInstance().setScreen(new JournalScreen(journal,journal.getInt("Spread"),false));}
    static JournalScreen proof(CompoundTag journal,int spread){return new JournalScreen(journal,spread,true);}

    int index(int side){return spread*2+side;}
    JournalPage page(int index){return pages.getOrDefault(index,JournalPage.BLANK);}
    int pageX(int side){return (width-(2*JournalPageRenderer.W+6))/2+side*(JournalPageRenderer.W+6);}
    int pageY(){return Math.max(6,(height-JournalPageRenderer.H)/2-12);}
    boolean editable(int side){int i=index(side);return i>=1&&i<=Journals.PAGES;}

    @Override protected void init(){
        tell(spread);
        for(int side=0;side<2;side++){
            // Kept while its page is open, so rebuilding the buttons never loses what is being written.
            if(editors[side]!=null&&editing[side]==index(side))continue;
            editors[side]=null;editing[side]=-1;if(!editable(side))continue;
            var field=new JournalTextField(font,JournalPageRenderer.TEXT_W);field.setCharacterLimit(JournalPage.MAX_TEXT);field.setValue(page(index(side)).text());field.seekCursor(Whence.END,0);editors[side]=field;editing[side]=index(side);
        }
        int y=pageY()+JournalPageRenderer.H+6,left=pageX(0),right=pageX(1)+JournalPageRenderer.W;
        addRenderableWidget(Button.builder(Component.literal("<"),b->turn(-1)).bounds(left,y,20,18).build()).active=spread>0;
        addRenderableWidget(Button.builder(Component.literal(">"),b->turn(1)).bounds(right-20,y,20,18).build()).active=spread<Journals.PAGES/2;
    }
    private void setPage(int side,String text){int i=index(side);pages.put(i,page(i).withWriting(text,page(i).placed()));dirty.add(i);}
    String currentText(int side){return editors[side]!=null?editors[side].value():page(index(side)).text();}

    boolean fits(JournalTextField field){return field.getLineCount()<=JournalPageRenderer.LINES&&field.value().chars().filter(c->c=='\n').count()<=JournalPage.MAX_NEWLINES&&field.value().length()<=JournalPage.MAX_TEXT;}

    private void turn(int by){
        int next=Mth.clamp(spread+by,0,Journals.PAGES/2);if(next==spread)return;
        send();spread=next;focused=-1;tell(spread);
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN,1));
        rebuildWidgets();
    }
    /** Every page changed since it was last sent goes back to the server, as it is now. */
    private void send(){
        for(int side=0;side<2;side++)if(editors[side]!=null){int i=index(side);if(!editors[side].value().equals(page(i).text()))setPage(side,editors[side].value());}
        if(proof||Minecraft.getInstance().getConnection()==null){dirty.clear();return;}
        for(int i:dirty){var t=new CompoundTag();t.putString("Text",page(i).text());PacketDistributor.sendToServer(new JournalEditPayload(i,t));}
        dirty.clear();
    }
    @Override public void removed(){send();tell(JournalViewPayload.SHUT);super.removed();}
    private void tell(int at){if(at==told)return;told=at;if(!proof&&Minecraft.getInstance().getConnection()!=null)PacketDistributor.sendToServer(new JournalViewPayload(at));}
    @Override public boolean isPauseScreen(){return false;}

    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){g.fill(0,0,width,height,0xE01E1A16);}
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        super.render(g,mouseX,mouseY,partial);
        int top=pageY();
        for(int side=0;side<2;side++){
            int x=pageX(side),i=index(side);
            if(i>Journals.PAGES){g.fill(x,top,x+JournalPageRenderer.W,top+JournalPageRenderer.H,0xFF2A231C);continue;}
            var page=page(i);
            JournalPageRenderer.paper(g,x,top,side==0,i==0,wear,i);
            if(i==0){JournalPageRenderer.cover(g,font,x,top,name);continue;}
            JournalPageRenderer.rubbings(g,font,x,top,page);
            if(editors[side]!=null)editor(g,side,x,top);else JournalPageRenderer.writing(g,font,x,top,page.text());
            JournalPageRenderer.notes(g,font,x,top,page,currentText(side));
            // Placed lines stay on their own page, however far they are turned or dragged.
            g.enableScissor(x,top,x+JournalPageRenderer.W,top+JournalPageRenderer.H);
            for(var run:page.placed())JournalPageRenderer.placed(g,font,x,top,run,false);
            g.disableScissor();
            JournalPageRenderer.header(g,font,x,top,page);JournalPageRenderer.number(g,font,x,top,i);
        }
    }
    /** The writing as it is being written: graphite on the paper, with the selection and a blinking caret. */
    private void editor(GuiGraphics g,int side,int x,int y){
        var field=editors[side];String value=field.value();var lines=field.lines();
        int tx=x+JournalPageRenderer.TEXT_X,ty=y+JournalPageRenderer.TEXT_Y;
        if(field.hasSelection()){int[] sel=field.selection();
            for(int r=0;r<lines.size();r++){int[] l=lines.get(r);int a=Math.max(sel[0],l[0]),b=Math.min(sel[1],l[1]);if(a>=b)continue;
                g.fill(tx+font.width(value.substring(l[0],a)),ty+r*JournalPageRenderer.LINE-1,tx+font.width(value.substring(l[0],b)),ty+r*JournalPageRenderer.LINE+8,JournalPageRenderer.SELECT);}}
        io.github.knaitoe.theoldesthouse.house.HouseText.enterLiterature();
        try{for(int r=0;r<lines.size()&&r<JournalPageRenderer.LINES;r++){int[] l=lines.get(r);g.drawString(font,value.substring(l[0],l[1]),tx,ty+r*JournalPageRenderer.LINE,JournalPageRenderer.INK,false);}}
        finally{io.github.knaitoe.theoldesthouse.house.HouseText.leaveLiterature();}
        if(focused==side&&(Util.getMillis()/500)%2==0){
            int line=field.getLineAtCursor();int[] l=field.line(line);int cx=tx+font.width(value.substring(l[0],Math.min(field.cursor(),value.length())));int cy=ty+line*JournalPageRenderer.LINE;
            g.fill(cx,cy-1,cx+1,cy+9,JournalPageRenderer.INK);
        }
    }

    int sideAt(double mx,double my){for(int side=0;side<2;side++){int x=pageX(side),y=pageY();if(mx>=x&&mx<x+JournalPageRenderer.W&&my>=y&&my<y+JournalPageRenderer.H)return side;}return -1;}
    @Override public boolean mouseClicked(double mx,double my,int button){
        if(super.mouseClicked(mx,my,button))return true;
        int side=sideAt(mx,my);if(side<0||!editable(side)){focused=-1;return false;}
        double px=mx-pageX(side),py=my-pageY();
        focused=side;var field=editors[side];
        if(field!=null){field.setSelecting(false);field.seekCursorToPoint(px-JournalPageRenderer.TEXT_X,py-JournalPageRenderer.TEXT_Y);}
        rebuildWidgets();return true;
    }
    @Override public boolean mouseDragged(double mx,double my,int button,double dx,double dy){
        if(focused>=0&&editors[focused]!=null&&button==0){editors[focused].setSelecting(true);editors[focused].seekCursorToPoint(mx-pageX(focused)-JournalPageRenderer.TEXT_X,my-pageY()-JournalPageRenderer.TEXT_Y);editors[focused].setSelecting(false);return true;}
        return super.mouseDragged(mx,my,button,dx,dy);
    }
    @Override public boolean mouseScrolled(double mx,double my,double sx,double sy){
        if(sy!=0&&focused<0){turn(sy>0?-1:1);return true;}
        return super.mouseScrolled(mx,my,sx,sy);
    }
    @Override public boolean keyPressed(int key,int scan,int mods){
        if(focused>=0&&editors[focused]!=null&&key!=256){
            var field=editors[focused];String before=field.value();int cursor=field.cursor();
            if(field.keyPressed(key)){if(!fits(field)){field.setValue(before);field.seekCursor(Whence.ABSOLUTE,cursor);}else if(!before.equals(field.value()))scratch();return true;}
        }
        if(key==256&&focused>=0){focused=-1;rebuildWidgets();return true;}
        return super.keyPressed(key,scan,mods);
    }
    @Override public boolean charTyped(char c,int mods){
        if(focused>=0&&editors[focused]!=null&&StringUtil.isAllowedChatCharacter(c)){
            var field=editors[focused];String before=field.value();int cursor=field.cursor();field.insertText(Character.toString(c));
            if(!fits(field)){field.setValue(before);field.seekCursor(Whence.ABSOLUTE,cursor);}else scratch();
            return true;
        }
        return super.charTyped(c,mods);
    }
    /** The pencil on the paper, now and then while the reader writes. */
    private void scratch(){
        long now=Util.getMillis();if(now-lastScratch<1400)return;lastScratch=now;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT,1.6F,.12F));
    }
}
