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
 * by the game"). The pages go back to the server as they are left; nothing is ever signed.
 * 0.4.199 (journal/TornPages): a written page held by its corner, a right-click held down, lifts and tears out; what is left is
 * the stub, and a right-click on the stub takes the page back, wherever it has got to. 0.4.201: a written page's corner curls
 * under the pointer, and the corner itself carries the only instruction here, how to tear it out.
 */
public final class JournalScreen extends Screen {
    private final String name;
    private final int wear;
    private final boolean proof;
    private final Map<Integer,JournalPage> pages=new HashMap<>();
    private final Set<Integer> dirty=new HashSet<>();
    private final Set<Integer> torn=new HashSet<>();
    /** The pages written since the last death before this one: they took the worst of it (journal/PostMortem). */
    private final Set<Integer> soaked=new HashSet<>();
    /** The page held by its corner to be torn out, and since when. */
    private int holding=-1;private long held;
    static final long HOLD_MS=900;
    private int spread;
    private final JournalTextField[] editors=new JournalTextField[2];
    private final int[] editing={-1,-1};
    private int focused=-1;
    private long lastScratch;
    /** The spread the server was last told is open, so it is told only when that changes. */
    private int told=JournalViewPayload.SHUT;
    /**
     * 0.4.268 (the owner: "A ribbon bookmark, page-number jump, and search through the player's own writing"): the page the ribbon
     * lies in (0 for none), and the second row's find and page fields, kept across the screen's rebuilds.
     */
    private int ribbon;
    private net.minecraft.client.gui.components.EditBox finder,jumper;
    private String findText="",jumpText="";
    private boolean findFocus,jumpFocus,notFound,controls;
    static final int RIBBON=0xFF7A2430,RIBBON_DARK=0xFF54161F;

    JournalScreen(CompoundTag journal,int spread,boolean proof){
        super(Component.literal("Journal"));
        name=journal.getString("Name");wear=journal.getInt("Wear");this.proof=proof;this.spread=Mth.clamp(spread,0,Journals.PAGES/2);
        var all=journal.getCompound("Pages");for(String key:all.getAllKeys()){try{int i=Integer.parseInt(key);if(i>=1&&i<=Journals.PAGES)pages.put(i,JournalPage.load(all.getCompound(key)));}catch(NumberFormatException ignored){}}
        for(int i:journal.getIntArray("Torn"))if(i>=1&&i<=Journals.PAGES)torn.add(i);
        for(int i:journal.getIntArray("Soaked"))if(i>=1&&i<=Journals.PAGES)soaked.add(i);
        ribbon=Mth.clamp(journal.getInt("Ribbon"),0,Journals.PAGES);
    }
    public static void open(CompoundTag journal){Minecraft.getInstance().setScreen(new JournalScreen(journal,journal.getInt("Spread"),false));}
    static JournalScreen proof(CompoundTag journal,int spread){return new JournalScreen(journal,spread,true);}
    /** 0.4.266: the proof's journal turned to what was said, with these kept conversations. */
    static JournalScreen proofHeard(CompoundTag journal,CompoundTag history){var j=new JournalScreen(journal,0,true);j.heardMode=true;j.pendingHeard=history;return j;}
    /** 0.4.268: the proof's journal with its second row showing, searched for {@code query} from the first spread. */
    static JournalScreen proofFind(CompoundTag journal,String query){var j=new JournalScreen(journal,0,true);j.controls=true;j.findText=query;j.searchOnOpen=true;return j;}
    private boolean searchOnOpen;
    /** The open spread (for the proofs). */
    int spreadOpen(){return spread;}

    int index(int side){return spread*2+side;}
    JournalPage page(int index){return pages.getOrDefault(index,JournalPage.BLANK);}
    int pageX(int side){return (width-(2*JournalPageRenderer.W+6))/2+side*(JournalPageRenderer.W+6);}
    /** The pages' top, leaving room under them for the two rows of controls even on the smallest screen. */
    int pageY(){return Math.max(4,(height-(JournalPageRenderer.H+44))/2);}
    /** The first row of controls under the pages, and the second (0.4.268). */
    int rowY(){return pageY()+JournalPageRenderer.H+4;}
    boolean editable(int side){int i=index(side);return i>=1&&i<=Journals.PAGES&&!torn.contains(i);}

    @Override protected void init(){
        if(heardMode){initHeard();return;}
        tell(spread);
        for(int side=0;side<2;side++){
            // Kept while its page is open, so rebuilding the buttons never loses what is being written.
            if(editors[side]!=null&&editing[side]==index(side))continue;
            editors[side]=null;editing[side]=-1;if(!editable(side))continue;
            var field=new JournalTextField(font,JournalPageRenderer.TEXT_W);field.setCharacterLimit(JournalPage.MAX_TEXT);field.setValue(page(index(side)).text());field.seekCursor(Whence.END,0);editors[side]=field;editing[side]=index(side);
        }
        int y=rowY(),left=pageX(0),right=pageX(1)+JournalPageRenderer.W;
        addRenderableWidget(Button.builder(Component.literal("<"),b->turn(-1)).bounds(left,y,20,18).build()).active=spread>0;
        addRenderableWidget(Button.builder(Component.literal(">"),b->turn(1)).bounds(right-20,y,20,18).build()).active=spread<Journals.PAGES/2;
        // 0.4.266: the ribbon to what was said to this reader (labyrinth/TalkHistory), printed, behind their own pages.
        if(!proof)addRenderableWidget(Button.builder(Component.literal("What was said"),b->showHeard(true)).bounds((left+right)/2-50,y,100,18).build());
        // 0.4.268: the ribbon, a jump to a page by its number, and a search through the reader's own writing.
        if(!proof||controls){
            int y2=y+20;boolean here=ribbon>0&&ribbon/2==spread;
            addRenderableWidget(Button.builder(Component.literal(here?"Ribbon out":"Ribbon here"),b->ribbon(here?0:Math.max(1,spread*2))).bounds(left,y2,62,16).build());
            addRenderableWidget(Button.builder(Component.literal("To ribbon"),b->turnTo(ribbon/2)).bounds(left+64,y2,58,16).build()).active=ribbon>0&&!here;
            finder=new net.minecraft.client.gui.components.EditBox(font,left+126,y2,right-left-126-40,16,Component.literal("Find"));
            finder.setMaxLength(48);finder.setValue(findText);finder.setHint(Component.literal("Find in your writing").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
            finder.setTextColor(notFound?0xFFC87070:0xFFE0E0E0);finder.setResponder(t->{if(!t.equals(findText)){findText=t;notFound=false;finder.setTextColor(0xFFE0E0E0);}});addRenderableWidget(finder);
            jumper=new net.minecraft.client.gui.components.EditBox(font,right-36,y2,36,16,Component.literal("Page"));
            jumper.setMaxLength(2);jumper.setFilter(t->t.chars().allMatch(Character::isDigit));jumper.setValue(jumpText);jumper.setHint(Component.literal("Page").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
            jumper.setResponder(t->jumpText=t);addRenderableWidget(jumper);
            if(findFocus)setFocused(finder);else if(jumpFocus)setFocused(jumper);
        }else{finder=null;jumper=null;}
        if(searchOnOpen){searchOnOpen=false;find();}
    }

    // ------------------------------------------------------------------ 0.4.268, the ribbon, the page number and the search
    /** Lays the ribbon in a page (0 takes it out); the server keeps it with the journal. */
    private void ribbon(int page){
        ribbon=Mth.clamp(page,0,Journals.PAGES);
        if(!proof&&Minecraft.getInstance().getConnection()!=null)PacketDistributor.sendToServer(new io.github.knaitoe.theoldesthouse.network.JournalRibbonPayload(ribbon));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN,1.4F,.5F));
        rebuildWidgets();
    }
    /** Turns straight to a spread. */
    private void turnTo(int target){turn(Mth.clamp(target,0,Journals.PAGES/2)-spread);}
    /** The page number typed in the second row: its spread, if the journal has that page. */
    private void jump(){
        int n;try{n=Integer.parseInt(jumpText);}catch(NumberFormatException e){return;}
        jumpText="";jumpFocus=false;if(n>=1&&n<=Journals.PAGES)turnTo(n/2);else rebuildWidgets();
    }
    /** What a page holds in the reader's own hand, as it stands on the screen. */
    private String writing(int i){for(int side=0;side<2;side++)if(index(side)==i&&editors[side]!=null)return editors[side].value();return page(i).text();}
    /**
     * The next page after this spread whose writing holds what was typed, wrapping round to the start; only the reader's own writing
     * is searched, not the House's lines or the other hand's. The spread turns to it and the words are picked out; nothing found turns
     * the field's text a dull red.
     */
    private void find(){
        String q=findText.strip().toLowerCase(java.util.Locale.ROOT);if(q.isEmpty())return;
        int from=spread*2+2;
        for(int k=0;k<Journals.PAGES;k++){
            int i=1+Math.floorMod(from-1+k,Journals.PAGES);if(torn.contains(i))continue;
            int at=writing(i).toLowerCase(java.util.Locale.ROOT).indexOf(q);if(at<0)continue;
            findFocus=true;notFound=false;
            if(i/2!=spread)turnTo(i/2);else rebuildWidgets();
            var field=editors[i%2];if(field!=null&&editing[i%2]==i){field.seekCursor(Whence.ABSOLUTE,at);field.setSelecting(true);field.seekCursor(Whence.ABSOLUTE,at+q.length());field.setSelecting(false);}
            return;
        }
        notFound=true;findFocus=true;rebuildWidgets();
    }
    /** The ribbon: down the page it lies in, out past its foot; elsewhere in the book, only its end shows at the spine. */
    private void drawRibbon(GuiGraphics g){
        if(ribbon<=0)return;int top=pageY(),foot=top+JournalPageRenderer.H;
        if(ribbon/2==spread){
            int side=ribbon%2,x=side==0?pageX(0)+JournalPageRenderer.W-12:pageX(1)+7;
            g.fill(x,top-3,x+5,foot+3,RIBBON);g.fill(x+4,top-3,x+5,foot+3,RIBBON_DARK);
            g.fill(x,foot+3,x+2,foot+5,RIBBON);g.fill(x+3,foot+3,x+5,foot+5,RIBBON);
        }else{int x=pageX(0)+JournalPageRenderer.W+1;g.fill(x,foot-6,x+4,foot+3,RIBBON);g.fill(x+3,foot-6,x+4,foot+3,RIBBON_DARK);}
    }

    // ------------------------------------------------------------------ 0.4.266, what was said
    /**
     * The owner's second quality-of-life note ("Let players revisit conversations they actually heard, especially their own promises and
     * answers"), and then: "i'm okay with the conversation history being integrated into the player's journal". Behind the ribbon,
     * every conversation the panel showed this reader, newest first: who, the day and where, then each line, the reader's own replies
     * set in. Printed small in plain type, not in the reader's hand; it takes none of their pages and cannot be written on or torn.
     */
    private boolean heardMode,heardAsked;
    private int heardSpread;
    private java.util.List<java.util.List<Object[]>> heardPages;
    static final float SMALL=.75F;
    static final int HEARD_ROWS=18,HEARD_LINE=12,YOU_INK=0xFF2E4A66;
    public static void heard(io.github.knaitoe.theoldesthouse.network.TalkHistoryPayload payload){if(Minecraft.getInstance().screen instanceof JournalScreen j)j.loadHeard(payload.history());}
    private void showHeard(boolean on){
        if(on==heardMode)return;
        if(on){send();focused=-1;holding=-1;
            if(!heardAsked&&!proof&&Minecraft.getInstance().getConnection()!=null){heardAsked=true;PacketDistributor.sendToServer(new io.github.knaitoe.theoldesthouse.network.TalkHistoryRequestPayload());}}
        heardMode=on;rebuildWidgets();
    }
    private CompoundTag pendingHeard;
    private void initHeard(){
        if(pendingHeard!=null){var h=pendingHeard;pendingHeard=null;layHeard(h);}
        int y=rowY(),left=pageX(0),right=pageX(1)+JournalPageRenderer.W;int last=heardPages==null?0:Math.max(0,(heardPages.size()-1)/2);
        heardSpread=Mth.clamp(heardSpread,0,last);
        addRenderableWidget(Button.builder(Component.literal("<"),b->turnHeard(-1)).bounds(left,y,20,18).build()).active=heardSpread>0;
        addRenderableWidget(Button.builder(Component.literal(">"),b->turnHeard(1)).bounds(right-20,y,20,18).build()).active=heardSpread<last;
        addRenderableWidget(Button.builder(Component.literal("My pages"),b->showHeard(false)).bounds((left+right)/2-50,y,100,18).build());
    }
    private void turnHeard(int by){
        int last=heardPages==null?0:Math.max(0,(heardPages.size()-1)/2);int next=Mth.clamp(heardSpread+by,0,last);if(next==heardSpread)return;heardSpread=next;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN,1));rebuildWidgets();
    }
    /** The reader's kept conversations, laid out into printed pages: newest first, a conversation's heading never left alone at a page's foot. */
    void loadHeard(CompoundTag history){layHeard(history);if(heardMode)rebuildWidgets();}
    private void layHeard(CompoundTag history){
        var talks=history.getList("Talks",net.minecraft.nbt.Tag.TAG_COMPOUND);int wrap=(int)(JournalPageRenderer.TEXT_W/SMALL);
        var pagesOut=new java.util.ArrayList<java.util.List<Object[]>>();var page=new java.util.ArrayList<Object[]>();
        page.add(new Object[]{Component.literal("What was said").getVisualOrderText(),JournalPageRenderer.FAINT,0});page.add(null);
        for(int t=talks.size()-1;t>=0;t--){
            var talk=talks.getCompound(t);var block=new java.util.ArrayList<Object[]>();
            String where=talk.getString("Where");String head=talk.getString("Who")+" \u00b7 day "+talk.getLong("Day")+(where.isEmpty()?"":" \u00b7 "+where);
            for(var seq:font.split(Component.literal(head),wrap))block.add(new Object[]{seq,JournalPageRenderer.FAINT,0});int heading=block.size();
            for(var tag:talk.getList("Lines",net.minecraft.nbt.Tag.TAG_COMPOUND)){
                var line=(CompoundTag)tag;String speaker=line.getString("S"),text=line.getString("T");boolean you=speaker.isEmpty();
                for(var seq:font.split(Component.literal(you?"\u2014 "+text:speaker+": "+text),wrap-(you?8:0)))block.add(new Object[]{seq,you?YOU_INK:JournalPageRenderer.INK,you?8:0});
            }
            block.add(null);
            for(int i=0;i<block.size();i++){
                // A heading starts a page rather than sitting alone at the foot of one.
                if(i==0&&page.size()+heading+1>HEARD_ROWS&&!page.isEmpty()){pagesOut.add(page);page=new java.util.ArrayList<>();}
                if(page.size()>=HEARD_ROWS){pagesOut.add(page);page=new java.util.ArrayList<>();}
                if(block.get(i)==null&&page.isEmpty())continue;
                page.add(block.get(i));
            }
        }
        if(talks.isEmpty())page.add(new Object[]{Component.literal("Nothing has been said to you yet.").getVisualOrderText(),JournalPageRenderer.FAINT,0});
        pagesOut.add(page);heardPages=pagesOut;
    }
    /** For tests: the printed pages as text, one string a page. */
    java.util.List<String> heardText(){
        var out=new java.util.ArrayList<String>();if(heardPages==null)return out;
        for(var page:heardPages){var b=new StringBuilder();for(var row:page){if(row==null){b.append('\n');continue;}((net.minecraft.util.FormattedCharSequence)row[0]).accept((i,style,cp)->{b.appendCodePoint(cp);return true;});b.append('\n');}out.add(b.toString());}
        return out;
    }
    private void renderHeard(GuiGraphics g,int mouseX,int mouseY,float partial){
        super.render(g,mouseX,mouseY,partial);
        int top=pageY();
        for(int side=0;side<2;side++){
            int x=pageX(side),n=heardSpread*2+side;
            JournalPageRenderer.paper(g,x,top,side==0,false,wear,1000+n);
            if(heardPages==null){if(side==0)g.drawString(font,"\u2026",x+JournalPageRenderer.TEXT_X,top+JournalPageRenderer.TEXT_Y,JournalPageRenderer.FAINT,false);continue;}
            if(n>=heardPages.size())continue;
            var pose=g.pose();pose.pushPose();pose.translate(x+JournalPageRenderer.TEXT_X,top+JournalPageRenderer.TEXT_Y,0);pose.scale(SMALL,SMALL,1);
            int row=0;for(var r:heardPages.get(n)){if(r!=null)g.drawString(font,(net.minecraft.util.FormattedCharSequence)r[0],(int)r[2],row*HEARD_LINE,(int)r[1],false);row++;}
            pose.popPose();
            JournalPageRenderer.number(g,font,x,top,n+1);
        }
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
    /** 0.4.265: unsent writing also goes back while the screen is open, a second after the hand stops and every five seconds while it goes on. */
    private final io.github.knaitoe.theoldesthouse.journal.WritingClock clock=new io.github.knaitoe.theoldesthouse.journal.WritingClock();
    @Override public void tick(){
        super.tick();
        String[] now=new String[2],sent=new String[2];
        for(int side=0;side<2;side++)if(editors[side]!=null){now[side]=editors[side].value();sent[side]=page(index(side)).text();}
        if(clock.tick(now,sent))send();
    }
    /** Every page changed since it was last sent goes back to the server, as it is now. */
    private void send(){clock.sent();sendNow();}
    private void sendNow(){
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
        if(heardMode){renderHeard(g,mouseX,mouseY,partial);return;}
        super.render(g,mouseX,mouseY,partial);
        int top=pageY();boolean hint=false;
        for(int side=0;side<2;side++){
            int x=pageX(side),i=index(side);
            if(i>Journals.PAGES){g.fill(x,top,x+JournalPageRenderer.W,top+JournalPageRenderer.H,0xFF2A231C);continue;}
            if(torn.contains(i)){JournalPageRenderer.stub(g,x,top,side==0,i);continue;}
            var page=page(i);
            JournalPageRenderer.paper(g,x,top,side==0,i==0,soaked.contains(i)?Math.min(Journals.WORN,wear+1):wear,i);
            if(i==0){JournalPageRenderer.cover(g,font,x,top,name);continue;}
            JournalPageRenderer.rubbings(g,font,x,top,page);
            if(editors[side]!=null)editor(g,side,x,top);else JournalPageRenderer.writing(g,font,x,top,page.text());
            JournalPageRenderer.notes(g,font,x,top,page,currentText(side));
            // Placed lines stay on their own page, however far they are turned or dragged.
            g.enableScissor(x,top,x+JournalPageRenderer.W,top+JournalPageRenderer.H);
            for(var run:page.placed())JournalPageRenderer.placed(g,font,x,top,run,false);
            g.disableScissor();
            JournalPageRenderer.header(g,font,x,top,page);JournalPageRenderer.number(g,font,x,top,i);
            if(holding==side)lift(g,side,x,top);
            // 0.4.201 (owner: "I'm not seeing the ability to tear a page free"): a written page's corner curls up under the
            // pointer, and on the corner itself the hand is told how.
            else if(!proof&&sideAt(mouseX,mouseY)==side&&!currentText(side).isBlank()){curl(g,side,x,top,6);hint|=atCorner(side,mouseX,mouseY);}
        }
        drawRibbon(g);
        if(hint)g.renderTooltip(font,net.minecraft.network.chat.Component.literal("Hold right-click to tear it out"),mouseX,mouseY);
    }
    /** Whether the pointer is on a page's bottom outer corner, the one that lifts. */
    boolean atCorner(int side,double mx,double my){int x=pageX(side),foot=pageY()+JournalPageRenderer.H,outer=side==0?x:x+JournalPageRenderer.W;return my>=foot-16&&my<foot&&Math.abs(mx-outer)<16;}
    /** The corner held to tear the page: it lifts further the longer it is held, and then the page comes away. */
    private void lift(GuiGraphics g,int side,int x,int y){
        float progress=Math.min(1F,(Util.getMillis()-held)/(float)HOLD_MS);
        curl(g,side,x,y,6+(int)(progress*46));
        if(progress>=1)tear(side);
    }
    /** The page's bottom outer corner turned up by {@code size} pixels, the dark of the desk showing under it. */
    private static void curl(GuiGraphics g,int side,int x,int y,int size){
        int outer=side==0?x:x+JournalPageRenderer.W-1,dir=side==0?1:-1,foot=y+JournalPageRenderer.H;
        for(int i=0;i<size;i++){int cx=outer+dir*i,h=size-i;g.fill(cx,foot-h,cx+1,foot,0xFF231D17);g.fill(cx,foot-h-2,cx+1,foot-h,0x50000000);}
        for(int i=0;i<size;i++){int cx=outer+dir*(size-1-i),h=i;g.fill(cx,foot-size,cx+1,foot-size+h,0xFFD8CBAE);}
    }
    /** The page comes away in the reader's hand; the server tears it out and the stub is all that is left here. */
    private void tear(int side){
        int i=index(side);holding=-1;send();
        torn.add(i);pages.remove(i);editors[side]=null;editing[side]=-1;if(focused==side)focused=-1;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN,.55F,1F));
        if(!proof&&Minecraft.getInstance().getConnection()!=null)PacketDistributor.sendToServer(new io.github.knaitoe.theoldesthouse.network.JournalTearPayload(i,false));
        rebuildWidgets();
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
        if(super.mouseClicked(mx,my,button)){
            // A field in the second row takes the keys from the page being written.
            if(getFocused()!=null&&(getFocused()==finder||getFocused()==jumper)){focused=-1;findFocus=getFocused()==finder;jumpFocus=getFocused()==jumper;}
            return true;
        }
        if(heardMode)return false;
        int side=sideAt(mx,my);
        if(side>=0&&button==0){findFocus=false;jumpFocus=false;if(getFocused()==finder||getFocused()==jumper)setFocused(null);}
        if(button==1&&side>=0){int i=index(side);
            // A right-click on the stub takes the page back; held on a written page, it lifts the corner to tear it out.
            if(torn.contains(i)){if(!proof&&Minecraft.getInstance().getConnection()!=null)PacketDistributor.sendToServer(new io.github.knaitoe.theoldesthouse.network.JournalTearPayload(i,true));return true;}
            if(editable(side)&&!currentText(side).isBlank()){holding=side;held=Util.getMillis();return true;}
            return false;
        }
        if(side<0||!editable(side)){focused=-1;return false;}
        double px=mx-pageX(side),py=my-pageY();
        focused=side;var field=editors[side];
        if(field!=null){field.setSelecting(false);field.seekCursorToPoint(px-JournalPageRenderer.TEXT_X,py-JournalPageRenderer.TEXT_Y);}
        rebuildWidgets();return true;
    }
    @Override public boolean mouseReleased(double mx,double my,int button){if(button==1)holding=-1;return super.mouseReleased(mx,my,button);}
    @Override public boolean mouseDragged(double mx,double my,int button,double dx,double dy){
        if(focused>=0&&editors[focused]!=null&&button==0){editors[focused].setSelecting(true);editors[focused].seekCursorToPoint(mx-pageX(focused)-JournalPageRenderer.TEXT_X,my-pageY()-JournalPageRenderer.TEXT_Y);editors[focused].setSelecting(false);return true;}
        return super.mouseDragged(mx,my,button,dx,dy);
    }
    @Override public boolean mouseScrolled(double mx,double my,double sx,double sy){
        if(sy!=0&&heardMode){turnHeard(sy>0?-1:1);return true;}
        if(sy!=0&&focused<0){turn(sy>0?-1:1);return true;}
        return super.mouseScrolled(mx,my,sx,sy);
    }
    @Override public boolean keyPressed(int key,int scan,int mods){
        if(focused<0&&(key==257||key==335)){
            if(finder!=null&&getFocused()==finder){find();return true;}
            if(jumper!=null&&getFocused()==jumper){jump();return true;}
        }
        if(focused<0&&key==256&&(getFocused()==finder||getFocused()==jumper)&&getFocused()!=null){findFocus=false;jumpFocus=false;setFocused(null);return true;}
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
