package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.labyrinth.GhostsLore;
import io.github.knaitoe.theoldesthouse.network.GhostsLorePayload;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** The family's private papers and a saved browser page, with no replies or completion buttons. */
public final class GhostsLoreScreen extends Screen {
    final int kind,day;private final List<String> pages;private int page,scroll,maxScroll;
    public GhostsLoreScreen(int kind,int day){super(Component.literal(kind==GhostsLore.DIARY?"Sarah's diary":kind==GhostsLore.BANK?"Kitchen papers":kind==GhostsLore.DRAWING?"Inside Merry's book":"Marjorie's laptop"));this.kind=kind;this.day=day;pages=GhostsLore.pages(kind,day);}
    public static void open(GhostsLorePayload p){if(p.kind()>=0&&p.kind()<=GhostsLore.DRAWING)Minecraft.getInstance().setScreen(new GhostsLoreScreen(p.kind(),Math.max(1,Math.min(7,p.day()))));}
    void proofPage(int selected){page=Math.max(0,Math.min(pages.size()-1,selected));scroll=0;}
    private int w(){return Math.min(500,width-24);}private int h(){return Math.min(330,height-24);}private int x(){return (width-w())/2;}private int y(){return (height-h())/2;}
    @Override protected void init(){
        addRenderableWidget(Button.builder(Component.literal("Close"),b->onClose()).bounds(x()+w()-66,y()+h()-25,56,18).build());
        if(kind==GhostsLore.LAPTOP){String[] labels={"Study","Saved articles","Bookmarks"};int tab=(w()-24)/3;
            for(int n=0;n<labels.length;n++){final int selected=n;addRenderableWidget(Button.builder(Component.literal(labels[n]),b->{page=selected;scroll=0;}).bounds(x()+12+n*tab,y()+28,tab-3,18).build());}
        }else if(pages.size()>1){
            addRenderableWidget(Button.builder(Component.literal("<"),b->{page=Math.max(0,page-1);scroll=0;}).bounds(x()+12,y()+h()-25,25,18).build());
            addRenderableWidget(Button.builder(Component.literal(">"),b->{page=Math.min(pages.size()-1,page+1);scroll=0;}).bounds(x()+42,y()+h()-25,25,18).build());
        }
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float partial){}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        boolean browser=kind==GhostsLore.LAPTOP;int left=x(),top=y(),wide=w(),high=h(),ink=browser?0xff292e32:0xff433a30;
        g.fill(0,0,width,height,0x99000000);g.fill(left+3,top+3,left+wide+3,top+high+3,0x66000000);
        g.fill(left,top,left+wide,top+high,browser?0xff3b4249:0xff81705a);g.fill(left+2,top+2,left+wide-2,top+high-2,browser?0xfff1f0e9:0xffeee3c9);
        if(browser)g.fill(left+2,top+2,left+wide-2,top+23,0xff515b65);
        g.drawString(font,title,left+13,top+10,browser?0xfff5f1e6:ink,false);
        int body=top+(browser?59:35),bottom=top+high-32;
        if(kind==GhostsLore.DRAWING){
            int pictureH=Math.min(144,Math.min((wide-34)*3/4,Math.max(36,(bottom-body-32)*2/3))),pictureW=pictureH*4/3;
            if(bottom-body>48){
                var texture=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("the_oldest_house","textures/gui/ghosts_sister_cats"+(day>=4?"_vines":"")+".png");
                var pose=g.pose();pose.pushPose();pose.translate(left+(wide-pictureW)/2,body,0);pose.scale(pictureW/64F,pictureH/48F,1);g.blit(texture,0,0,0,0,64,48,64,48);pose.popPose();body+=pictureH+10;
            }
        }
        if(browser&&page==0){
            var pose=g.pose();pose.pushPose();pose.translate(left+17,body,0);pose.scale(2,2,1);g.drawString(font,"4",0,0,0xff465e73,false);pose.popPose();
            g.drawString(font,"recurring patterns",left+43,body+4,ink,false);body+=30;
        }
        var lines=font.split(Component.literal(pages.get(page)),wide-38);maxScroll=Math.max(0,lines.size()*12-(bottom-body));scroll=Math.min(scroll,maxScroll);
        g.enableScissor(left+12,body,left+wide-12,bottom);
        int line=0;for(var text:lines){g.drawString(font,text,left+17,body+line*12-scroll,ink,false);line++;}g.disableScissor();
        if(maxScroll>0){int rail=bottom-body,thumb=Math.max(12,rail*rail/(rail+maxScroll)),at=body+(rail-thumb)*scroll/maxScroll;g.fill(left+wide-9,body,left+wide-6,bottom,0xffc2c0b4);g.fill(left+wide-9,at,left+wide-6,at+thumb,0xff767e80);}
        if(!browser)g.drawCenteredString(font,(page+1)+" / "+pages.size(),left+wide/2,top+high-19,0xff81725f);
        super.render(g,mx,my,partial);
    }
    @Override public boolean mouseScrolled(double mx,double my,double dx,double dy){scroll=Math.max(0,Math.min(maxScroll,scroll-(int)(dy*20)));return true;}
}
