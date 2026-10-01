package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.network.ClapCuePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

/** Brief direction and proximity feedback on the cloth; turns still matter. */
public final class ClapCueClient {
    private static BlockPos source;
    private static boolean wardrobe;
    private static long received;
    private ClapCueClient() {}
    public static void accept(ClapCuePayload cue){source=cue.source();wardrobe=cue.wardrobe();received=System.nanoTime();}
    public static void clear(){source=null;}
    public static void render(GuiGraphics gui,int edge) {
        var mc=Minecraft.getInstance();
        if(source==null||mc.player==null||!ClapGameClientState.bound()||ClapGameClientState.ending())return;
        float age=(System.nanoTime()-received)/1_000_000_000F;
        if(age>1.4F)return;
        double dx=source.getX()+.5-mc.player.getX(),dz=source.getZ()+.5-mc.player.getZ();
        double distance=Math.sqrt(dx*dx+dz*dz);
        double bearing=Mth.wrapDegrees(Math.toDegrees(Math.atan2(-dx,dz))-mc.player.getYRot());
        String direction=Math.abs(bearing)<35?"Ahead":Math.abs(bearing)>145?"Behind":bearing<0?"Left":"Right";
        String hint=wardrobe&&distance<3.2?"Open the wardrobe":distance<2?"Close":direction;
        int alpha=(int)(215*Math.max(0,1-age/1.4F));
        if(alpha<8)return;
        int colour=(alpha<<24)|0xE5DED0,w=gui.guiWidth(),x=w/2+(int)(Math.sin(Math.toRadians(bearing))*w*.32);
        // A small pair of traces at the lower cloth edge answers the two hands in the sound.
        int length=distance<3?16:9;
        gui.fill(x-length-3,edge-6,x-3,edge-4,colour);gui.fill(x+3,edge-6,x+length+3,edge-4,colour);
        gui.drawCenteredString(mc.font,hint,w/2,Math.max(8,edge-23),colour);
    }
}
