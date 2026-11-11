package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import io.github.knaitoe.theoldesthouse.labyrinth.BorrascaCards;
import io.github.knaitoe.theoldesthouse.network.BorrascaCardSeenPayload;
import io.github.knaitoe.theoldesthouse.network.BorrascaCardsPayload;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 0.4.455, the owner, of the page of text that told a reader what hung on the doctor's corkboard: "Try to show, don't tell. Try
 * to design an actual ui for this". The board as a reader stands at it (labyrinth.BorrascaCards): cork in a wooden frame, the
 * families' birth cards pinned to it, each with its printed picture, the child's name in the family's own hand, the day, and
 * who it is from.
 * <ul>
 * <li>A card under the pointer lifts. A click takes it down: it comes up large, as it is held, with everything on it (what is
 * printed, the name, the day and year, the line to the doctor and who signed it). A click puts it back; the arrow keys take
 * down the next along the board.</li>
 * <li>The envelope turns over in the hand when it is clicked: the doctor's name on the front and nothing else, and its mouth
 * open on nothing.</li>
 * <li>A card pinned over another covers it; one underneath shows an edge, and can be taken out by that edge.</li>
 * </ul>
 * Nothing is written on the screen but what is written on the cards. A piece held for a moment is told to the server as looked
 * at.
 */
public final class BorrascaCardsScreen extends Screen {
    public static final ResourceLocation SHEET=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/gui/borrasca_cards.png");
    static final int SW=128,SH=64;
    /** The board with its frame, its frame's width, and a card as it hangs on it. */
    public static final int BW=308,BH=172,FRAME=6,CW=86,CH=56;
    /** A card in the hand, in its own units. */
    public static final int HW=160,HH=112;
    /** How long a piece is held before it counts as looked at. */
    static final int COUNTS=15;
    static final int[] PAPER={0xFFF5EFDF,0xFFF8E1E3,0xFFE0EAF5,0xFFF7F0CC,0xFFE2F1E3,0xFFEFE8D6},ACCENT={0xFF9A8F78,0xFFD07A94,0xFF6F99CF,0xFFC9A63A,0xFF6FAE7B,0xFF9A8F78};
    static final int[] PIN={0xFFC8423A,0xFF3F6FB8,0xFFE0B83A,0xFF4E9A5E,0xFFE8E4DA,0xFF8A8F96,0xFFC8423A};
    static final int INK=0xFF2B2A33,OLD_INK=0xFF5A4A3A;
    /** How a place's card hangs off true, as a hand pins it. */
    static final int[][] NUDGE={{-2,1},{1,-2},{2,2},{1,1},{-1,-1},{0,2}};
    final int visit;final List<BorrascaCards.Piece> board;final int[] order;int held=-1,heldFor;boolean turned;
    /** The pieces already told to the server as looked at, as bits: those it sent with the board, and those told since. */
    int told;

    BorrascaCardsScreen(int visit,int seen){super(Component.literal("Dr. Hale's corkboard"));this.visit=visit;this.board=BorrascaCards.board(visit);this.order=BorrascaCards.order(visit);this.told=seen;}
    public static void accept(BorrascaCardsPayload p){Minecraft.getInstance().setScreen(new BorrascaCardsScreen(p.visit(),p.seen()));}
    @Override public boolean isPauseScreen(){return false;}
    @Override public Component getNarrationMessage(){return held>=0?Component.literal(board.get(held).text()):super.getNarrationMessage();}
    @Override public void tick(){
        if(held>=0&&++heldFor==COUNTS&&(told&1<<held)==0){told|=1<<held;PacketDistributor.sendToServer(new BorrascaCardSeenPayload(held));}
    }

    /** Screen pixels to the board's pixel: two if they fit, one, or a fraction in a window too small for one. */
    static float scale(int width,int height){float fit=Math.min((width-8)/(float)BW,(height-8)/(float)BH);return fit>=2?2:fit>=1?1:Math.max(.5F,fit);}
    /** Screen pixels to a held card's unit: the largest whole number that fits. */
    static int zoom(int width,int height){return Math.max(1,Math.min((width-24)/HW,(height-24)/HH));}
    /** Where a piece hangs, from the board's top left corner: one underneath shows below and to the left, one on top sits above and to the right. */
    static int[] at(BorrascaCards.Piece p){
        int x=FRAME+12+p.place()%3*94+NUDGE[p.place()][0],y=FRAME+16+p.place()/3*74+NUDGE[p.place()][1];
        if(p.under()){x-=6;y+=7;}if(p.over()){x+=7;y-=12;}
        return new int[]{x,y};
    }
    /** The piece under a point on the board, the topmost first; none is -1. */
    static int pick(List<BorrascaCards.Piece> pieces,float x,float y){
        for(int i=pieces.size()-1;i>=0;i--){int[] at=at(pieces.get(i));if(x>=at[0]&&x<at[0]+CW&&y>=at[1]&&y<at[1]+CH)return i;}
        return -1;
    }
    /** A card's paper gone yellow with its years. */
    static int aged(int c){int r=((c>>16&255)*55+0xC9*45)/100,g=((c>>8&255)*55+0xB4*45)/100,b=((c&255)*55+0x84*45)/100;return 0xFF000000|r<<16|g<<8|b;}
    /** The hand a piece is written in. Six families and the envelope: the House's own letters, upright, leaning, thin and heavy. */
    static Style hand(int hand){
        return switch(hand){case 0->Style.EMPTY.withFont(HouseWriting.KAREN_FONT);case 1->Style.EMPTY.withFont(HouseWriting.WILL_FONT);case 2->Style.EMPTY.withFont(HouseWriting.PELAFINA_FONT);
            case 3->Style.EMPTY.withFont(HouseWriting.JOHNNY_FONT);case 4->Style.EMPTY.withFont(HouseWriting.MARGIN_FONT);case 6->Style.EMPTY.withFont(HouseWriting.KAREN_FONT).withItalic(true);default->Style.EMPTY.withFont(HouseWriting.ZAMPANO_FONT);};
    }
    private static Component written(BorrascaCards.Piece p,String s){return Component.literal(s).withStyle(hand(p.hand()));}
    /** The child's name as it is written small on the hanging card: one line or two. */
    static List<FormattedCharSequence> smallName(Font font,BorrascaCards.Piece p){return font.split(written(p,p.name()),CW-31);}
    /** The child's name as it is written on the card in the hand, at twice the hand's size: one line or two. */
    static List<FormattedCharSequence> largeName(Font font,BorrascaCards.Piece p){return font.split(written(p,p.name()),(HW-56)/2);}
    static List<FormattedCharSequence> thanks(Font font,BorrascaCards.Piece p){return font.split(written(p,p.thanks()),HW-28);}
    /** Where the rule under a held card's name lies: under one line of name and what else is written by it, lower under two. */
    static int rule(Font font,BorrascaCards.Piece p){return Math.max(55,24+largeName(font,p).size()*18+(p.detail().isEmpty()?0:10)+1);}
    /**
     * What does not fit on a piece as it is drawn, or null: every line on the hanging card inside its border, and everything on the
     * card in the hand above its lower border. The proof asks this of every piece on every visit's board.
     */
    public static String problem(Font font,BorrascaCards.Piece p){
        if(p.envelope())return font.width(written(p,p.signed()))>CW-14?"the envelope's name is wider than its face":font.width(written(p,p.signed()))*2>HW-20?"the envelope's name is wider than the envelope in the hand":null;
        var small=smallName(font,p);
        if(small.size()>2)return "the name takes more than two lines on the hanging card: "+p.name();
        for(var line:small)if(font.width(line)>CW-31)return "a line of the name is wider than the hanging card: "+p.name();
        if(font.width(written(p,p.day()))>CW-12)return "the day is wider than the hanging card: "+p.day();
        if(font.width(written(p,p.signed()))>CW-12)return "the signature is wider than the hanging card: "+p.signed();
        var large=largeName(font,p);
        if(large.size()>2)return "the name takes more than two lines in the hand: "+p.name();
        if(font.width(p.printed())>HW-56)return "the printed line is wider than the card: "+p.printed();
        if(font.width(written(p,p.date()))>HW-28)return "the date is wider than the card: "+p.date();
        var thanks=thanks(font,p);
        if(thanks.size()>2)return "the line to the doctor takes more than two lines: "+p.thanks();
        if(rule(font,p)+17+thanks.size()*9>HH-20)return "the line to the doctor runs into the signature: "+p.thanks();
        if(font.width(written(p,"\u2014 "+p.signed()))>HW-28)return "the signature is wider than the card: "+p.signed();
        return null;
    }
    private static void sheet(GuiGraphics g,int x,int y,int u,int v,int w,int h){g.blit(SHEET,x,y,u,v,w,h,SW,SH);}
    private static void picture(GuiGraphics g,int x,int y,int which){sheet(g,x,y,40+which%5*16,which/5*16,16,16);}
    private static void pin(GuiGraphics g,int x,int y,int colour){
        g.setColor((colour>>16&255)/255F,(colour>>8&255)/255F,(colour&255)/255F,1);sheet(g,x,y,40,34,5,5);g.setColor(1,1,1,1);
    }
    private static void outline(GuiGraphics g,int x0,int y0,int x1,int y1,int colour){g.fill(x0,y0,x1,y0+1,colour);g.fill(x0,y1-1,x1,y1,colour);g.fill(x0,y0+1,x0+1,y1-1,colour);g.fill(x1-1,y0+1,x1,y1-1,colour);}
    /** A straight line a pixel wide, by steps. */
    private static void line(GuiGraphics g,int x0,int y0,int x1,int y1,int colour){
        int n=Math.max(1,Math.max(Math.abs(x1-x0),Math.abs(y1-y0)));
        for(int i=0;i<=n;i++){int x=x0+Math.round((x1-x0)*i/(float)n),y=y0+Math.round((y1-y0)*i/(float)n);g.fill(x,y,x+1,y+1,colour);}
    }

    /** The board: its frame, its cork, and every piece but one (the one in the hand), the one under the pointer lifted. */
    public static void board(GuiGraphics g,Font font,List<BorrascaCards.Piece> pieces,int hover,int skip){
        for(int y=FRAME;y<BH-FRAME;y+=32)for(int x=FRAME;x<BW-FRAME;x+=32)sheet(g,x,y,0,0,Math.min(32,BW-FRAME-x),Math.min(32,BH-FRAME-y));
        for(int x=FRAME;x<BW-FRAME;x+=32){sheet(g,x,0,0,32,Math.min(32,BW-FRAME-x),FRAME);sheet(g,x,BH-FRAME,0,32,Math.min(32,BW-FRAME-x),FRAME);}
        for(int y=FRAME;y<BH-FRAME;y+=32){sheet(g,0,y,32,0,FRAME,Math.min(32,BH-FRAME-y));sheet(g,BW-FRAME,y,32,0,FRAME,Math.min(32,BH-FRAME-y));}
        for(int[] corner:new int[][]{{0,0},{BW-FRAME,0},{0,BH-FRAME},{BW-FRAME,BH-FRAME}})sheet(g,corner[0],corner[1],32,32,FRAME,FRAME);
        // What the frame's top and side throw on the cork.
        g.fill(FRAME,FRAME,BW-FRAME,FRAME+2,0x40000000);g.fill(FRAME,FRAME+2,FRAME+2,BH-FRAME,0x28000000);
        for(int i=0;i<pieces.size();i++){if(i==skip)continue;int[] at=at(pieces.get(i));small(g,font,pieces.get(i),at[0],at[1]-(i==hover?1:0),i==hover);}
        // Where the piece in the hand hung: the hole its pin left.
        if(skip>=0&&skip<pieces.size()){int[] at=at(pieces.get(skip));g.fill(at[0]+CW/2,at[1],at[0]+CW/2+1,at[1]+1,0xFF4A3520);}
    }
    /** A piece as it hangs: a card's picture, the child's name, the day and who it is from; or the envelope's face. */
    static void small(GuiGraphics g,Font font,BorrascaCards.Piece p,int x,int y,boolean lifted){
        int paper=p.old()?aged(PAPER[p.paper()]):PAPER[p.paper()],accent=p.old()?aged(ACCENT[p.paper()]):ACCENT[p.paper()],ink=p.old()?OLD_INK:INK;
        g.fill(x+2,y+2+(lifted?1:0),x+CW+2,y+CH+2+(lifted?1:0),0x58000000);g.fill(x,y,x+CW,y+CH,paper);
        if(p.envelope()){
            // Its face: the doctor's name, and the edges of the flap behind showing through the paper.
            int edge=aged(paper);outline(g,x,y,x+CW,y+CH,edge);line(g,x+1,y+1,x+CW/2,y+CH/2-2,edge);line(g,x+CW-2,y+1,x+CW/2,y+CH/2-2,edge);
            var to=written(p,p.signed());g.drawString(font,to,x+(CW-font.width(to))/2,y+38,ink,false);
        }else{
            outline(g,x+2,y+2,x+CW-2,y+CH-2,accent);picture(g,x+6,y+6,p.picture());
            var name=smallName(font,p);
            for(int i=0;i<Math.min(2,name.size());i++)g.drawString(font,name.get(i),x+26,y+(name.size()>1?5:10)+i*9,ink,false);
            g.drawString(font,written(p,p.day()),x+6,y+28,ink,false);
            var signed=written(p,p.signed());g.drawString(font,signed,x+CW-6-font.width(signed),y+40,ink,false);
        }
        if(p.old())sheet(g,x+CW-8,y+CH-8,48,34,8,8);
        pin(g,x+CW/2-2,y-2,PIN[p.hand()]);
        if(lifted)outline(g,x-1,y-1,x+CW+1,y+CH+1,0x60FFFFFF);
    }
    /** A piece in the hand, from its top left corner, in its own units: everything on it. The envelope shows its face, or its open mouth once turned. */
    public static void large(GuiGraphics g,Font font,BorrascaCards.Piece p,boolean turned){
        int paper=p.old()?aged(PAPER[p.paper()]):PAPER[p.paper()],accent=p.old()?aged(ACCENT[p.paper()]):ACCENT[p.paper()],ink=p.old()?OLD_INK:INK;var pose=g.pose();
        // Turned over, the envelope's flap stands up above its fold, so the envelope itself begins lower.
        int fold=p.envelope()&&turned?20:0;
        g.fill(4,4+fold,HW+4,HH+4,0x70000000);g.fill(0,fold,HW,HH,paper);
        if(p.envelope()){
            int edge=aged(paper),inside=0xFFB5A98C;outline(g,0,fold,HW,HH,edge);
            if(turned){
                // Its back. The flap is lifted and seen from inside, leaning away: its gum a paler line along its edges.
                for(int y=1;y<fold;y++){int w=(HW/2-3)*y/fold;g.fill(HW/2-w,y,HW/2+w,y+1,0xFFE4DCC6);}
                line(g,3,fold-1,HW/2,1,edge);line(g,HW-4,fold-1,HW/2,1,edge);line(g,9,fold-2,HW/2,4,0xFFF6F1E2);line(g,HW-10,fold-2,HW/2,4,0xFFF6F1E2);
                // The mouth open in a V under the fold, and the paper of its own inside all there is to see in it.
                int deep=fold+38,half=HW/2-8;
                for(int y=fold+1;y<deep;y++){int w=half*(deep-y)/(deep-fold-1);g.fill(HW/2-w,y,HW/2+w,y+1,y<fold+6?0xFF8F8468:inside);}
                line(g,8,fold+1,HW/2,deep,edge);line(g,HW-9,fold+1,HW/2,deep,edge);
                // The seams of its back, from the bottom corners up to the point of the mouth.
                line(g,1,HH-2,HW/2,deep,edge);line(g,HW-2,HH-2,HW/2,deep,edge);
            }else{
                line(g,1,1,HW/2,54,edge);line(g,HW-2,1,HW/2,54,edge);
                var to=written(p,p.signed());
                pose.pushPose();pose.translate(HW/2F-font.width(to),68,0);pose.scale(2,2,1);g.drawString(font,to,0,0,ink,false);pose.popPose();
            }
            if(p.old()){pose.pushPose();pose.translate(HW-16,HH-16,0);pose.scale(2,2,1);sheet(g,0,0,48,34,8,8);pose.popPose();}
            return;
        }
        outline(g,4,4,HW-4,HH-4,accent);outline(g,6,6,HW-6,HH-6,accent);
        pose.pushPose();pose.translate(10,12,0);pose.scale(2,2,1);picture(g,0,0,p.picture());pose.popPose();
        g.drawString(font,p.printed(),46,12,accent,false);
        var name=largeName(font,p);int y=24;
        for(int i=0;i<Math.min(2,name.size());i++,y+=18){pose.pushPose();pose.translate(46,y,0);pose.scale(2,2,1);g.drawString(font,name.get(i),0,0,ink,false);pose.popPose();}
        if(!p.detail().isEmpty())g.drawString(font,written(p,p.detail()),46,y,ink,false);
        int rule=rule(font,p);g.fill(12,rule,HW-12,rule+1,accent);
        g.drawString(font,written(p,p.date()),14,rule+5,ink,false);
        var thanks=thanks(font,p);
        for(int i=0;i<Math.min(2,thanks.size());i++)g.drawString(font,thanks.get(i),14,rule+17+i*9,ink,false);
        var signed=written(p,"\u2014 "+p.signed());g.drawString(font,signed,HW-14-font.width(signed),HH-19,ink,false);
        if(p.old()){pose.pushPose();pose.translate(HW-16,HH-16,0);pose.scale(2,2,1);sheet(g,0,0,48,34,8,8);pose.popPose();}
        pose.pushPose();pose.translate(HW/2F-5,-4,0);pose.scale(2,2,1);pin(g,0,0,PIN[p.hand()]);pose.popPose();
    }

    private void sound(float pitch){Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN,pitch));}
    void take(int index){held=index;heldFor=0;turned=false;sound(1.2F);}
    /** Where the piece in the hand comes in the board's order. */
    private int along(){for(int i=0;i<order.length;i++)if(order[i]==held)return i;return 0;}
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float partial){}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        g.fill(0,0,width,height,0xB4000000);
        float s=scale(width,height);int left=Math.round((width-BW*s)/2),top=Math.round((height-BH*s)/2);var pose=g.pose();
        pose.pushPose();pose.translate(left,top,0);pose.scale(s,s,1);
        board(g,font,board,held>=0?-1:pick(board,(mx-left)/s,(my-top)/s),held);
        pose.popPose();
        if(held>=0){
            g.fill(0,0,width,height,0x90000000);int z=zoom(width,height);
            pose.pushPose();pose.translate((width-HW*z)/2F,(height-HH*z)/2F,50);pose.scale(z,z,1);large(g,font,board.get(held),turned);pose.popPose();
        }
        super.render(g,mx,my,partial);
    }
    @Override public boolean mouseClicked(double mx,double my,int button){
        if(held>=0){
            int z=zoom(width,height),x=(width-HW*z)/2,y=(height-HH*z)/2;
            if(board.get(held).envelope()&&mx>=x&&mx<x+HW*z&&my>=y&&my<y+HH*z){turned=!turned;sound(1.4F);return true;}
            held=-1;sound(.9F);return true;
        }
        float s=scale(width,height);int i=pick(board,(float)(mx-Math.round((width-BW*s)/2))/s,(float)(my-Math.round((height-BH*s)/2))/s);
        if(i>=0)take(i);
        return true;
    }
    @Override public boolean keyPressed(int key,int scan,int mods){
        if(held>=0&&key==InputConstants.KEY_ESCAPE){held=-1;sound(.9F);return true;}
        if(key==InputConstants.KEY_RIGHT||key==InputConstants.KEY_D){take(order[held<0?0:(along()+1)%order.length]);return true;}
        if(key==InputConstants.KEY_LEFT||key==InputConstants.KEY_A){take(order[held<0?order.length-1:(along()+order.length-1)%order.length]);return true;}
        if(held>=0&&board.get(held).envelope()&&(key==InputConstants.KEY_SPACE||key==InputConstants.KEY_RETURN)){turned=!turned;sound(1.4F);return true;}
        if(held<0&&minecraft!=null&&minecraft.options.keyInventory.matches(key,scan)){onClose();return true;}
        return super.keyPressed(key,scan,mods);
    }
}
