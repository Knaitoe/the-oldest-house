package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LastSeen;
import io.github.knaitoe.theoldesthouse.labyrinth.LastSeenTexts;
import io.github.knaitoe.theoldesthouse.network.LastSeenFolderPayload;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * 0.4.668, Last Seen (docs/LAST_SEEN_0_4_668.md): the folder held open, as the reader's own game draws it. Two of the ten are opened in
 * turn, the first and the sixth, through the real screen with its real payload. Each must draw its card, and a photograph that is a face:
 * many colours, unlike the manila round it. The two photographs must differ. Run in the font check's chain, between the hall changes and
 * the hall's feed.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class LastSeenFolderVisualProof {
    private LastSeenFolderVisualProof(){}
    private static final int[] CHILDREN={LastSeen.ROCIO,5};
    private static boolean active;private static int page,frames;private static final int[] photos=new int[CHILDREN.length];
    static void start(Minecraft mc){active=true;page=0;frames=0;open(mc);}
    private static void open(Minecraft mc){int c=CHILDREN[page];LastSeenFolderScreen.accept(new LastSeenFolderPayload(c,LastSeenTexts.file(c),LastSeenTexts.CHILDREN[c],LastSeenTexts.WHERE[c],LastSeenTexts.DAY[c]));}
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();
        if(!active||!(mc.screen instanceof LastSeenFolderScreen)||++frames<24)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
            image.writeToFile(dir.resolve("native-last-seen-folder-"+page+".png"));
            double scale=mc.getWindow().getGuiScale();
            int x=(mc.getWindow().getGuiScaledWidth()-LastSeenFolderScreen.W)/2,y=(mc.getWindow().getGuiScaledHeight()-LastSeenFolderScreen.H)/2;
            // The card: a point on the manila left of the photograph, under it, is drawn and not the world's dark behind.
            int card=image.getPixelRGBA((int)((x+50)*scale),(int)((y+150)*scale));
            int r=card&0xff,g=card>>8&0xff,b=card>>16&0xff;
            if(!(r>140&&g>120&&b>70&&r>b+30))throw new IllegalStateException("The folder's card is not drawn: "+Integer.toHexString(card));
            // The photograph: its face is many colours.
            var colours=new HashSet<Integer>();int px=x+22,py=y+40;
            for(int yy=0;yy<LastSeenFolderScreen.PHOTO;yy+=2)for(int xx=0;xx<LastSeenFolderScreen.PHOTO;xx+=2)colours.add(image.getPixelRGBA((int)((px+xx)*scale),(int)((py+yy)*scale))&0xffffff);
            if(colours.size()<6)throw new IllegalStateException("The folder's photograph shows no face: "+colours.size()+" colours");
            int hash=0;for(int yy=8;yy<LastSeenFolderScreen.PHOTO;yy+=8)for(int xx=8;xx<LastSeenFolderScreen.PHOTO;xx+=8)hash=hash*31+image.getPixelRGBA((int)((px+xx)*scale),(int)((py+yy)*scale));
            photos[page]=hash;
        }
        if(++page<CHILDREN.length){frames=0;open(mc);return;}
        if(photos[0]==photos[1])throw new IllegalStateException("Two children's folders show the same photograph");
        Files.writeString(dir.resolve("last-seen-folder-passed.txt"),"Two of the ten's folders held open through the real screen: the manila card, each child's own face from their skin in the photograph, and the typed form.\n");
        TheOldestHouse.LOGGER.info("LAST SEEN FOLDER CHECK PASSED: the card and two different faces");
        active=false;mc.setScreen(new HallFeedVisualProof());
    }
}
