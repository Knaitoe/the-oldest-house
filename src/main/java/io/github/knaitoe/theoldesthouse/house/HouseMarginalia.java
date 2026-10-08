package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Per-reader, depth-gated serial writing, displayed through vanilla's lectern/book interface. */
public final class HouseMarginalia {
    public static final String ID = "house_marginalia";
    public enum Thread implements StringRepresentable {
        HOUSEKEEPING(4), CALLS(4), ROOM(4), POEMS(5);
        public final int chapters;
        Thread(int chapters) { this.chapters = chapters; }
        @Override public String getSerializedName() { return name().toLowerCase(java.util.Locale.ROOT); }
    }
    private HouseMarginalia() {}
    public static int band(int depth) { return depth < 6 ? 0 : depth < 8 ? 1 : depth < 12 ? 2 : depth < 16 ? 3 : 4; }
    public static CompoundTag record(LabyrinthData data, UUID player) { return data.state(ID).getCompound(player.toString()).copy(); }
    private static void save(LabyrinthData data, UUID player, CompoundTag own) { CompoundTag state=data.state(ID); state.put(player.toString(),own); data.setState(ID,state); }
    public static int next(LabyrinthData data, UUID player, Thread thread) { return record(data,player).getCompound("Next").getInt(thread.getSerializedName()); }
    static void finishLegacy(ServerPlayer player, String binding, Thread thread, int chapter) {
        if (chapter < 0) return;
        LabyrinthData data=LabyrinthData.get(player.server); CompoundTag own=record(data,player.getUUID());
        CompoundTag bindings=own.getCompound("Bindings"), entry=bindings.getCompound(binding);
        if (entry.getBoolean("Finished")) return;
        entry.putBoolean("Finished",true); bindings.put(binding,entry); own.put("Bindings",bindings);
        CompoundTag progress=own.getCompound("Next"); String key=thread.getSerializedName();
        progress.putInt(key,Math.max(progress.getInt(key),chapter+1)); own.put("Next",progress); save(data,player.getUUID(),own);
    }
    static void takeLegacy(ServerPlayer player,String binding) {
        var data=LabyrinthData.get(player.server);var own=record(data,player.getUUID());
        var bindings=own.getCompound("Bindings");var entry=bindings.getCompound(binding);
        entry.putBoolean("Taken",true);bindings.put(binding,entry);own.put("Bindings",bindings);save(data,player.getUUID(),own);
    }
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || event.getHand()!=InteractionHand.MAIN_HAND || !(event.getEntity() instanceof ServerPlayer player)
                || !player.level().dimension().equals(HouseDimensions.INTERIOR)
                || !player.isAlive() || player.gameMode.getGameModeForPlayer()==GameType.SPECTATOR || player.distanceToSqr(event.getPos().getCenter())>25) return;
        var state=player.level().getBlockState(event.getPos());
        if (!(state.getBlock() instanceof NoteSurfaceBlock)) return;
        Thread thread=state.getValue(NoteSurfaceBlock.THREAD); BlockPos pos=event.getPos().immutable();
        event.setCanceled(true); event.setCancellationResult(InteractionResult.SUCCESS);
        if(io.github.knaitoe.theoldesthouse.labyrinth.StaircaseWriting.open(player,pos))return;
        if(ExpeditionInquiry.paper(player,pos,thread))return;
        // The provider creates a separate container and page counter for every reader.
        player.openMenu(new SimpleMenuProvider((id,inventory,reader)->new NotebookMenu(id,player,pos,thread),Component.literal("Loose writing")));
    }
    public static final class NotebookMenu extends LecternMenu {
        private final ServerPlayer reader;
        private final BlockPos surface;
        private final Thread thread;
        private final HouseCorrespondence.Encounter encountered;
        private final SimpleContainer pages;
        private boolean taken;
        public NotebookMenu(int id, ServerPlayer reader, BlockPos surface, Thread thread) {
            this(id,reader,surface,thread,new SimpleContainer(1),new SimpleContainerData(1));
        }
        private NotebookMenu(int id, ServerPlayer reader, BlockPos surface, Thread thread, SimpleContainer pages, SimpleContainerData page) {
            super(id,pages,page); this.reader=reader; this.surface=surface.immutable(); this.thread=thread; this.pages=pages;
            encountered=HouseCorrespondence.bind(reader,surface,thread);
            taken=encountered.taken();pages.setItem(0,encountered.book().copy());
            if (book().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()==1) HouseCorrespondence.finish(reader,encountered,thread);
        }
        public ItemStack book() { return pages.getItem(0).copy(); }
        @Override public boolean stillValid(Player player) {
            return player==reader && player.isAlive() && validSurface() && !pages.isEmpty();
        }
        @Override public boolean clickMenuButton(Player player,int button) {
            if (player!=reader || !validSurface() || pages.isEmpty()) return false;
            int count=book().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size();
            if (button>=100 && (button-100<0 || button-100>=count)) return false;
            if (button==1 && getPage()<=0 || button==2 && getPage()>=count-1) return false;
            if (button==3 && (taken||HouseCorrespondence.taken(reader,encountered))) return false;
            boolean changed=super.clickMenuButton(player,button);
            if (!changed) return false;
            if (button==3) {
                taken=true;HouseCorrespondence.take(reader,encountered,thread);
            } else if (getPage()>=count-1) HouseCorrespondence.finish(reader,encountered,thread);
            return true;
        }
        private boolean validSurface() { return reader.isAlive() && reader.level().dimension().equals(HouseDimensions.INTERIOR)
                && reader.gameMode.getGameModeForPlayer()!=GameType.SPECTATOR && reader.distanceToSqr(surface.getCenter())<=64
                && reader.level().getBlockState(surface).getBlock() instanceof NoteSurfaceBlock; }
    }
    /** Operator previews are ordinary immutable books; they never write reading progress. */
    public static List<ItemStack> samples(ServerPlayer reader) {
        var result=new java.util.ArrayList<ItemStack>();
        for(Thread thread:Thread.values()) for(int chapter=0;chapter<thread.chapters;chapter++) result.add(MarginaliaTexts.book(reader,thread,chapter));
        return result;
    }
}
