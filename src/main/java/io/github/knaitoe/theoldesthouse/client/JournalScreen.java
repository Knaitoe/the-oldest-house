package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.journal.JournalPage;
import io.github.knaitoe.theoldesthouse.journal.Journals;
import io.github.knaitoe.theoldesthouse.network.JournalEditPayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The reader's journal open in their hands: two pages at a time, written straight onto the paper. A line can be lifted out
 * of the writing and set anywhere on the page, turned, mirrored, wound into a spiral, cascaded down the page, narrowed to a
 * column or boxed. The pages go back to the server as they are left; nothing is ever signed. There are no instructions here.
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
    /** The selected lifted line: which side of the spread and which run on that page; -1 for none. */
    private int selSide=-1,selRun=-1;
    private boolean dragging;private float dragX,dragY;
    private long lastScratch;

    JournalScreen(CompoundTag journal,int spread,boolean proof){
        super(Component.literal("Journal"));
        name=journal.getString("Name");wear=journal.getInt("Wear");this.proof=proof;this.spread=Mth.clamp(spread,0,Journals.PAGES/2);
        var all=journal.getCompound("Pages");for(String key:all.getAllKeys()){try{int i=Integer.parseInt(key);if(i>=1&&i<=Journals.PAGES)pages.put(i,JournalPage.load(all.getCompound(key)));}catch(NumberFormatException ignored){}}
    }
    public static void open(CompoundTag journal){Minecraft.getInstance().setScreen(new JournalScreen(journal,0,false));}
    static JournalScreen proof(CompoundTag journal,int spread,int selectSide,int selectRun){var s=new JournalScreen(journal,spread,true);s.selSide=selectSide;s.selRun=selectRun;return s;}

    int index(int side){return spread*2+side;}
    JournalPage page(int index){return pages.getOrDefault(index,JournalPage.BLANK);}
    int pageX(int side){return (width-(2*JournalPageRenderer.W+6))/2+side*(JournalPageRenderer.W+6);}
    int pageY(){return Math.max(6,(height-JournalPageRenderer.H)/2-12);}
    boolean editable(int side){int i=index(side);return i>=1&&i<=Journals.PAGES;}

    @Override protected void init(){
        for(int side=0;side<2;side++){
            // Kept while its page is open, so rebuilding the buttons never loses what is being written.
            if(editors[side]!=null&&editing[side]==index(side))continue;
            editors[side]=null;editing[side]=-1;if(!editable(side))continue;
            var field=new JournalTextField(font,JournalPageRenderer.TEXT_W);field.setCharacterLimit(JournalPage.MAX_TEXT);field.setValue(page(index(side)).text());field.seekCursor(Whence.END,0);editors[side]=field;editing[side]=index(side);
        }
        int y=pageY()+JournalPageRenderer.H+6,left=pageX(0),right=pageX(1)+JournalPageRenderer.W;
        addRenderableWidget(Button.builder(Component.literal("<"),b->turn(-1)).bounds(left,y,20,18).build()).active=spread>0;
        addRenderableWidget(Button.builder(Component.literal(">"),b->turn(1)).bounds(right-20,y,20,18).build()).active=spread<Journals.PAGES/2;
        if(selected()==null){
            addRenderableWidget(Button.builder(Component.literal("Lift line"),b->lift()).bounds(width/2-40,y,80,18).build()).active=focused>=0&&page(index(focused)).placed().size()<JournalPage.MAX_PLACED;
        }else{
            var run=selected();int x=width/2-133;
            addRenderableWidget(Button.builder(Component.literal(shapeName(run.shape())),b->change(r->r.with(r.x(),r.y(),r.angle(),r.size(),r.mirrored(),r.shape().next()))).bounds(x,y,64,18).build());
            addRenderableWidget(Button.builder(Component.literal("Mirror"),b->change(r->r.with(r.x(),r.y(),r.angle(),r.size(),!r.mirrored(),r.shape()))).bounds(x+66,y,48,18).build());
            addRenderableWidget(Button.builder(Component.literal("-"),b->change(r->r.with(r.x(),r.y(),r.angle(),r.size()/1.15F,r.mirrored(),r.shape()))).bounds(x+116,y,18,18).build());
            addRenderableWidget(Button.builder(Component.literal("+"),b->change(r->r.with(r.x(),r.y(),r.angle(),r.size()*1.15F,r.mirrored(),r.shape()))).bounds(x+136,y,18,18).build());
            addRenderableWidget(Button.builder(Component.literal("Back into the page"),b->drop()).bounds(x+156,y,110,18).build());
        }
    }
    static String shapeName(JournalPage.Shape shape){return switch(shape){case LINE->"A line";case SPIRAL->"A spiral";case CASCADE->"A cascade";case COLUMN->"A column";case BOX->"A box";};}

    JournalPage.Placed selected(){
        if(selSide<0||!editable(selSide))return null;var runs=page(index(selSide)).placed();return selRun>=0&&selRun<runs.size()?runs.get(selRun):null;
    }
    private void select(int side,int run){selSide=side;selRun=run;if(side>=0)focused=-1;rebuildWidgets();}
    private void setPage(int side,String text,List<JournalPage.Placed> runs){int i=index(side);pages.put(i,page(i).withWriting(text,runs));dirty.add(i);}
    private void change(java.util.function.UnaryOperator<JournalPage.Placed> how){
        var run=selected();if(run==null)return;var runs=new ArrayList<>(page(index(selSide)).placed());runs.set(selRun,how.apply(run));
        setPage(selSide,currentText(selSide),runs);rebuildWidgets();
    }
    String currentText(int side){return editors[side]!=null?editors[side].value():page(index(side)).text();}

    /** The wrapped line at the cursor comes out of the writing and lies a little askew where it was, selected. */
    private void lift(){
        if(focused<0||editors[focused]==null)return;var field=editors[focused];
        var runs=new ArrayList<>(page(index(focused)).placed());if(runs.size()>=JournalPage.MAX_PLACED)return;
        int line=field.getLineAtCursor();int[] at=field.line(line);String value=field.value();String words=value.substring(at[0],at[1]).strip();
        if(words.isEmpty())return;if(words.length()>JournalPage.MAX_RUN)words=words.substring(0,JournalPage.MAX_RUN);
        int end=at[1];if(end<value.length()&&value.charAt(end)=='\n')end++;
        field.setValue(value.substring(0,at[0])+value.substring(end));field.seekCursor(Whence.ABSOLUTE,Math.min(at[0],field.value().length()));
        float x=(JournalPageRenderer.TEXT_X+2F)/JournalPageRenderer.W,y=(JournalPageRenderer.TEXT_Y+line*JournalPageRenderer.LINE+4F)/JournalPageRenderer.H;
        runs.add(new JournalPage.Placed(words,JournalPage.Shape.LINE,x,y,-6,1,false));
        int side=focused;setPage(side,field.value(),runs);select(side,runs.size()-1);
    }
    /** A placed line goes back to the end of the writing, if there is room. */
    private void drop(){
        var run=selected();if(run==null)return;int side=selSide;var field=editors[side];if(field==null)return;
        String before=field.value();String joined=before.isEmpty()?run.text():before+(before.endsWith("\n")?"":"\n")+run.text();
        field.setValue(joined);if(!fits(field)){field.setValue(before);return;}
        var runs=new ArrayList<>(page(index(side)).placed());runs.remove(selRun);setPage(side,field.value(),runs);select(-1,-1);
    }
    boolean fits(JournalTextField field){return field.getLineCount()<=JournalPageRenderer.LINES&&field.value().chars().filter(c->c=='\n').count()<=JournalPage.MAX_NEWLINES&&field.value().length()<=JournalPage.MAX_TEXT;}

    private void turn(int by){
        int next=Mth.clamp(spread+by,0,Journals.PAGES/2);if(next==spread)return;
        send();spread=next;focused=-1;selSide=-1;selRun=-1;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN,1));
        rebuildWidgets();
    }
    /** Every page changed since it was last sent goes back to the server, as it is now. */
    private void send(){
        for(int side=0;side<2;side++)if(editors[side]!=null){int i=index(side);if(!editors[side].value().equals(page(i).text()))setPage(side,editors[side].value(),page(i).placed());}
        if(proof||Minecraft.getInstance().getConnection()==null){dirty.clear();return;}
        for(int i:dirty){var p=page(i);var t=new CompoundTag();t.putString("Text",p.text());var runs=new ListTag();for(var r:p.placed()){var c=new CompoundTag();c.putString("Text",r.text());c.putString("Shape",r.shape().id());c.putFloat("X",r.x());c.putFloat("Y",r.y());c.putFloat("Angle",r.angle());c.putFloat("Size",r.size());if(r.mirrored())c.putBoolean("Mirrored",true);runs.add(c);}t.put("Placed",runs);
            PacketDistributor.sendToServer(new JournalEditPayload(i,t));}
        dirty.clear();
    }
    @Override public void removed(){send();super.removed();}
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
            if(editors[side]!=null)editor(g,side,x,top);else JournalPageRenderer.writing(g,font,x,top,page.text());
            // Placed lines stay on their own page, however far they are turned or dragged.
            g.enableScissor(x,top,x+JournalPageRenderer.W,top+JournalPageRenderer.H);
            var runs=page.placed();for(int r=0;r<runs.size();r++)JournalPageRenderer.placed(g,font,x,top,runs.get(r),side==selSide&&r==selRun);
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
        int side=sideAt(mx,my);if(side<0||!editable(side)){select(-1,-1);focused=-1;return false;}
        double px=mx-pageX(side),py=my-pageY();var runs=page(index(side)).placed();
        for(int r=runs.size()-1;r>=0;r--){var b=JournalPageRenderer.bounds(font,runs.get(r));
            if(px>=b[0]-2&&px<=b[2]+2&&py>=b[1]-2&&py<=b[3]+2){select(side,r);dragging=button==0;dragX=(float)(px-runs.get(r).x()*JournalPageRenderer.W);dragY=(float)(py-runs.get(r).y()*JournalPageRenderer.H);return true;}}
        if(selSide>=0)select(-1,-1);
        focused=side;var field=editors[side];
        if(field!=null){field.setSelecting(false);field.seekCursorToPoint(px-JournalPageRenderer.TEXT_X,py-JournalPageRenderer.TEXT_Y);}
        rebuildWidgets();return true;
    }
    @Override public boolean mouseDragged(double mx,double my,int button,double dx,double dy){
        if(dragging&&selected()!=null){
            float nx=(float)((mx-pageX(selSide)-dragX)/JournalPageRenderer.W),ny=(float)((my-pageY()-dragY)/JournalPageRenderer.H);
            var run=selected();var runs=new ArrayList<>(page(index(selSide)).placed());runs.set(selRun,run.with(nx,ny,run.angle(),run.size(),run.mirrored(),run.shape()));setPage(selSide,currentText(selSide),runs);return true;
        }
        if(focused>=0&&editors[focused]!=null&&button==0){editors[focused].setSelecting(true);editors[focused].seekCursorToPoint(mx-pageX(focused)-JournalPageRenderer.TEXT_X,my-pageY()-JournalPageRenderer.TEXT_Y);editors[focused].setSelecting(false);return true;}
        return super.mouseDragged(mx,my,button,dx,dy);
    }
    @Override public boolean mouseReleased(double mx,double my,int button){dragging=false;return super.mouseReleased(mx,my,button);}
    @Override public boolean mouseScrolled(double mx,double my,double sx,double sy){
        if(selected()!=null&&sy!=0){change(r->r.with(r.x(),r.y(),r.angle()+(sy>0?-15:15),r.size(),r.mirrored(),r.shape()));return true;}
        if(sy!=0&&selected()==null&&focused<0){turn(sy>0?-1:1);return true;}
        return super.mouseScrolled(mx,my,sx,sy);
    }
    @Override public boolean keyPressed(int key,int scan,int mods){
        if(selected()!=null){
            if(key==256){select(-1,-1);return true;}
            if(key==259||key==261){drop();return true;}
            float step=1F/JournalPageRenderer.H;
            if(key==262||key==263||key==264||key==265){float dx=key==262?step:key==263?-step:0,dy=key==264?step:key==265?-step:0;change(r->r.with(r.x()+dx,r.y()+dy,r.angle(),r.size(),r.mirrored(),r.shape()));return true;}
        }
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
