package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.network.CompanionOrderPayload;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Native pixels and font, with petting in the centre. The world keeps moving. */
public final class CompanionWheel extends Screen {
    private final int entityId;
    private static final String[] LABELS={"Follow","Stay","Track deeper","Find way out"};
    private static final String[] KEYS={"W / Up","S / Down","D / Right","A / Left"};
    public CompanionWheel(int entityId) {super(Component.literal("Companion"));this.entityId=entityId;}
    public static void open(int id) {
        Minecraft client=Minecraft.getInstance();
        if(client.level!=null&&client.level.getEntity(id)!=null)client.setScreen(new CompanionWheel(id));
    }
    @Override public boolean isPauseScreen(){return false;}
    // Screen's default background invokes the post-process blur. This menu draws its own backdrop.
    @Override public void renderBackground(GuiGraphics gui,int mouseX,int mouseY,float partial) {}
    private int radius(){return Math.max(60,Math.min(112,Math.min(width/2-8,height/2-25)));}
    private int selection(double x,double y) {
        double dx=x-width/2.0,dy=y-height/2.0,d=dx*dx+dy*dy;
        if(d>radius()*radius())return -1;
        if(d<23*23)return CompanionOrders.PET_ACTION;
        return Math.abs(dx)>Math.abs(dy) ? dx>0?2:3 : dy>0?1:0;
    }
    private void choose(int order) {
        PacketDistributor.sendToServer(new CompanionOrderPayload(entityId,order));onClose();
    }
    @Override public void render(GuiGraphics gui,int mouseX,int mouseY,float partial) {
        int cx=width/2,cy=height/2,r=radius(),offset=(int)(r*.65),hover=selection(mouseX,mouseY);
        for(int y=-r;y<=r;y++) {
            int span=(int)Math.sqrt(r*r-y*y);
            gui.fill(cx-span,cy+y,cx+span+1,cy+y+1,0xFF171B1C);
        }
        gui.fill(cx-1,cy-r+6,cx+1,cy+r-5,0xFF424941);
        gui.fill(cx-r+6,cy-1,cx+r-5,cy+1,0xFF424941);
        var entity=minecraft.level==null?null:minecraft.level.getEntity(entityId);
        gui.drawCenteredString(font,entity==null?"Companion":entity.getDisplayName().getString(),cx,Math.max(5,cy-r-17),0xFFF4ECDC);
        ItemStack[] icons={Items.BONE.getDefaultInstance(),Items.LEAD.getDefaultInstance(),
                Items.TORCH.getDefaultInstance(),Items.COMPASS.getDefaultInstance()};
        int[] dx={0,0,offset,-offset},dy={-offset,offset,0,0};
        for(int i=0;i<4;i++) {
            int x=cx+dx[i],y=cy+dy[i];
            String label=r<95&&i>=2?(i==2?"Deeper":"Way out"):LABELS[i];
            int half=Math.max(22,font.width(label)/2+4);
            if(hover==i)gui.fill(x-half,y-23,x+half+1,y+27,0xFF485044);
            gui.renderItem(icons[i],x-8,y-20);
            gui.drawCenteredString(font,label,x,y+1,hover==i?0xFFFFE2A0:0xFFF4ECDC);
            gui.drawCenteredString(font,r<95?KEYS[i].substring(0,1):KEYS[i],x,y+14,0xFFBAC7BE);
        }
        gui.fill(cx-22,cy-17,cx+23,cy+19,hover==CompanionOrders.PET_ACTION?0xFF61644B:0xFF282E29);
        gui.drawCenteredString(font,"Pet",cx,cy-10,0xFFFFE2A0);
        gui.drawCenteredString(font,"E",cx,cy+4,0xFFD6DEC9);
        gui.drawCenteredString(font,"Esc: close · The world keeps moving",cx,Math.min(height-11,cy+r+10),0xFFBAC7BE);
    }
    @Override public boolean mouseClicked(double x,double y,int button) {
        if(button==0){int chosen=selection(x,y);if(chosen>=0){choose(chosen);return true;}}
        return super.mouseClicked(x,y,button);
    }
    @Override public boolean keyPressed(int key,int scan,int mods) {
        int chosen=switch(key) {
            case GLFW.GLFW_KEY_W,GLFW.GLFW_KEY_UP -> 0;
            case GLFW.GLFW_KEY_S,GLFW.GLFW_KEY_DOWN -> 1;
            case GLFW.GLFW_KEY_D,GLFW.GLFW_KEY_RIGHT -> 2;
            case GLFW.GLFW_KEY_A,GLFW.GLFW_KEY_LEFT -> 3;
            case GLFW.GLFW_KEY_E,GLFW.GLFW_KEY_ENTER -> CompanionOrders.PET_ACTION;
            default -> -1;
        };
        if(chosen>=0){choose(chosen);return true;}
        return super.keyPressed(key,scan,mods);
    }
}
