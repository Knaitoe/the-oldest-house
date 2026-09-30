package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.network.CompanionOrderPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** A four-way wheel: mouse, arrows or WASD. Opening it never pauses a dangerous room. */
public final class CompanionWheel extends Screen {
    private final int entityId;
    private static final String[] LABELS={"Follow","Stay","Track deeper","Find way out"};
    private static final String[] KEYS={"W / Up","S / Down","D / Right","A / Left"};
    private static final int[] DX={0,0,70,-70}, DY={-70,70,0,0};
    public CompanionWheel(int entityId) {super(Component.literal("Companion"));this.entityId=entityId;}
    public static void open(int id) {
        Minecraft client=Minecraft.getInstance();
        if(client.level!=null&&client.level.getEntity(id)!=null)client.setScreen(new CompanionWheel(id));
    }
    @Override public boolean isPauseScreen(){return false;}
    private int selection(double x,double y) {
        double dx=x-width/2.0,dy=y-height/2.0,d=dx*dx+dy*dy;
        if(d<24*24||d>116*116)return -1;
        return Math.abs(dx)>Math.abs(dy) ? dx>0?2:3 : dy>0?1:0;
    }
    private void choose(int order) {
        PacketDistributor.sendToServer(new CompanionOrderPayload(entityId,order));
        onClose();
    }
    @Override public void render(GuiGraphics gui,int mouseX,int mouseY,float partial) {
        int cx=width/2,cy=height/2,hover=selection(mouseX,mouseY);
        for(int y=-116;y<=116;y++) {
            int span=(int)Math.sqrt(116*116-y*y);
            gui.fill(cx-span,cy+y,cx+span+1,cy+y+1,0xDD171B1C);
        }
        gui.fill(cx-1,cy-107,cx+1,cy+108,0x665D625E);
        gui.fill(cx-107,cy-1,cx+108,cy+1,0x665D625E);
        var entity=minecraft.level==null?null:minecraft.level.getEntity(entityId);
        String name=entity==null?"Companion":entity.getDisplayName().getString();
        gui.drawCenteredString(font,name,cx,Math.max(8,cy-136),0xEEE6D4);
        ItemStack[] icons={Items.BONE.getDefaultInstance(),Items.LEAD.getDefaultInstance(),
                Items.TORCH.getDefaultInstance(),Items.COMPASS.getDefaultInstance()};
        for(int i=0;i<4;i++) {
            int x=cx+DX[i],y=cy+DY[i];
            if(hover==i)gui.fill(x-51,y-23,x+52,y+27,0xAA565B4E);
            gui.renderItem(icons[i],x-8,y-20);
            gui.drawCenteredString(font,LABELS[i],x,y+1,hover==i?0xFFF0C5:0xDAD6C7);
            gui.drawCenteredString(font,KEYS[i],x,y+14,0x9FABA3);
        }
        gui.drawCenteredString(font,"Esc",cx,cy-4,0xA8A99F);
        gui.drawCenteredString(font,"The world keeps moving.",cx,Math.min(height-14,cy+127),0xB2B7AB);
        super.render(gui,mouseX,mouseY,partial);
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
            default -> -1;
        };
        if(chosen>=0){choose(chosen);return true;}
        return super.keyPressed(key,scan,mods);
    }
}
