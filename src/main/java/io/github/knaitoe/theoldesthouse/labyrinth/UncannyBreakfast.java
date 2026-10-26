package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;

/** A finite handed menu and one personal order; the icons are native, immovable menu buttons. */
public final class UncannyBreakfast {
    public enum Order {
        PANCAKES("pancakes","Pancakes"),WAFFLES("waffles","Waffles"),EGGS_CHEESE("eggs_cheese","Eggs and cheese"),GRITS("grits","Grits");
        public final String id,label;Order(String id,String label){this.id=id;this.label=label;}
        public static Order of(String id){for(var o:values())if(o.id.equals(id))return o;return null;}
    }
    private static final LabyrinthPlace PLACE=LabyrinthPlace.SECOND_WAITRESS;
    public static final String VIEW="UncannyMenuBlurb131";
    public static final int ORDER=600;
    private UncannyBreakfast(){}
    private static void blurb(ItemStack s){s.set(DataComponents.WRITTEN_BOOK_CONTENT,UncannyReading.excerpt(PLACE,0,new CompoundTag()).get(DataComponents.WRITTEN_BOOK_CONTENT));CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putBoolean(VIEW,true));}
    public static ItemStack menu(ServerPlayer p){var s=new ItemStack(UncannyRegistry.MENU.get());CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putUUID("UncannyMenuOwner",p.getUUID());t.putUUID("UncannyMenu",UUID.randomUUID());});blurb(s);return s;}
    private static boolean owns(ServerPlayer p,ItemStack menu){var c=menu.get(DataComponents.CUSTOM_DATA);return menu.is(UncannyRegistry.MENU.get())&&c!=null&&c.copyTag().hasUUID("UncannyMenuOwner")&&c.copyTag().getUUID("UncannyMenuOwner").equals(p.getUUID());}
    public static boolean canOrder(ServerPlayer p){
        if(!LiteraryVignettes.inside(p,PLACE))return false;var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);var b=LiteraryVignettes.base(p,PLACE);
        if(b==null||!own.getBoolean("DinerMenuGiven130")||own.contains("Breakfast130")||own.getBoolean("MealReleased")||own.getBoolean("Completed")||UncannyStories.failed(own)||!UncannyDiner.seated(p,b))return false;
        var a=UncannyDiner.waitress(p,b);var w=UncannyStories.world(p.serverLevel(),b,PLACE);
        return a!=null&&a.distanceToSqr(p)<16&&w.hasUUID("Host127")&&w.getUUID("Host127").equals(p.getUUID());
    }
    public static boolean open(ServerPlayer p,ItemStack menu){
        if(!LiteraryVignettes.participant(p)||!owns(p,menu))return false;
        // Upgrade the same old physical menu in place; never issue a second original.
        blurb(menu);p.getInventory().setChanged();var view=menu.copy();CustomData.update(DataComponents.CUSTOM_DATA,view,t->t.putBoolean("CanOrder131",canOrder(p)));
        p.openMenu(new SimpleMenuProvider((id,inv,who)->new LiteraryVignettes.Pages(id,p,PLACE,"Passage0124_0",view,true,false,p.blockPosition()),menu.getHoverName()));
        if(p.containerMenu instanceof LiteraryVignettes.Pages pages)pages.opened();return true;
    }
    public static boolean button(ServerPlayer p,ItemStack menu,int button){
        int n=button-ORDER;if(!owns(p,menu)||n<0||n>=Order.values().length)return false;
        if(!choose(p,Order.values()[n]))return false;p.closeContainer();return true;
    }
    public static boolean choose(ServerPlayer p,Order order){
        if(order==null||!canOrder(p))return false;var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);
        own.putString("Breakfast130",order.id);own.putInt("OrderPlacedAt130",own.getInt("UncannyAge"));
        if(own.contains("OriginalMeal")){var s=ItemStack.parseOptional(p.registryAccess(),own.getCompound("OriginalMeal"));if(!s.isEmpty()){
            s.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(order.ordinal()+1));s.set(DataComponents.CUSTOM_NAME,Component.literal(order.label));CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putString("Breakfast130",order.id));own.put("OriginalMeal",s.save(p.registryAccess()));
        }}
        LiteraryVignettes.save(d,p.getUUID(),PLACE,own);p.displayClientMessage(Component.literal("“"+order.label+". I'll put that through.”"),false);return true;
    }
}
