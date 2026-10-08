package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** One optional investigation, based on actual crossings and finite personally kept originals. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class ExpeditionInquiry {
    public static final String ID="expedition_inquiry_0461";
    private static final String OWNER="InquiryReader",DOCUMENT="InquiryDocument";
    private static final String FIELD="field",PLAN="plan",MARGIN="margin",RETURN="return";
    private static final String PLAN_TEXT="Three offsets between the entrance and the service door. The passage is drawn as a single line. No returning branch is marked.\n\nIn the lower margin: keep walking forward.\n\nThe paper has been folded through the last measurement. No date survives.";
    private static final String MARGIN_TEXT="I did turn back. The bend I had just passed was ahead of me again. I could not make my count fit the straight line on the plan.\n\nPerhaps this was another corridor. I left before I could check.\n\nThe fold goes through the word another. Take this back to the hallway if you can bear to count again.";
    private ExpeditionInquiry(){}
    public static CompoundTag record(ServerPlayer p){return LabyrinthData.get(p.server).stateEntry(ID,p.getUUID().toString());}
    private static void save(ServerPlayer p,CompoundTag own){LabyrinthData.get(p.server).setStateEntry(ID,p.getUUID().toString(),own);}
    private static boolean active(ServerPlayer p){return p.isAlive()&&!p.isSpectator();}
    private static long step(ServerPlayer p){return HouseCorrespondence.record(LabyrinthData.get(p.server),p.getUUID()).getLong("Step");}
    private static boolean inHall(ServerPlayer p){var origin=HouseSavedData.get(p.server).houseOrigin();return origin!=null&&p.level().dimension().equals(HouseDimensions.INTERIOR)
            &&LabyrinthPlaces.placeAt(origin,p.blockPosition())==LabyrinthPlace.LONG_HALLWAY;}
    private static boolean desk(ServerPlayer p,BlockPos at){
        var s=p.level().getBlockState(at);return p.level().dimension().equals(HouseDimensions.INTERIOR)&&s.getBlock() instanceof HouseholdFurnitureBlock
                &&(s.getValue(HouseholdFurnitureBlock.KIND)==HouseholdFurnitureBlock.Kind.WALNUT_DESK||s.getValue(HouseholdFurnitureBlock.KIND)==HouseholdFurnitureBlock.Kind.READING_DESK);
    }
    private static ItemStack original(ServerPlayer p,String id){return ItemStack.parseOptional(p.registryAccess(),record(p).getCompound("Books").getCompound(id));}
    private static String document(ItemStack book){return book.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getString(DOCUMENT);}
    private static boolean exact(ServerPlayer p,ItemStack book){
        var tag=book.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();String id=tag.getString(DOCUMENT);
        return tag.hasUUID(OWNER)&&p.getUUID().equals(tag.getUUID(OWNER))&&!id.isEmpty()&&ItemStack.isSameItemSameComponents(book,original(p,id));
    }
    private static void bind(ServerPlayer p,String id,String title,String author,HouseWriting.WritingStyle style,String text){
        var own=record(p);var books=own.getCompound("Books");if(books.contains(id))return;
        var note=new CorrespondenceTexts.Note("inquiry_"+id,title,author,style,"",0,0,HouseMarginalia.Thread.ROOM,text);
        var book=CorrespondenceTexts.book(note,Map.of());CustomData.update(DataComponents.CUSTOM_DATA,book,t->{t.putUUID(OWNER,p.getUUID());t.putString(DOCUMENT,id);});
        books.put(id,book.save(p.registryAccess()));own.put("Books",books);save(p,own);
    }
    private static String fieldText(int forward,int reverse,boolean revisited){
        return "The long hallway\n\nForward repeats I crossed: "+forward+".\nReverse repeats I crossed: "+reverse+".\n\n"
                +"I checked this wall myself. These are counts of my crossings, not a measure of the whole corridor.\n\n"
                +(revisited?"The other sheet is still in its own hand. I will keep both versions. Neither tells me who walked here first.":"I will keep this sheet. If a plan turns up, I can lay the originals together on a writing desk.");
    }
    public static List<ItemStack> specimens(){
        var out=new ArrayList<ItemStack>();String[] titles={"A bend counted twice","Service passage: a plan","On the back of another plan","Back at the same bend"};
        String[] texts={fieldText(12,3,false),PLAN_TEXT,MARGIN_TEXT,fieldText(2,1,true)};
        var styles=new HouseWriting.WritingStyle[]{HouseWriting.WritingStyle.WILL,HouseWriting.WritingStyle.ZAMPANO,HouseWriting.WritingStyle.KAREN,HouseWriting.WritingStyle.WILL};
        for(int i=0;i<4;i++)out.add(CorrespondenceTexts.book(new CorrespondenceTexts.Note("inquiry_specimen_"+i,titles[i],"Native specimen",styles[i],"",0,0,HouseMarginalia.Thread.ROOM,texts[i]),Map.of()));
        return List.copyOf(out);
    }
    private static void say(ServerPlayer p,String words){p.displayClientMessage(Component.literal(words),false);}
    @SubscribeEvent public static void use(PlayerInteractEvent.RightClickBlock e){
        if(e.isCanceled()||e.getHand()!=InteractionHand.MAIN_HAND||!(e.getEntity() instanceof ServerPlayer p)||!active(p)||p.distanceToSqr(e.getPos().getCenter())>25)return;
        if(desk(p,e.getPos())&&!record(p).getCompound("Books").isEmpty()){
            e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
            say(p,"Lay your original hallway papers together here. You can take them back whenever you leave.");
            p.openMenu(new SimpleMenuProvider((id,inv,reader)->new DeskMenu(id,p,e.getPos()),Component.literal("Your hallway papers")));return;
        }
        if(!inHall(p)||!p.isShiftKeyDown()||!p.getMainHandItem().isEmpty()||!e.getFace().getAxis().isHorizontal()
                ||!p.level().getBlockState(e.getPos()).is(Blocks.LIGHT_GRAY_TERRACOTTA))return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        var observed=LabyrinthLoops.observations(p);var own=record(p);
        if(observed.getInt("Forward")==0){say(p,"Nothing to compare yet. Follow the next bend, then check the wall again.");return;}
        String id=FIELD;
        if(own.getBoolean("Read_margin")&&step(p)>own.getLong("MarginStep")){
            if(observed.getInt("Backward")==0){say(p,"The other account describes coming back. You have only checked this visit in one direction.");return;}
            id=RETURN;
        }
        bind(p,id,id.equals(FIELD)?"A bend counted twice":"Back at the same bend",p.getGameProfile().getName(),HouseWriting.WritingStyle.WILL,
                fieldText(observed.getInt("Forward"),observed.getInt("Backward"),id.equals(RETURN)));
        own=record(p);if(!own.contains("FieldStep")){own.putLong("FieldStep",step(p));save(p,own);}
        if(id.equals(RETURN)&&!own.getBoolean("Revisited")){own.putBoolean("Revisited",true);save(p,own);PlaytestLog.event(p,"inquiry_revisit","forward",observed.getInt("Forward"),"backward",observed.getInt("Backward"));}
        open(p,id,e.getPos(),false);
    }
    /** Called at an unbound ordinary paper, preserving every existing correspondence binding. */
    public static boolean paper(ServerPlayer p,BlockPos at,HouseMarginalia.Thread thread){
        if(!active(p)||thread!=HouseMarginalia.Thread.ROOM||inHall(p))return false;
        var d=LabyrinthData.get(p.server);String key=at.asLong()+":"+thread.getSerializedName()+":"+HouseMarginalia.band(d.returnDepth(p.getUUID()));
        var old=HouseMarginalia.record(d,p.getUUID()).getCompound("Bindings").getCompound(key);
        var bound=HouseCorrespondence.record(d,p.getUUID()).getCompound("Bindings").getCompound(key);
        if(old.contains("Book")||bound.contains("Step")&&bound.getLong("Step")==step(p))return false;
        var own=record(p);String id;
        if(own.getBoolean("Read_field")&&!own.getBoolean("Taken_plan")&&step(p)>=own.getLong("FieldStep")+2){
            id=PLAN;bind(p,id,"Service passage: a plan","An unsigned survey",HouseWriting.WritingStyle.ZAMPANO,
                    PLAN_TEXT);
        }else if(own.getBoolean("Compared")&&!own.getBoolean("Taken_margin")&&step(p)>=own.getLong("ComparedStep")+2){
            id=MARGIN;bind(p,id,"On the back of another plan","A different hand",HouseWriting.WritingStyle.KAREN,
                    MARGIN_TEXT);
        }else return false;
        open(p,id,at,false);return true;
    }
    private static void open(ServerPlayer p,String id,BlockPos at,boolean carried){p.openMenu(new SimpleMenuProvider((menu,inv,reader)->new PaperMenu(menu,p,id,at,carried),Component.literal("Hallway papers")));}
    @SubscribeEvent public static void read(PlayerInteractEvent.RightClickItem e){
        if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p)||!active(p)||!exact(p,p.getItemInHand(e.getHand())))return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);open(p,document(p.getItemInHand(e.getHand())),p.blockPosition(),true);
    }
    private static void finished(ServerPlayer p,String id){
        var own=record(p);if(own.getBoolean("Read_"+id))return;own.putBoolean("Read_"+id,true);
        if(id.equals(MARGIN))own.putLong("MarginStep",step(p));save(p,own);
        PlaytestLog.event(p,"inquiry_read","document",id);
    }
    private static void give(ServerPlayer p,ItemStack book){if(!p.getInventory().add(book)){var drop=p.drop(book,false);if(drop!=null)drop.setTarget(p.getUUID());}p.inventoryMenu.broadcastChanges();}
    public static final class PaperMenu extends LecternMenu {
        private final ServerPlayer reader;private final String id;private final BlockPos at;private final boolean carried;private final ItemStack book;private final int count;
        PaperMenu(int menu,ServerPlayer p,String id,BlockPos at,boolean carried){this(menu,p,id,at,carried,new SimpleContainer(1));}
        private PaperMenu(int menu,ServerPlayer p,String id,BlockPos at,boolean carried,SimpleContainer pages){
            super(menu,pages,new SimpleContainerData(1));reader=p;this.id=id;this.at=at.immutable();this.carried=carried;book=original(p,id);pages.setItem(0,book.copy());count=book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size();if(count==1)finished(p,id);
        }
        @Override public boolean stillValid(Player p){
            if(p!=reader||!active(reader))return false;
            if(!carried)return reader.level().dimension().equals(HouseDimensions.INTERIOR)&&reader.distanceToSqr(at.getCenter())<=25
                    &&(reader.level().getBlockState(at).getBlock() instanceof NoteSurfaceBlock||inHall(reader)&&reader.level().getBlockState(at).is(Blocks.LIGHT_GRAY_TERRACOTTA));
            for(int slot=0;slot<reader.getInventory().getContainerSize();slot++)if(ItemStack.isSameItemSameComponents(book,reader.getInventory().getItem(slot)))return true;
            return false;
        }
        @Override public boolean clickMenuButton(Player p,int button){
            if(!stillValid(p))return false;
            if(button==3){var own=record(reader);if(carried||own.getBoolean("Taken_"+id))return false;own.putBoolean("Taken_"+id,true);save(reader,own);give(reader,book.copy());reader.closeContainer();return true;}
            if(button>=100&&button-100>=count||button==1&&getPage()<=0||button==2&&getPage()>=count-1)return false;
            boolean changed=super.clickMenuButton(p,button);if(changed&&getPage()==count-1)finished(reader,id);return changed;
        }
    }
    /** Native personal custody. Every slot mutation saves the exact original; opening never refills it. */
    private static final class KeptPapers extends SimpleContainer {
        final ServerPlayer reader;boolean ready;
        KeptPapers(ServerPlayer p){super(9);reader=p;var saved=record(p).getCompound("Desk");for(int i=0;i<9;i++)setItem(i,ItemStack.parseOptional(p.registryAccess(),saved.getCompound(Integer.toString(i))));ready=true;}
        @Override public void setChanged(){
            super.setChanged();if(!ready)return;var own=record(reader);var saved=new CompoundTag();boolean field=false,plan=false;
            for(int i=0;i<9;i++){var book=getItem(i);if(!book.isEmpty())saved.put(Integer.toString(i),book.save(reader.registryAccess()));if(exact(reader,book)){field|=document(book).equals(FIELD);plan|=document(book).equals(PLAN);}}
            own.put("Desk",saved);
            if(field&&plan&&own.getBoolean("Read_field")&&own.getBoolean("Read_plan")&&!own.getBoolean("Compared")){
                own.putBoolean("Compared",true);own.putLong("ComparedStep",step(reader));say(reader,"Your counted bends lie beside a straight line. The dates are missing. You leave room for another account.");PlaytestLog.event(reader,"inquiry_compare","originals",2);
            }save(reader,own);
        }
    }
    public static final class DeskMenu extends ChestMenu {
        private final ServerPlayer reader;private final BlockPos at;
        DeskMenu(int id,ServerPlayer p,BlockPos at){this(id,p,at,new KeptPapers(p));}
        private DeskMenu(int id,ServerPlayer p,BlockPos at,KeptPapers papers){
            super(MenuType.GENERIC_9x1,id,p.getInventory(),papers,1);reader=p;this.at=at.immutable();
            for(int i=0;i<9;i++){var old=getSlot(i);var slot=new Slot(papers,i,old.x,old.y){@Override public boolean mayPlace(ItemStack stack){return exact(reader,stack);}@Override public int getMaxStackSize(){return 1;}};slot.index=old.index;slots.set(i,slot);}
        }
        @Override public boolean stillValid(Player p){return p==reader&&active(reader)&&desk(reader,at)&&reader.distanceToSqr(at.getCenter())<=36;}
        @Override public void clicked(int slot,int button,ClickType type,Player p){if(stillValid(p))super.clicked(slot,button,type,p);}
        @Override public ItemStack quickMoveStack(Player p,int slot){if(!stillValid(p)||slot>=9&&!exact(reader,getSlot(slot).getItem()))return ItemStack.EMPTY;return super.quickMoveStack(p,slot);}
    }
}
