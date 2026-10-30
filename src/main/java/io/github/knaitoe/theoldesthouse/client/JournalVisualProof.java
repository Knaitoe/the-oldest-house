package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseText;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * Native proof of the journal screen: the cover and a written page, then a spread with every shape the House can set on a
 * page (a turned line, a spiral, a cascade, a column, a box, a mirrored line) dog-eared, then the same spread torn and
 * tide-marked. The second
 * view must carry the blue House word and the red Minotaur, because a journal page is literature. The fourth is a spread with
 * two pencil rubbings and their lines, written beside; then the other hand's ink in its own lettering. Then Zampanò's courtyard
 * pages on the same renderer: each of the nine folders (one with a leaf already taken), and three leaves lifted out: one
 * turned over to read its mirrored box, one turned upside down, one turned on its side.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class JournalVisualProof {
    private static int state=-1,frames;
    static final int JOURNAL=5,FOLDERS=io.github.knaitoe.theoldesthouse.journal.ZampanoLeaves.FOLDERS,LIFTED=3,STATES=JOURNAL+FOLDERS+LIFTED;
    /** Leaf 3 is taken from the second folder; the third folder's struck Minotaur is red and the eighth folder's house is blue. */
    static final int TAKEN=1<<3,RED_FOLDER=2,BLUE_FOLDER=7;
    private JournalVisualProof(){}
    static void begin(){state=0;frames=0;show();}
    private static void show(){
        var mc=Minecraft.getInstance();
        if(state>=JOURNAL){int c=state-JOURNAL;
            mc.setScreen(c<FOLDERS?ZampanoScreen.proof(c,-1,TAKEN,0,false):switch(c-FOLDERS){case 0->ZampanoScreen.proof(2,5,0,0,true);case 1->ZampanoScreen.proof(3,7,0,2,false);default->ZampanoScreen.proof(7,15,0,1,false);});return;}
        mc.setScreen(switch(state){case 0->JournalScreen.proof(sample(0),0);case 1->JournalScreen.proof(sample(2),1);case 2->JournalScreen.proof(sample(4),1);case 3->JournalScreen.proof(sample(1),2);default->JournalScreen.proof(sample(1),3);});
    }
    static CompoundTag run(String text,String shape,float x,float y,float angle,float size,boolean mirrored){var t=new CompoundTag();t.putString("Text",text);t.putString("Shape",shape);t.putFloat("X",x);t.putFloat("Y",y);t.putFloat("Angle",angle);t.putFloat("Size",size);if(mirrored)t.putBoolean("Mirrored",true);return t;}
    static CompoundTag page(String text,String begun,int day,CompoundTag... runs){var t=new CompoundTag();t.putString("Text",text);var list=new ListTag();for(var r:runs)list.add(r);t.put("Placed",list);t.putString("Begun",begun);t.putInt("Day",day);t.putBoolean("House",true);return t;}
    static ListTag rubbings(String id,float angle){var r=new CompoundTag();r.putString("Id",id);r.putFloat("X",io.github.knaitoe.theoldesthouse.journal.Journals.RUBBING_X);r.putFloat("Y",io.github.knaitoe.theoldesthouse.journal.Journals.RUBBING_Y);r.putFloat("Angle",angle);var list=new ListTag();list.add(r);return list;}
    static CompoundTag note(String mark,String text,String word,int use,float x,float y){var t=new CompoundTag();t.putString("Mark",mark);if(!text.isEmpty())t.putString("Text",text);if(!word.isEmpty()){t.putString("Word",word);t.putInt("Use",use);}t.putFloat("X",x);t.putFloat("Y",y);return t;}
    static CompoundTag sample(int wear){
        var journal=new CompoundTag();journal.putString("Name","OTHProof");journal.putInt("Wear",wear);var pages=new CompoundTag();
        pages.put("1",page("I came in by the front door at noon. The hall was longer than the outside of the house. I measured it twice and wrote both numbers down.\n\nLeft at the lamp.","the long hall",3,run("it is longer than it was","line",.14F,.78F,-7,1,false)));
        pages.put("2",page("","the stair",4,run("the house keeps the measure when I forget it","spiral",.5F,.32F,0,1,false),run("down and down the stairs into the dark","cascade",.12F,.64F,6,1,false)));
        pages.put("3",page("Three doors on the landing.","the landing",4,run("DO NOT open the third door. The Minotaur waits under the stair.","box",.16F,.2F,-4,1,false),run("left at the lamp, then left again, then","column",.68F,.5F,0,1,false),run("turn back","line",.3F,.86F,12,1.4F,true)));
        var rubbed=page("Room 217. The bell rang twice and nobody came.","the Stanley",5);rubbed.put("Rubbings",rubbings("room_number",-3));pages.put("4",rubbed);
        var gouges=page("Under the trunk in the archive.","the courtyard",6);gouges.put("Rubbings",rubbings("gouges",4));pages.put("5",gouges);
        // The other hand: a name struck through, an answer under a line, and the blank page after the last.
        var struck=page("I followed Tess down to the water.\nWe went left at the stair and nobody said a word.","the lake house",7);
        var ink=new ListTag();ink.add(note("struck","","Tess",0,0,0));ink.add(note("direction","No. Right.","left",0,.11F,.2F));struck.put("Notes",ink);pages.put("6",struck);
        var blank=new CompoundTag();var written=new ListTag();written.add(note("written",io.github.knaitoe.theoldesthouse.journal.CoAuthor.BLANK,"",0,.16F,.36F));blank.put("Notes",written);pages.put("7",blank);
        journal.put("Pages",pages);return journal;
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(state<0||!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof JournalScreen||mc.screen instanceof ZampanoScreen)||++frames<4)return;
        var folder=Path.of("../build/font-smoke");
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
            int blue=0,red=0,ink=0,other=0;
            for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){int c=image.getPixelRGBA(x,y);int r=c&255,g=c>>8&255,b=c>>16&255;
                if(near(r,g,b,HouseText.INK_BLUE))blue++;if(near(r,g,b,HouseText.INK_RED))red++;if(near(r,g,b,JournalPageRenderer.INK&0xFFFFFF)||near(r,g,b,JournalPageRenderer.BACK_INK&0xFFFFFF))ink++;if(near(r,g,b,JournalPageRenderer.OTHER_INK&0xFFFFFF))other++;}
            if(state==3){int missing=0;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){int c=image.getPixelRGBA(x,y);if((c&255)>200&&(c>>8&255)<60&&(c>>16&255)>200)missing++;}
                if(missing>10)throw new IllegalStateException("The rubbings must draw their own charcoal, not the missing texture: "+missing);}
            if(state==4&&other<60)throw new IllegalStateException("The other hand must leave its ink: a stroke, an answer and a written page: "+other);
            if(state==1&&(blue<8||red<8))throw new IllegalStateException("The journal page must show the blue House word and the red Minotaur: blue="+blue+" red="+red);
            if(state>=JOURNAL&&ink<40)throw new IllegalStateException("Zampano's page "+(state-JOURNAL)+" must carry his writing: ink="+ink);
            if(state==JOURNAL+RED_FOLDER&&red<8)throw new IllegalStateException("Zampano's struck Minotaur must be red: red="+red);
            if(state==JOURNAL+BLUE_FOLDER&&blue<8)throw new IllegalStateException("Zampano's house must be blue: blue="+blue);
            image.writeToFile(folder.resolve(state<JOURNAL?"native-journal-"+state+".png":"native-courtyard-"+(state-JOURNAL)+".png"));
        }
        frames=0;state++;
        if(state<STATES){show();return;}
        state=-1;
        Files.writeString(folder.resolve("journal-passed.txt"),"The journal screen: the name inside the cover and a written page; every placed shape (a turned line, a spiral, a cascade, a column, a box, a mirrored line) on dog-eared and torn, tide-marked paper; the blue House word and the red Minotaur on the page; two pencil rubbings with their lines; the other hand's ink (a struck name, an answer under a line, a blank page written on). Zampano's nine courtyard folders in his hand, one leaf taken, the struck Minotaur red and the house blue; a leaf turned over, one upside down and one on its side.\n");
        TheOldestHouse.LOGGER.info("JOURNAL CHECK PASSED: five journal and twelve courtyard native views");mc.setScreen(new NpcVisualProof());
    }
    static boolean near(int r,int g,int b,int rgb){return Math.abs(r-(rgb>>16&255))<10&&Math.abs(g-(rgb>>8&255))<10&&Math.abs(b-(rgb&255))<10;}
}
