package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LastSeen;
import io.github.knaitoe.theoldesthouse.labyrinth.LastSeenTexts;
import io.github.knaitoe.theoldesthouse.network.LastSeenDrawerPayload;
import io.github.knaitoe.theoldesthouse.network.LastSeenFolderPayload;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
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
 * <p>0.4.671: then Nora's drawer, through the real screen with its real payload: the client's own folder, its photograph the client's own
 * face from its own skin, and a second reader's under it, reached with the screen's own page turn. Each must draw its card and a face, and
 * the two faces must differ (the second reader is given a default skin unlike the client's).
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class LastSeenFolderVisualProof {
    private LastSeenFolderVisualProof(){}
    private static final int[] CHILDREN={LastSeen.ROCIO,5};
    /** Two children's folders, then the drawer's two pages. */
    private static final int PAGES=CHILDREN.length+2;
    private static boolean active;private static int page,frames;private static final int[] photos=new int[PAGES];
    static void start(Minecraft mc){active=true;page=0;frames=0;open(mc);}
    private static void open(Minecraft mc){
        if(page<CHILDREN.length){int c=CHILDREN[page];LastSeenFolderScreen.accept(new LastSeenFolderPayload(c,LastSeenTexts.file(c),LastSeenTexts.CHILDREN[c],LastSeenTexts.WHERE[c],LastSeenTexts.DAY[c]));return;}
        if(page==CHILDREN.length){
            // The proof runs from the title screen, with no player: the account itself stands for the client.
            var me=mc.getUser().getProfileId();var mine=LastSeenFolderScreen.skin(me).texture();
            // A second reader whose default skin is not the client's, so the two photographs cannot be the same face.
            var other=java.util.UUID.nameUUIDFromBytes("last-seen-other".getBytes());
            for(int i=0;net.minecraft.client.resources.DefaultPlayerSkin.get(other).texture().equals(mine)&&i<64;i++)other=java.util.UUID.nameUUIDFromBytes(("last-seen-other-"+i).getBytes());
            LastSeenFolderScreen.drawer(new LastSeenDrawerPayload(List.of(
                new LastSeenDrawerPayload.Folder(me,LastSeenTexts.FORM_PLAIN,LastSeenTexts.file(me),mc.getUser().getName(),LastSeenTexts.takenOn(3,13000),LastSeenTexts.note("HAVEN")),
                new LastSeenDrawerPayload.Folder(other,LastSeenTexts.FORM,LastSeenTexts.file(other),"Wren",LastSeenTexts.takenOn(11,500),LastSeenTexts.note("")))));
            return;
        }
        if(mc.screen instanceof LastSeenFolderScreen folder){
            if(folder.pages()!=2)throw new IllegalStateException("The drawer's screen holds "+folder.pages()+" folders, not 2");
            folder.turn(1);
        }
    }
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
        if(++page<PAGES){frames=0;open(mc);return;}
        if(photos[0]==photos[1])throw new IllegalStateException("Two children's folders show the same photograph");
        if(photos[2]==photos[3])throw new IllegalStateException("The drawer's two readers' folders show the same photograph");
        if(mc.screen instanceof LastSeenFolderScreen folder&&folder.page()!=1)throw new IllegalStateException("The drawer's page did not turn");
        Files.writeString(dir.resolve("last-seen-folder-passed.txt"),"Two of the ten's folders held open through the real screen: the manila card, each child's own face from their skin in the photograph, and the typed form. "
            +"Then Nora's drawer: the client's own folder with its own face, and a second reader's on the next page, with a different face.\n");
        TheOldestHouse.LOGGER.info("LAST SEEN FOLDER CHECK PASSED: the card, two children's faces and two readers' faces in the drawer");
        active=false;mc.setScreen(new HallFeedVisualProof());
    }
}
