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
    public static final String ID="staircase_pages_0427";
    private StaircaseWriting(){}
    public static boolean open(ServerPlayer p,BlockPos pos){
        BlockPos origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null||!FinaleArchitecture.contains(origin,pos))return false;
        if(StaircaseLeaves.open(p,pos))return true;
        p.openMenu(new SimpleMenuProvider((id,inv,player)->new PageMenu(id,p,pos),Component.literal("A page on the stairs")));return true;
    }
    private static ItemStack original(ServerPlayer p,BlockPos pos){
        var data=LabyrinthData.get(p.server);var own=data.stateEntry(ID,p.getUUID().toString());var books=own.getCompound("Books");String key=Long.toString(pos.asLong());
        var editions=own.getCompound("Editions");
        // A read or collected original is immutable, including editions older than this pool.
        if(books.contains(key))return ItemStack.parseOptional(p.registryAccess(),books.getCompound(key));
        var indices=own.getCompound("Indices");var used=own.getCompound("Used");int number=ordinal(HouseSavedData.get(p.server).houseOrigin(),pos);
        int chosen=Math.floorMod(number,StaircaseNotes.TEXTS.size());
        for(int n=0;n<StaircaseNotes.TEXTS.size()&&used.getBoolean(Integer.toString(chosen));n++)chosen=(chosen+1)%StaircaseNotes.TEXTS.size();
        // The last paper ABOVE the chamber anticipates the prisoner. The shaft continues below it.
        // Saved lower-shaft editions remain exact and cannot mint a second copy here.
        boolean panther=pos.equals(pantherLanding(HouseSavedData.get(p.server).houseOrigin()))&&!used.getBoolean(Integer.toString(StaircaseNotes.TEXTS.size()));
        if(panther)chosen=StaircaseNotes.TEXTS.size();
        ItemStack book=panther?StaircaseNotes.panther():StaircaseNotes.specimen(chosen);var content=book.get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT);
        var rendered=new ArrayList<Component>();for(var page:content.pages())rendered.add(page.raw());
        // One occasional annotation, tied to an actual original loss. No repeated
        // evidence roll-call or theoretical lecture is appended to every loose page.
        if(!panther&&Math.floorMod(number,5)==3&&!own.getBoolean("LossNoted")){
            var loss=MotherCollection.get(p.server).all().stream().filter(e->p.getUUID().equals(e.owner)).findFirst();
            if(loss.isPresent()){rendered.add(HouseWriting.page(HouseWriting.WritingStyle.WILL,loss.get().name+"\n\nI wrote it down before I forgot which shelf."));own.putBoolean("LossNoted",true);}
        }
        book=HouseWriting.book(content.title().raw(),content.author(),rendered);
        indices.putInt(key,chosen);used.putBoolean(Integer.toString(chosen),true);own.put("Indices",indices);own.put("Used",used);
        editions.putInt(key,panther?458:448);own.put("Editions",editions);
        books.put(key,book.save(p.registryAccess()));own.put("Books",books);data.setStateEntry(ID,p.getUUID().toString(),own);return book;
    }
    public static List<BlockPos> positions(BlockPos origin){
        var b=FinaleArchitecture.base(origin);var out=new ArrayList<BlockPos>();int turn=0;
        for(var at:FinaleArchitecture.fullRoute(origin))if(Math.abs(at.getX()-b.getX())==FinaleArchitecture.STAIR_RADIUS&&Math.abs(at.getZ()-b.getZ())==FinaleArchitecture.STAIR_RADIUS){
            if(turn%3==1)out.add(FinaleRepairs.paper(at,b,turn));turn++;
        }return List.copyOf(out);
    }
    public static BlockPos pantherLanding(BlockPos origin){
        return positions(origin).stream().filter(p->p.getY()>FinaleArchitecture.ARENA+3)
                .min(java.util.Comparator.comparingInt(BlockPos::getY)).orElseThrow();
    }
    public static int ordinal(BlockPos origin,BlockPos pos){
        var papers=positions(origin);int nearest=0;double best=Double.MAX_VALUE;
        for(int i=0;i<papers.size();i++){double distance=papers.get(i).distSqr(pos);if(distance<best){best=distance;nearest=i;}}return nearest;
    }
    public static void moved(net.minecraft.server.MinecraftServer server,BlockPos from,BlockPos to){
        var data=LabyrinthData.get(server);var all=data.state(ID);String a=Long.toString(from.asLong()),b=Long.toString(to.asLong());
        for(String player:new ArrayList<>(all.getAllKeys())){var own=all.getCompound(player);
            for(String field:List.of("Books","Taken","Editions","Indices")){var tags=own.getCompound(field);if(tags.contains(a)&&!tags.contains(b)){tags.put(b,tags.get(a).copy());tags.remove(a);own.put(field,tags);}}
            all.put(player,own);
        }data.setState(ID,all);
    }
    private static final class PageMenu extends LecternMenu {
        private final ServerPlayer reader;private final BlockPos pos;private final ItemStack book;private final int pages;
        private long lastPageAt;
        PageMenu(int id,ServerPlayer p,BlockPos pos){this(id,p,pos,new SimpleContainer(1));}
        PageMenu(int id,ServerPlayer p,BlockPos pos,SimpleContainer display){super(id,display,new SimpleContainerData(1));reader=p;this.pos=pos.immutable();book=original(p,pos);display.setItem(0,book.copy());pages=book.get(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT).pages().size();lastPageAt=pages==1?p.serverLevel().getGameTime():-1;}
        @Override public void removed(Player p){boolean full=p==reader&&stillValid(p)&&getPage()==pages-1&&lastPageAt>=0&&reader.serverLevel().getGameTime()-lastPageAt>=20;super.removed(p);if(full)StaircaseLeaks.offer(reader,pos,book);}
        @Override public boolean stillValid(Player p){return p==reader&&reader.isAlive()&&reader.distanceToSqr(pos.getCenter())<25&&reader.level().getBlockState(pos).is(HouseBlocks.NOTE_SURFACE.get());}
        @Override public boolean clickMenuButton(Player p,int button){
            if(!stillValid(p))return false;
            if(button==3){if(reader.isSpectator())return false;var data=LabyrinthData.get(reader.server);var own=data.stateEntry(ID,reader.getUUID().toString());var taken=own.getCompound("Taken");String key=Long.toString(pos.asLong());if(taken.getBoolean(key))return false;
                taken.putBoolean(key,true);own.put("Taken",taken);data.setStateEntry(ID,reader.getUUID().toString(),own);var item=book.copy();if(!reader.getInventory().add(item))reader.drop(item,false);reader.inventoryMenu.broadcastChanges();return true;}
            if(button>=100){if(button-100>=pages)return false;}else if(button==1){if(getPage()<=0)return false;}else if(button==2){if(getPage()>=pages-1)return false;}else return false;
            boolean result=super.clickMenuButton(p,button);if(result)lastPageAt=getPage()==pages-1?reader.serverLevel().getGameTime():-1;return result;
        }
    }
}
