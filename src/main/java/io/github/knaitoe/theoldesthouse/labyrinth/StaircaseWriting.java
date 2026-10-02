package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Finite personal loose pages, bound once from actual in-game losses and resolutions. */
public final class StaircaseWriting {
    private static final String ID="staircase_pages_0427";
    private StaircaseWriting(){}
    public static boolean open(ServerPlayer p,BlockPos pos){
        BlockPos origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null||!FinaleArchitecture.contains(origin,pos))return false;
        p.openMenu(new SimpleMenuProvider((id,inv,player)->new PageMenu(id,p,pos),Component.literal("A page on the stairs")));return true;
    }
    private static ItemStack original(ServerPlayer p,BlockPos pos){
        var data=LabyrinthData.get(p.server);var all=data.state(ID);var own=all.getCompound(p.getUUID().toString());var books=own.getCompound("Books");String key=Long.toString(pos.asLong());
        if(books.contains(key))return ItemStack.parseOptional(p.registryAccess(),books.getCompound(key));
        int number=Math.floorDiv(FinaleArchitecture.TOP-pos.getY(),48);String[] names={"Sabine","E. Marsh","M. L.","Ruth","A. Bell","Jonah"};
        String[] scraps={
            "I counted the landings. I counted them again. The second number was smaller. There were more stairs.",
            "I left my red scarf on the rail. Three hours later it was below me. I had never turned around.",
            "Mara's brass compass. Theo's lunch tin. My brother's left boot. I wrote their names so I would know which things to leave behind.",
            "Please do not follow the light. I put it there because I could no longer bear the dark. I am not a way out.",
            "I sleep facing the wall. The sound stops when I turn toward the stairs. I have stopped turning.",
            "There was a landing for rest. Then another. A place that expects you to rest expects you to wake up here.",
            "The child below asked me to open the bars. His voice did not sound far away. I was still a day above him.",
            "The stone is warm where a hand would rest. No hands have passed me. I keep my own against my coat.",
            "If these words are mine, why did I find them before I wrote them? If they are yours, stop here. Stop before the door.",
            "I have enough rope to reach the next landing. I have enough rope to reach the next landing. I have enough rope to reach—",
            "Someone has been collecting the things we lose. That is not the same as returning them.",
            "I can hear my name below. I can hear my name above. One voice is running out of breath."
        };
        var pages=new ArrayList<String>();pages.add("STAIR PAGE "+(number+1)+"\n\n"+scraps[Math.floorMod(number,scraps.length)]);
        var losses=MotherCollection.get(p.server).all().stream().filter(e->p.getUUID().equals(e.owner)).map(e->e.name).distinct().limit(8).toList();
        if(!losses.isEmpty())pages.add("Someone has written the names beneath the older ink:\n\n"+String.join("\n",losses)+"\n\nThe names were not here when the page was torn.");
        var seen=Arrays.stream(WitnessAccount.Story.values()).filter(s->WitnessAccount.has(data,p.getUUID(),s)).toList();
        if(!seen.isEmpty())pages.add("A second hand:\n\n"+seen.get(Math.floorMod(number,seen.size())).text+"\n\nThe stairs do not forget what came down them.");
        if(IndianLakeProgress.hasThrown(data,p.getUUID()))pages.add("You let go at the water.\n\nThe water has not let go of you.");
        else if(IndianLakeProgress.wasHunted(data,p.getUUID()))pages.add("You have heard something approach behind you.\n\nA railing only protects you from one direction.");
        else pages.add("Below the bars, something is waiting to hear how you hold a weapon.\n\nDo not mistake waiting for sleep.");
        var traces=HouseExperience.traces(p);if(!traces.isEmpty())pages.add(traces.get(Math.floorMod(number,traces.size())));
        var rendered=new ArrayList<Component>();for(String page:pages)rendered.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,page));rendered.addAll(ThresholdWriting.pages(number));
        ItemStack book=HouseWriting.book("Stair page "+(number+1),names[Math.floorMod(number,names.length)],rendered);
        books.put(key,book.save(p.registryAccess()));own.put("Books",books);all.put(p.getUUID().toString(),own);data.setState(ID,all);return book;
    }
    private static final class PageMenu extends LecternMenu {
        private final ServerPlayer reader;private final BlockPos pos;private final ItemStack book;private final int pages;
        PageMenu(int id,ServerPlayer p,BlockPos pos){this(id,p,pos,new SimpleContainer(1));}
        PageMenu(int id,ServerPlayer p,BlockPos pos,SimpleContainer display){super(id,display,new SimpleContainerData(1));reader=p;this.pos=pos.immutable();book=original(p,pos);display.setItem(0,book.copy());pages=book.get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).pages().size();}
        @Override public boolean stillValid(Player p){return p==reader&&reader.isAlive()&&reader.distanceToSqr(pos.getCenter())<25&&reader.level().getBlockState(pos).is(HouseBlocks.NOTE_SURFACE.get());}
        @Override public boolean clickMenuButton(Player p,int button){
            if(!stillValid(p))return false;
            if(button==3){var data=LabyrinthData.get(reader.server);var all=data.state(ID);var own=all.getCompound(reader.getUUID().toString());var taken=own.getCompound("Taken");String key=Long.toString(pos.asLong());if(taken.getBoolean(key))return false;
                taken.putBoolean(key,true);own.put("Taken",taken);all.put(reader.getUUID().toString(),own);data.setState(ID,all);var item=book.copy();if(!reader.getInventory().add(item))reader.drop(item,false);reader.inventoryMenu.broadcastChanges();return true;}
            if(button>=100){if(button-100>=pages)return false;}else if(button==1){if(getPage()<=0)return false;}else if(button==2){if(getPage()>=pages-1)return false;}else return false;
            return super.clickMenuButton(p,button);
        }
    }
}
