package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.house.HouseText;
import io.github.knaitoe.theoldesthouse.journal.JournalPage;
import io.github.knaitoe.theoldesthouse.journal.ZampanoLeaves;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.RandomSource;

/**
 * Draws a page: paper and its wear, the flowing writing, and every placed line in its shape (at any angle, mirrored, a
 * spiral, a cascade down the page, a narrow column or a boxed window), in graphite. "House" is blue as in every book and
 * "Minotaur" red, because a page is literature. Zampanò's pages in the courtyard are drawn by the same pieces.
 */
public final class JournalPageRenderer {
    public static final int W=150, H=196, TEXT_X=16, TEXT_Y=16, TEXT_W=118, LINES=15, LINE=9;
    public static final int PAPER=0xFFEFE6D0, ENDPAPER=0xFFE4D6B8, INK=0xFF34343A, RULE=0x26506A8A, MARGIN=0x30B04030, FAINT=0xFF8E8474, SELECT=0x553C64A8;
    static final int COLUMN_W=44, BOX_W=78;
    private static int uncull;
    private JournalPageRenderer(){}

    /** Glyphs drawn from behind face away from the screen, and text is culled; a mirrored run or a page turned over draws with culling off. */
    public static void uncull(GuiGraphics g){if(uncull++==0){g.flush();RenderSystem.disableCull();}}
    public static void recull(GuiGraphics g){if(uncull>0&&--uncull==0){g.flush();RenderSystem.enableCull();}}
    static FormattedCharSequence seq(String text,Style hand){return FormattedCharSequence.forward(text,hand);}
    static int width(Font font,String text,Style hand){return font.width(FormattedText.of(text,hand));}

    /** The paper, its rules and, by wear: water rings, a dog-eared corner, swollen ripples, then a tide line and a torn corner. */
    public static void paper(GuiGraphics g,int x,int y,boolean left,boolean cover,int wear,int seed){
        g.fill(x,y,x+W,y+H,cover?ENDPAPER:PAPER);
        // The gutter's shadow.
        for(int i=0;i<6;i++){int a=(6-i)*7;int gx=left?x+W-1-i:x+i;g.fill(gx,y,gx+1,y+H,a<<24);}
        if(!cover){
            for(int i=0;i<LINES;i++){int ry=y+TEXT_Y+(i+1)*LINE-1;g.fill(x+8,ry,x+W-8,ry+1,RULE);}
            int mx=x+TEXT_X-3;g.fill(mx,y+6,mx+1,y+H-6,MARGIN);
        }
        if(wear<=0)return;
        var random=RandomSource.create(seed*7919L+wear);
        for(int r=0;r<2+random.nextInt(2);r++)ring(g,x+20+random.nextInt(W-40),y+30+random.nextInt(H-60),12+random.nextInt(14),0x34785830);
        if(wear>=2){int corner=14;for(int i=0;i<corner;i++){int cx=left?x+i:x+W-1-i;int w=corner-i;g.fill(left?cx:cx,y,left?cx+1:cx+1,y+w,0xFFD8CBAE);}
            for(int i=0;i<corner;i++){int cx=left?x+i:x+W-1-i;g.fill(cx,y+corner-1-i,cx+1,y+corner-i,0x40000000);}}
        if(wear>=3)for(int band=y+18;band<y+H-6;band+=23)for(int i=0;i<W;i+=2){int dy=(int)Math.round(Math.sin((i+seed*13)/9.0)*2);g.fill(x+i,band+dy,x+i+2,band+dy+2,0x12000000);}
        if(wear>=4){
            int tide=y+(int)(H*.62);
            for(int i=0;i<W;i++){int dy=(int)Math.round(Math.sin((i+seed*5)/7.0)*3+Math.sin(i/3.0)*1);g.fill(x+i,tide+dy,x+i+1,tide+dy+1,0x5A6B4A2A);g.fill(x+i,tide+dy+1,x+i+1,y+H,0x10785830);}
            // The torn corner, at the page's foot on its outer edge.
            for(int i=0;i<18;i++){int cx=left?x+i:x+W-1-i;int h=18-i+(i%3==0?2:0);g.fill(cx,y+H-h,cx+1,y+H,0xFF1E1A16);}
        }
    }
    static void ring(GuiGraphics g,int cx,int cy,int r,int color){
        for(int a=0;a<360;a+=3){double t=Math.toRadians(a);int px=cx+(int)Math.round(Math.cos(t)*r),py=cy+(int)Math.round(Math.sin(t)*r);g.fill(px,py,px+1,py+1,color);}
    }

    /** The name inside the cover, in the reader's own hand. */
    public static void cover(GuiGraphics g,Font font,int x,int y,String name){
        HouseText.enterLiterature();
        try{int w=font.width(name);g.drawString(font,name,x+(W-w)/2,y+H/2-4,INK,false);}
        finally{HouseText.leaveLiterature();}
    }

    /** The page's flowing writing, wrapped as the editor wraps it. */
    public static void writing(GuiGraphics g,Font font,int x,int y,String text){writing(g,font,x,y,text,Style.EMPTY,INK);}
    public static void writing(GuiGraphics g,Font font,int x,int y,String text,Style hand,int ink){
        HouseText.enterLiterature();
        try{int row=0;for(var line:font.split(FormattedText.of(text,hand),TEXT_W)){if(row>=LINES)break;g.drawString(font,line,x+TEXT_X,y+TEXT_Y+row*LINE,ink,false);row++;}}
        finally{HouseText.leaveLiterature();}
    }

    /** The page's number, centred at its foot, clear of a torn corner. */
    public static void number(GuiGraphics g,Font font,int x,int y,int index){String n=Integer.toString(index);g.drawString(font,n,x+(W-font.width(n))/2,y+H-11,FAINT,false);}
    /** Where and when the page was begun, faintly at its foot. */
    public static void header(GuiGraphics g,Font font,int x,int y,JournalPage page){
        if(page.day()<=0)return;
        String text=(page.begun().isEmpty()?"":page.begun()+", ")+"day "+page.day();
        var pose=g.pose();pose.pushPose();pose.translate(x+TEXT_X,y+H-21,0);pose.scale(.75F,.75F,1);
        g.drawString(font,text,0,0,FAINT,false);pose.popPose();
    }

    public static void placed(GuiGraphics g,Font font,int x,int y,JournalPage.Placed run,boolean selected){placed(g,font,x,y,run,selected,Style.EMPTY,INK);}
    /** A run in a given hand (Zampanò's font, struck through) and ink. */
    public static void placed(GuiGraphics g,Font font,int x,int y,JournalPage.Placed run,boolean selected,Style hand,int ink){
        float px=x+run.x()*W, py=y+run.y()*H;
        var pose=g.pose();pose.pushPose();pose.translate(px,py,0);
        pose.mulPose(Axis.ZP.rotationDegrees(run.angle()));
        pose.scale(run.mirrored()?-run.size():run.size(),run.size(),1);
        // Mirrored glyphs face away from the screen; text is culled by default, so a mirrored run draws with culling off.
        if(run.mirrored())uncull(g);
        HouseText.enterLiterature();
        try{
            if(selected){var b=local(font,run,hand);g.fill((int)Math.floor(b[0])-2,(int)Math.floor(b[1])-2,(int)Math.ceil(b[2])+2,(int)Math.ceil(b[3])+2,SELECT);}
            switch(run.shape()){
                case LINE->g.drawString(font,seq(run.text(),hand),0,-4,ink,false);
                case COLUMN->{int row=0;for(var line:font.split(FormattedText.of(run.text(),hand),COLUMN_W))g.drawString(font,line,0,-4+row++*LINE,ink,false);}
                case BOX->{var lines=font.split(FormattedText.of(run.text(),hand),BOX_W);int h=lines.size()*LINE;int w=0;for(var l:lines)w=Math.max(w,font.width(l));
                    frame(g,-4,-8,w+4,h-2,ink);int row=0;for(var line:lines)g.drawString(font,line,0,-4+row++*LINE,ink,false);}
                case CASCADE->{float cx=0;int row=0;for(var word:run.text().trim().split("\\s+")){g.drawString(font,seq(word,hand),(int)cx,-4+row*LINE,ink,false);cx+=(width(font,word,hand)+width(font," ",hand))*.55F;row++;}}
                case SPIRAL->spiral(g,font,run.text(),hand,ink);
            }
        }finally{HouseText.leaveLiterature();if(run.mirrored())recull(g);}
        pose.popPose();
    }
    static void frame(GuiGraphics g,int x0,int y0,int x1,int y1,int color){g.fill(x0,y0,x1,y0+1,color);g.fill(x0,y1-1,x1,y1,color);g.fill(x0,y0,x0+1,y1,color);g.fill(x1-1,y0,x1,y1,color);}

    /** Letters wound outward from the centre, each turned along the curve; the blue and red words keep their colour letter by letter. */
    static void spiral(GuiGraphics g,Font font,String text,Style hand,int ink){
        var seq=HouseText.literature(Component.literal(text).withStyle(hand)).getVisualOrderText();
        var letters=new ArrayList<Object[]>();
        seq.accept((index,style,cp)->{letters.add(new Object[]{style,cp});return true;});
        double theta=0;var pose=g.pose();
        for(var l:letters){
            double r=5+2.4*theta;var piece=FormattedCharSequence.codepoint((int)l[1],(net.minecraft.network.chat.Style)l[0]);
            float lx=(float)(Math.cos(theta)*r),ly=(float)(Math.sin(theta)*r);
            pose.pushPose();pose.translate(lx,ly,0);pose.mulPose(Axis.ZP.rotationDegrees((float)Math.toDegrees(theta)+90));
            g.drawString(font,piece,-font.width(piece)/2,-4,ink,false);pose.popPose();
            theta+=(font.width(piece)+.8)/r;
        }
    }

    /** A run's bounds before it is turned, scaled and mirrored, around its anchor. */
    static float[] local(Font font,JournalPage.Placed run,Style hand){
        return switch(run.shape()){
            case LINE->new float[]{0,-4,width(font,run.text(),hand),5};
            case COLUMN->{int n=font.split(FormattedText.of(run.text(),hand),COLUMN_W).size();yield new float[]{0,-4,COLUMN_W,-4+n*LINE};}
            case BOX->{var lines=font.split(FormattedText.of(run.text(),hand),BOX_W);int w=0;for(var l:lines)w=Math.max(w,font.width(l));yield new float[]{-4,-8,w+4,lines.size()*LINE-2};}
            case CASCADE->{float cx=0,wmax=0;String[] words=run.text().trim().split("\\s+");for(var word:words){wmax=Math.max(wmax,cx+width(font,word,hand));cx+=(width(font,word,hand)+width(font," ",hand))*.55F;}yield new float[]{0,-4,wmax,-4+words.length*LINE};}
            case SPIRAL->{double theta=0,r=5;for(int i=0;i<run.text().length();i++){r=5+2.4*theta;theta+=(width(font,run.text().substring(i,i+1),hand)+.8)/r;}float R=(float)r+5;yield new float[]{-R,-R,R,R};}
        };
    }
    /** The run's bounds on the page, after turning, scaling and mirroring, as page pixels: x0, y0, x1, y1. */
    public static float[] bounds(Font font,JournalPage.Placed run){
        var b=local(font,run,Style.EMPTY);double a=Math.toRadians(run.angle()),c=Math.cos(a),s=Math.sin(a);float sx=run.mirrored()?-run.size():run.size(),sy=run.size();
        float x0=Float.MAX_VALUE,y0=Float.MAX_VALUE,x1=-Float.MAX_VALUE,y1=-Float.MAX_VALUE;
        for(float[] p:new float[][]{{b[0],b[1]},{b[2],b[1]},{b[0],b[3]},{b[2],b[3]}}){
            float qx=p[0]*sx,qy=p[1]*sy;float rx=(float)(qx*c-qy*s)+run.x()*W,ry=(float)(qx*s+qy*c)+run.y()*H;
            x0=Math.min(x0,rx);y0=Math.min(y0,ry);x1=Math.max(x1,rx);y1=Math.max(y1,ry);
        }
        return new float[]{x0,y0,x1,y1};
    }

    /** A whole page as it would look closed and reopened: paper, writing, placed lines and its header. */
    public static void page(GuiGraphics g,Font font,int x,int y,int index,boolean left,int wear,JournalPage page,String name,int selected){
        paper(g,x,y,left,index==0,wear,index);
        if(index==0){cover(g,font,x,y,name);return;}
        rubbings(g,font,x,y,page);
        writing(g,font,x,y,page.text());
        notes(g,font,x,y,page,page.text());
        g.enableScissor(x,y,x+W,y+H);
        List<JournalPage.Placed> runs=page.placed();for(int i=0;i<runs.size();i++)placed(g,font,x,y,runs.get(i),i==selected);
        g.disableScissor();
        header(g,font,x,y,page);number(g,font,x,y,index);
    }

    /** 0.4.199: a page torn out of a journal (journal/TornPages), as anyone holding it sees it: no wear, no number, one ragged edge. */
    public static void torn(GuiGraphics g,Font font,int x,int y,JournalPage page){
        paper(g,x,y,false,false,0,7);
        rubbings(g,font,x,y,page);
        writing(g,font,x,y,page.text());
        notes(g,font,x,y,page,page.text());
        g.enableScissor(x,y,x+W,y+H);for(var run:page.placed())placed(g,font,x,y,run,false);g.disableScissor();
        header(g,font,x,y,page);
        ragged(g,x,y,H,true,0xFF1E1A16);
    }
    /** What a torn page leaves in the book: a strip of paper along the gutter, ragged where it came away, and the dark beyond. */
    public static void stub(GuiGraphics g,int x,int y,boolean left,int seed){
        g.fill(x,y,x+W,y+H,0xFF231D17);
        int strip=11,sx=left?x+W-strip:x;
        g.fill(sx,y,sx+strip,y+H,PAPER);
        for(int i=0;i<6;i++){int a=(6-i)*7;int gx=left?x+W-1-i:x+i;g.fill(gx,y,gx+1,y+H,a<<24);}
        // The tear runs along the strip's outer edge.
        for(int row=0;row<H;row++){int bite=bite(row,seed);int ex=left?sx:sx+strip;g.fill(left?ex:ex-bite,y+row,left?ex+bite:ex,y+row+1,0xFF231D17);}
    }
    /** One edge of a page torn away: little bites of the dark behind it, never quite the same twice. */
    static void ragged(GuiGraphics g,int x,int y,int h,boolean leftEdge,int dark){
        for(int row=0;row<h;row++){int bite=bite(row,3);int ex=leftEdge?x:x+W;g.fill(leftEdge?ex:ex-bite,y+row,leftEdge?ex+bite:ex,y+row+1,dark);}
    }
    static int bite(int row,int seed){return 1+Math.floorMod(row*7+(row/3)*13+seed*5,4)+(Math.floorMod(row/5+seed,7)==0?2:0);}

    /** The other hand: its own lettering, in ink rather than the reader's graphite (journal/CoAuthor). */
    public static final Style OTHER=Style.EMPTY.withFont(io.github.knaitoe.theoldesthouse.house.HouseWriting.MARGIN_FONT);
    public static final int OTHER_INK=0xFF2B1A12, OTHER_WASH=0x902B1A12;
    static final int OTHER_W=100;
    /**
     * The other hand's notes over the writing as it now stands: a heavy stroke through each word it struck, an answer squeezed
     * in under its line (or, once the reader has taken that line away, where the line was), and a written note where it lies.
     * A struck word the reader has deleted takes its stroke with it.
     */
    public static void notes(GuiGraphics g,Font font,int x,int y,JournalPage page,String text){
        if(page.notes().isEmpty())return;
        var lines=lines(font,text);var pose=g.pose();
        HouseText.enterLiterature();
        try{for(var n:page.notes())switch(n.mark()){
            case STRUCK->{int[] at=locate(font,text,lines,n.anchor(text),n.word().length());if(at==null)continue;
                int sx=x+TEXT_X+at[0],sy=y+TEXT_Y+at[1];g.fill(sx-1,sy+3,sx+at[2]+1,sy+5,OTHER_INK);g.fill(sx,sy+2,sx+at[2]+2,sy+3,OTHER_WASH);}
            case DIRECTION->{int at=n.anchor(text);int[] word=locate(font,text,lines,at,n.word().length());float nx,ny;
                if(word==null){nx=x+n.x()*W;ny=y+n.y()*H;}
                else{
                    // It answers the reader's sentence: after the last word of the paragraph holding the word, or on the row beneath it.
                    int row=word[1]/LINE;
                    while(row+1<lines.size()&&row+1<LINES){int e=lines.get(row)[1];if(e>=text.length()||text.charAt(e)=='\n')break;row++;}
                    int[] line=lines.get(row);float end=TEXT_X+font.width(text.substring(line[0],line[1]).stripTrailing())+4,wide=font.width(FormattedText.of(n.text(),OTHER))*.7F;
                    boolean after=end+wide<=W-6;
                    nx=x+(after?end:TEXT_X+4);ny=y+TEXT_Y+row*LINE+(after?1:LINE+1);
                }
                pose.pushPose();pose.translate(nx,ny,0);pose.mulPose(Axis.ZP.rotationDegrees(-4));pose.scale(.7F,.7F,1);
                g.drawString(font,seq(n.text(),OTHER),0,0,OTHER_INK,false);pose.popPose();}
            case WRITTEN,QUOTED->{pose.pushPose();pose.translate(x+n.x()*W,y+n.y()*H,0);pose.mulPose(Axis.ZP.rotationDegrees(-3));
                int row=0;for(var line:font.split(FormattedText.of(n.text(),OTHER),OTHER_W))g.drawString(font,line,0,row++*LINE,OTHER_INK,false);
                pose.popPose();}
        }}finally{HouseText.leaveLiterature();}
    }
    /** The writing's wrapped lines as the editor wraps them: where each begins and ends in the text. */
    static List<int[]> lines(Font font,String text){var out=new ArrayList<int[]>();font.getSplitter().splitLines(text,TEXT_W,Style.EMPTY,false,(style,start,end)->out.add(new int[]{start,end}));return out;}
    /** A span of the wrapped writing as page pixels from the text's corner: its left, its row's top and its width; null when off the page. */
    static int[] locate(Font font,String text,List<int[]> lines,int at,int length){
        if(at<0)return null;
        for(int r=0;r<lines.size()&&r<LINES;r++){int[] l=lines.get(r);if(at>=l[0]&&at<l[1]){int end=Math.min(at+length,l[1]);return new int[]{font.width(text.substring(l[0],at)),r*LINE,font.width(text.substring(at,end))};}}
        return null;
    }

    static final int RUBBING=52, CAPTION_W=104;
    /**
     * The page's charcoal rubbings, each a little askew, with the line about it written small beside it in the reader's
     * graphite, leaving room above for their own writing.
     */
    public static void rubbings(GuiGraphics g,Font font,int x,int y,JournalPage page){
        for(var r:page.rubbings()){
            var kind=io.github.knaitoe.theoldesthouse.journal.Rubbings.byId(r.id());if(kind==null)continue;
            float px=x+r.x()*W,py=y+r.y()*H;var pose=g.pose();
            pose.pushPose();pose.translate(px+RUBBING/2F,py+RUBBING/2F,0);pose.mulPose(Axis.ZP.rotationDegrees(r.angle()));
            RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
            g.blit(kind.texture(),-RUBBING/2,-RUBBING/2,RUBBING,RUBBING,0,0,64,64,64,64);
            RenderSystem.disableBlend();pose.popPose();
            HouseText.enterLiterature();
            try{pose.pushPose();pose.translate(px+RUBBING+6,py+4,0);pose.scale(.65F,.65F,1);int row=0;
                for(var line:font.split(FormattedText.of(kind.observation()),CAPTION_W))g.drawString(font,line,0,row++*LINE,INK,false);
                pose.popPose();}
            finally{HouseText.leaveLiterature();}
        }
    }

    /** Zampanò's hand: his font, as in his books. */
    public static final Style ZAMPANO=Style.EMPTY.withFont(io.github.knaitoe.theoldesthouse.house.HouseWriting.ZAMPANO_FONT);
    static final int BACK_INK=0xFF6A6870, BACK_SHADE=0x1C2A1E10;
    /**
     * One of Zampanò's leaves: paper aged by its own wear, his title faint at the head, his writing and every run in his hand,
     * struck runs crossed through, and his page number. Seen from the back (the caller mirrors it), the paper is shaded and the
     * ink is fainter, as writing is through paper. The caller clips it to the page.
     */
    public static void leaf(GuiGraphics g,Font font,int x,int y,int index,boolean left,boolean back){
        var leaf=ZampanoLeaves.leaf(index);int ink=back?BACK_INK:INK;
        paper(g,x,y,left,false,leaf.wear(),101+index);
        if(back)g.fill(x,y,x+W,y+H,BACK_SHADE);
        HouseText.enterLiterature();
        try{if(!leaf.title().isEmpty()){var pose=g.pose();pose.pushPose();pose.translate(x+TEXT_X,y+6,0);pose.scale(.75F,.75F,1);g.drawString(font,seq(leaf.title(),ZAMPANO),0,0,FAINT,false);pose.popPose();}}
        finally{HouseText.leaveLiterature();}
        writing(g,font,x,y,leaf.text(),ZAMPANO,ink);
        for(var run:leaf.runs())placed(g,font,x,y,run.placed(),false,run.struck()?ZAMPANO.withStrikethrough(true):ZAMPANO,ink);
        number(g,font,x,y,index+1);
    }
}
