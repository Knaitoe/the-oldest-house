package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.journal.ZampanoLeaves;
import io.github.knaitoe.theoldesthouse.network.ZampanoDrawerActionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * A folder from one of Zampanò's drawers, or a leaf the reader has pulled. The folder shows its two leaves side by side; a
 * leaf can be lifted out alone and turned a quarter at a time, or turned over to read what runs mirrored through the paper.
 * Taking a leaf is the reader's own: it leaves their folder, and nobody else's.
 */
public final class ZampanoScreen extends Screen {
    static final int W=JournalPageRenderer.W, H=JournalPageRenderer.H, FOLDER=0xFFB8925A, FOLDER_DARK=0xFF8E6C3E, FADE=0x40FFF4D8;
    private final int folder;private int pulled;private final boolean proof;
    /** The leaf lifted out (or the one held), or -1 while the folder lies open. */
    private int lifted;private int turn;private boolean back;
    private ZampanoScreen(int folder,int leaf,int pulled,boolean proof){super(Component.literal("Zampano"));this.folder=folder;this.lifted=leaf;this.pulled=pulled;this.proof=proof;}

    public static void open(int folder,int leaf,int pulled){
        var mc=Minecraft.getInstance();
        if(folder>=ZampanoLeaves.FOLDERS||folder<0&&!ZampanoLeaves.valid(leaf))return;
        // A refresh from the server (a leaf taken) keeps whatever the reader is looking at.
        if(mc.screen instanceof ZampanoScreen open&&open.folder==folder&&folder>=0){open.pulled=pulled;if(open.lifted>=0&&taken(pulled,open.lifted))open.lifted=-1;open.rebuildWidgets();return;}
        mc.setScreen(new ZampanoScreen(folder,folder>=0?-1:leaf,pulled,false));
    }
    static ZampanoScreen proof(int folder,int leaf,int pulled,int turn,boolean back){var s=new ZampanoScreen(folder,leaf,pulled,true);s.turn=turn;s.back=back;return s;}
    static boolean taken(int pulled,int leaf){return (pulled&1<<leaf)!=0;}
    boolean holding(){return folder<0;}

    int pageX(int side){return (width-(2*W+6))/2+side*(W+6);}
    int pageY(){return Math.max(6,(height-H)/2-12);}
    int centreX(){return width/2;}
    int centreY(){return pageY()+H/2;}

    @Override protected void init(){
        int y=pageY()+H+6;
        if(lifted>=0){
            int x=centreX()-145;
            addRenderableWidget(Button.builder(Component.literal("Turn"),b->{turn=(turn+1)%4;rustle();}).bounds(x,y,56,18).build());
            addRenderableWidget(Button.builder(Component.literal(back?"Turn it back":"Turn over"),b->{back=!back;rustle();rebuildWidgets();}).bounds(x+58,y,76,18).build());
            if(holding())addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose()).bounds(x+136,y,154,18).build());
            else{
                addRenderableWidget(Button.builder(Component.literal("Into the folder"),b->{lifted=-1;turn=0;back=false;rustle();rebuildWidgets();}).bounds(x+136,y,92,18).build());
                int leaf=lifted;var take=addRenderableWidget(Button.builder(Component.literal("Take it"),b->take(leaf)).bounds(x+230,y,60,18).build());take.active=!taken(pulled,leaf);
            }
            return;
        }
        for(int side=0;side<2;side++){
            int leaf=ZampanoLeaves.first(folder)+side,x=pageX(side);if(taken(pulled,leaf))continue;
            addRenderableWidget(Button.builder(Component.literal("Lift out"),b->{lifted=leaf;turn=0;back=false;rustle();rebuildWidgets();}).bounds(x+8,y,66,18).build());
            addRenderableWidget(Button.builder(Component.literal("Take it"),b->take(leaf)).bounds(x+W-74,y,66,18).build());
        }
    }
    void take(int leaf){
        if(taken(pulled,leaf))return;
        if(!proof&&Minecraft.getInstance().getConnection()!=null)PacketDistributor.sendToServer(new ZampanoDrawerActionPayload(leaf));
    }
    void rustle(){if(!proof)Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN,1.1F,.5F));}

    @Override public void removed(){
        if(!proof&&!holding()&&Minecraft.getInstance().getConnection()!=null)PacketDistributor.sendToServer(new ZampanoDrawerActionPayload(ZampanoDrawerActionPayload.CLOSE));
        super.removed();
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){g.fill(0,0,width,height,0xE01E1A16);}

    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        super.render(g,mouseX,mouseY,partial);
        if(lifted>=0){lifted(g);return;}
        int top=pageY();
        // The folder lies open under its two leaves, its tab at the top left.
        int x0=pageX(0)-6,x1=pageX(1)+W+6;
        g.fill(x0,top-6,x1,top+H+4,FOLDER);g.fill(x0+8,top-12,x0+64,top-6,FOLDER);g.fill(centreX()-1,top-6,centreX()+1,top+H+4,FOLDER_DARK);
        for(int side=0;side<2;side++){
            int leaf=ZampanoLeaves.first(folder)+side,x=pageX(side);
            if(taken(pulled,leaf)){
                // Where the leaf lay, the folder is paler: the sun never reached it.
                g.fill(x+2,top+2,x+W-2,top+H-2,FADE);continue;
            }
            g.enableScissor(x,top,x+W,top+H);
            JournalPageRenderer.leaf(g,font,x,top,leaf,side==0,false);
            g.disableScissor();
        }
    }
    /** One leaf held up: turned a quarter at a time about its middle, and mirrored when it is turned over. */
    void lifted(GuiGraphics g){
        int cx=centreX(),cy=centreY(),w=turn%2==0?W:H,h=turn%2==0?H:W;
        g.enableScissor(cx-w/2,cy-h/2,cx+w/2,cy+h/2);
        var pose=g.pose();pose.pushPose();pose.translate(cx,cy,0);pose.mulPose(Axis.ZP.rotationDegrees(turn*90));
        if(back){pose.scale(-1,1,1);JournalPageRenderer.uncull(g);}
        try{JournalPageRenderer.leaf(g,font,-W/2,-H/2,lifted,true,back);}
        finally{if(back)JournalPageRenderer.recull(g);pose.popPose();g.disableScissor();}
    }
}
