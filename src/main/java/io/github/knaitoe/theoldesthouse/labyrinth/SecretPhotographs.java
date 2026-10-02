package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.network.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.saveddata.maps.*;
import java.util.*;

/** Original photographs are finite per owner; projections never consume them. */
public final class SecretPhotographs {
    private SecretPhotographs(){}
    public static void project(ServerPlayer p,CompoundTag own){
        var b=IndianLakeRooms.base(p.server,LabyrinthPlace.KAREN_ROOM);if(b==null)return;var photos=own.getList("Record",Tag.TAG_COMPOUND);if(photos.isEmpty())return;
        var stack=ItemStack.parseOptional(p.registryAccess(),photos.getCompound(Math.floorMod(own.getInt("Projection"),photos.size())));var id=stack.get(DataComponents.MAP_ID);if(id==null)return;
        var data=p.serverLevel().getMapData(id);if(data==null)return;
        p.connection.send(new ClientboundMapItemDataPacket(id,data.scale,data.locked,Collections.emptyList(),new MapItemSavedData.MapPatch(0,0,128,128,data.colors.clone())));
        HousePackets.send(p,new PersonalProjectionPayload(b.offset(0,2,-15),id.id()));
    }
    public static void open(ServerPlayer p){p.openMenu(new SimpleMenuProvider((id,inv,who)->new Album(id,inv,p),Component.literal("Your secret photographs")));}
    public static final class Album extends ChestMenu {
        private final ServerPlayer owner;
        private final Set<Integer> originals=new HashSet<>();
        public Album(int id,Inventory inv,ServerPlayer p){this(id,inv,p,new SimpleContainer(9));}
        private Album(int id,Inventory inv,ServerPlayer p,SimpleContainer tray){
            super(MenuType.GENERIC_9x1,id,inv,tray,1);owner=p;var own=NovelVignettes.personal(LabyrinthData.get(p.server),p.getUUID());var photos=own.getList("Record",Tag.TAG_COMPOUND);
            for(int i=0;i<photos.size();i++){
                var stack=ItemStack.parseOptional(p.registryAccess(),photos.getCompound(i));var map=stack.get(DataComponents.MAP_ID);
                if(map!=null&&!own.getBoolean("PhotoTaken"+map.id())){tray.setItem(i,stack);originals.add(map.id());}
            }
        }
        @Override public boolean stillValid(Player p){return p==owner&&NovelVignettes.inside(owner,LabyrinthPlace.KAREN_ROOM);}
        @Override public void clicked(int slot,int button,ClickType type,Player p){
            if(p!=owner||!stillValid(p))return;
            // Disallow insertion and creative duplication into the album.
            if(slot>=0&&slot<9&&(type==ClickType.CLONE||type==ClickType.SWAP||!getCarried().isEmpty()))return;
            if(type==ClickType.QUICK_CRAFT)return;
            super.clicked(slot,button,type,p);commit();
        }
        @Override public ItemStack quickMoveStack(Player p,int slot){return slot>=9?ItemStack.EMPTY:super.quickMoveStack(p,slot);}
        private void commit(){
            var data=LabyrinthData.get(owner.server);var own=NovelVignettes.personal(data,owner.getUUID());
            for(int id:originals){boolean present=false;for(int i=0;i<9;i++){var map=getSlot(i).getItem().get(DataComponents.MAP_ID);present|=map!=null&&map.id()==id;}if(!present)own.putBoolean("PhotoTaken"+id,true);}
            NovelVignettes.save(data,owner.getUUID(),own);
        }
        @Override public void removed(Player p){commit();super.removed(p);}
    }
}
