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
        if(StaircaseLeaves.open(p,pos))return true;
        p.openMenu(new SimpleMenuProvider((id,inv,player)->new PageMenu(id,p,pos),Component.literal("A page on the stairs")));return true;
    }
    private static ItemStack original(ServerPlayer p,BlockPos pos){
        var data=LabyrinthData.get(p.server);var all=data.state(ID);var own=all.getCompound(p.getUUID().toString());var books=own.getCompound("Books");String key=Long.toString(pos.asLong());
        var editions=own.getCompound("Editions");
        if(books.contains(key)&&(editions.getInt(key)>=430||own.getCompound("Taken").getBoolean(key)))return ItemStack.parseOptional(p.registryAccess(),books.getCompound(key));
        int number=Math.floorDiv(FinaleArchitecture.TOP-pos.getY(),48);String[] names={"Sabine","E. Marsh","M. L.","Ruth","A. Bell","Jonah"};
        String[] scraps={
            "Tuesday\n\nThree tins left. I opened the peaches with the knife and lost half the juice. Tell Ruth I found her spoon. It's in my coat.",
            "I hung the scarf out to dry at breakfast. By supper the rail had frozen. I cut the cloth away. There's a red strip still caught under the bracket.",
            "Mara's compass is in the tin with the bandages. If she comes after us, give her the whole tin. The needle came loose yesterday.",
            "Jonah,\n\nWe waited until four. I left the lamp burning so you'd find the landing. There was enough oil for an hour. I couldn't stay with it.",
            "The noise at night is the buckle on my bag. It knocks against the rail when I turn over. I wrapped it in a sock. I slept.",
            "We stopped on the wide landing. Bell boiled water; Ruth took off her boots. Nobody said we were lost until the water was ready.",
            "I called down through the bars. A boy asked for his coat. Bell went to fetch ours. I asked his name. He asked for his coat again.",
            "The right rail is loose at the turn. I tightened one bolt with the spoon. The other won't catch. Hold the wall when you pass it.",
            "Ruth says the handwriting is mine. I don't use that loop on the g. I asked her to show me an old letter. She'd burned them for the tea.",
            "Forty feet of rope. Two broken clips.\n\nI tied the ends together and tried my weight on it. The knot held. I'm still sitting here.",
            "My boot was on the landing where we'd eaten. I had both boots on. Mara wouldn't pick it up. Neither would I.",
            "I answered a voice below before I knew what it had said. It called again. This time I heard my mother's name for me. I haven't told Bell."
        };
        HouseWriting.WritingStyle[] hands={HouseWriting.WritingStyle.KAREN,HouseWriting.WritingStyle.WILL,HouseWriting.WritingStyle.ZAMPANO};
        var rendered=new ArrayList<Component>();rendered.add(HouseWriting.page(hands[Math.floorMod(number,hands.length)],scraps[Math.floorMod(number,scraps.length)]));
        // One occasional annotation, tied to an actual original loss. No repeated
        // evidence roll-call or theoretical lecture is appended to every loose page.
        if(Math.floorMod(number,5)==3){
            var loss=MotherCollection.get(p.server).all().stream().filter(e->p.getUUID().equals(e.owner)).findFirst();
            if(loss.isPresent())rendered.add(HouseWriting.page(HouseWriting.WritingStyle.WILL,loss.get().name+"\n\nI wrote it down before I forgot which shelf."));
        }
        ItemStack book=HouseWriting.book("A loose sheet",names[Math.floorMod(number,names.length)],rendered);
        editions.putInt(key,430);own.put("Editions",editions);
        books.put(key,book.save(p.registryAccess()));own.put("Books",books);all.put(p.getUUID().toString(),own);data.setState(ID,all);return book;
    }
    public static void moved(net.minecraft.server.MinecraftServer server,BlockPos from,BlockPos to){
        var data=LabyrinthData.get(server);var all=data.state(ID);String a=Long.toString(from.asLong()),b=Long.toString(to.asLong());
        for(String player:new ArrayList<>(all.getAllKeys())){var own=all.getCompound(player);
            for(String field:List.of("Books","Taken","Editions")){var tags=own.getCompound(field);if(tags.contains(a)&&!tags.contains(b)){tags.put(b,tags.get(a).copy());tags.remove(a);own.put(field,tags);}}
            all.put(player,own);
        }data.setState(ID,all);
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
