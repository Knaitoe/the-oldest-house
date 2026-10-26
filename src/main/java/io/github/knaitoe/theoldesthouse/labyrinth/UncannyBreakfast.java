package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
    private UncannyBreakfast(){}
    public static ItemStack menu(ServerPlayer p){var s=new ItemStack(UncannyRegistry.MENU.get());CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putUUID("UncannyMenuOwner",p.getUUID());t.putUUID("UncannyMenu",UUID.randomUUID());});return s;}
    public static boolean canOrder(ServerPlayer p){
        if(!LiteraryVignettes.inside(p,PLACE))return false;var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);var b=LiteraryVignettes.base(p,PLACE);
        if(b==null||!own.getBoolean("DinerMenuGiven130")||own.contains("Breakfast130")||own.getBoolean("MealReleased")||own.getBoolean("Completed")||UncannyStories.failed(own)||!UncannyDiner.seated(p,b))return false;
        var a=UncannyDiner.waitress(p,b);var w=UncannyStories.world(p.serverLevel(),b,PLACE);
        return a!=null&&a.distanceToSqr(p)<16&&w.hasUUID("Host127")&&w.getUUID("Host127").equals(p.getUUID());
    }
    public static boolean open(ServerPlayer p,ItemStack menu){
        var data=menu.get(DataComponents.CUSTOM_DATA);if(!menu.is(UncannyRegistry.MENU.get())||data==null||!data.copyTag().hasUUID("UncannyMenuOwner")||!data.copyTag().getUUID("UncannyMenuOwner").equals(p.getUUID())||!canOrder(p))return false;
        var icons=new LinkedHashMap<Integer,ItemStack>();int slot=1;for(var o:Order.values()){
            var s=new ItemStack(UncannyRegistry.BREAKFAST_ICON.get());s.set(DataComponents.CUSTOM_NAME,Component.literal(o.label));s.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(o.ordinal()+1));icons.put(slot,s);slot+=2;
        }
        LiteraryChoiceMenu.open(p,"Breakfast",icons,()->canOrder(p),button->{int n=(button-1)/2;if(button<1||button>7||button%2==0||n>=Order.values().length)return false;return choose(p,Order.values()[n]);});return true;
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
