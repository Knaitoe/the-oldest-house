package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.labyrinth.KitchenCooking.Cook;
import io.github.knaitoe.theoldesthouse.labyrinth.KitchenCooking.Dish;
import io.github.knaitoe.theoldesthouse.labyrinth.KitchenCooking.Seasoning;
import io.github.knaitoe.theoldesthouse.labyrinth.KitchenCooking.Texture;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** 0.4.331: the familiar kitchen's cooking and its visitors (docs/KITCHEN_0_4_331.md). */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class KitchenTests {
    private KitchenTests(){}
    private static LiteraryTests.Fixture active;
    private static void close(){if(active!=null){active.close();active=null;}KitchenFittings.clearAll();}
    @AfterBatch(batch="kitchen_cook") public static void doneCook(ServerLevel l){close();}
    @AfterBatch(batch="kitchen_visitors") public static void doneVisitors(ServerLevel l){close();}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static ItemStack water(){return PotionContents.createItemStack(Items.POTION,Potions.WATER);}

    @GameTest(template="empty",batch="kitchen_cook",timeoutTicks=900)
    public static void aPanOfMoreThanOneThingIsADishByItsStrokes(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=LabyrinthPlace.FAMILIAR_KITCHEN;var b=f.base(place);var l=f.in;hold(f,place);KitchenFittings.clearAll();
        var p=NativeTestPlayers.survival(h,"kitchen_chef");f.players.add(p);var hand=InteractionHand.MAIN_HAND;
        var stove=b.offset(KitchenFittings.STOVE);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the kitchen is resident"))
            .thenExecute(()->{
                FamiliarRoom.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                p.teleportTo(l,b.getX()-3.5,b.getY(),b.getZ()-10.5,-90,0);
                // The strokes alone, from what went in and how long it was on.
                var fried=KitchenCooking.dish(List.of(new ItemStack(Items.CHICKEN),new ItemStack(Items.SUGAR)),50,"x");
                h.assertTrue(fried.texture()==Texture.CRISP&&fried.seasoning()==Seasoning.SWEET&&fried.cook()==Cook.UNDERDONE&&fried.rawFlesh(),"chicken and sugar, barely on: crisp, sweet, underdone, raw in the middle: "+fried);
                var muddle=KitchenCooking.dish(List.of(new ItemStack(Items.BREAD),new ItemStack(Items.SWEET_BERRIES),new ItemStack(Items.BLAZE_POWDER),new ItemStack(Items.COCOA_BEANS)),200,"x");
                h.assertTrue(muddle.seasoning()==Seasoning.MUDDLED&&muddle.cook()==Cook.THROUGH,"three kinds of taste is muddled: "+muddle);
                var creamy=KitchenCooking.dish(List.of(new ItemStack(Items.MILK_BUCKET),new ItemStack(Items.POTATO)),KitchenCooking.burnsAt(Texture.CREAMY),"x");
                h.assertTrue(creamy.texture()==Texture.CREAMY&&creamy.cook()==Cook.BURNT&&creamy.nutrition()<5,"milk makes it creamy; left long enough it burns and fills less: "+creamy);
                var tie=KitchenCooking.dish(List.of(new ItemStack(Items.BEEF),new ItemStack(Items.DRIED_KELP),new ItemStack(Items.SUGAR)),200,"x");
                h.assertTrue(tie.seasoning()==Seasoning.SALTY&&tie.second()==Seasoning.SWEET,"a tie goes to the stronger taste, the other kept: "+tie);
                // A real pan: beef on the ring alone, then salt and water in with it.
                p.setItemInHand(hand,new ItemStack(Items.BEEF));
                KitchenFittings.use(p,l,b,KitchenFittings.Fitting.STOVE,stove,Vec3.ZERO);
                h.assertTrue(KitchenFittings.cooking(l.getServer())&&KitchenFittings.pan(l).isEmpty(),"one kind of thing alone cooks the plain way");
                p.setItemInHand(hand,new ItemStack(Items.DRIED_KELP));KitchenFittings.use(p,l,b,KitchenFittings.Fitting.STOVE,stove,Vec3.ZERO);
                p.setItemInHand(hand,water());KitchenFittings.use(p,l,b,KitchenFittings.Fitting.STOVE,stove,Vec3.ZERO);
                var pan=KitchenFittings.pan(l);
                h.assertTrue(pan.size()==3&&KitchenCooking.texture(pan)==Texture.SOFT,"something else in with it makes a pan, and water makes it soft: "+pan);
                h.assertTrue(p.getInventory().countItem(Items.GLASS_BOTTLE)==1,"the bottle comes back");
                p.setItemInHand(hand,new ItemStack(Items.STICK));KitchenFittings.use(p,l,b,KitchenFittings.Fitting.STOVE,stove,Vec3.ZERO);
                h.assertTrue(KitchenFittings.pan(l).size()==3&&p.getMainHandItem().is(Items.STICK),"a stick does not go in the pan");
                p.setItemInHand(hand,ItemStack.EMPTY);
            })
            .thenIdle(KitchenCooking.ready(Texture.SOFT)+20)
            .thenExecute(()->{
                KitchenFittings.use(p,l,b,KitchenFittings.Fitting.STOVE,stove,Vec3.ZERO);
                var stack=p.getInventory().items.stream().filter(s->s.is(KitchenRegistry.DISH.get())).findFirst().orElse(ItemStack.EMPTY);
                var dish=KitchenCooking.read(stack);
                h.assertTrue(dish!=null&&dish.texture()==Texture.SOFT&&dish.seasoning()==Seasoning.SALTY&&dish.cook()==Cook.THROUGH&&"kitchen_chef".equals(dish.by()),"taken off when it smelled done: a soft, salty stew, cooked through, made by the cook: "+dish);
                h.assertTrue(stack.getHoverName().getString().equals("Salty stew")&&stack.get(DataComponents.FOOD).nutrition()==dish.nutrition()&&dish.nutrition()>=9,"named and filling by what went in: "+stack.getHoverName().getString()+" "+dish.nutrition());
                h.assertTrue(stack.get(DataComponents.CUSTOM_MODEL_DATA).value()==1,"in a bowl");
                h.assertTrue(!KitchenFittings.cooking(l.getServer())&&KitchenFittings.pan(l).isEmpty(),"the stove is clear");
                // Eaten inside the House: the slight lift of a soft, salty dish.
                var line=KitchenCooking.lift(p,dish,true);
                h.assertTrue(p.hasEffect(MobEffects.REGENERATION)&&line.contains("Warm")&&line.contains("stays with you"),"soft warms, salt stays: "+line);
                var raw=KitchenCooking.dish(List.of(new ItemStack(Items.CHICKEN),new ItemStack(Items.SUGAR)),50,"x");KitchenCooking.lift(p,raw,true);
                h.assertTrue(p.hasEffect(MobEffects.HUNGER)&&!p.hasEffect(MobEffects.DIG_SPEED),"underdone chicken gives no lift, only what raw chicken gives");
                // Left on the ring, a pan burns and the ring clicks off.
                p.setItemInHand(hand,new ItemStack(Items.POTATO));KitchenFittings.use(p,l,b,KitchenFittings.Fitting.STOVE,stove,Vec3.ZERO);
                p.setItemInHand(hand,new ItemStack(Items.DRIED_KELP));KitchenFittings.use(p,l,b,KitchenFittings.Fitting.STOVE,stove,Vec3.ZERO);
                var hob=f.data().stateEntry("kitchen_0239","stove");hob.putLong("Lit",l.getGameTime()-KitchenCooking.burnsAt(Texture.CRISP)-5);f.data().setStateEntry("kitchen_0239","stove",hob);
                p.setItemInHand(hand,ItemStack.EMPTY);
            })
            .thenIdle(30)
            .thenExecute(()->{
                h.assertTrue(f.data().stateEntry("kitchen_0239","stove").getBoolean("Off"),"burnt, and the ring has clicked off by itself");
                p.setItemInHand(hand,new ItemStack(Items.SUGAR));KitchenFittings.use(p,l,b,KitchenFittings.Fitting.STOVE,stove,Vec3.ZERO);
                h.assertTrue(KitchenFittings.pan(l).size()==2&&p.getMainHandItem().is(Items.SUGAR),"nothing more goes in a burnt pan");
                p.setItemInHand(hand,ItemStack.EMPTY);KitchenFittings.use(p,l,b,KitchenFittings.Fitting.STOVE,stove,Vec3.ZERO);
                var burnt=p.getInventory().items.stream().map(KitchenCooking::read).filter(d->d!=null&&d.cook()==Cook.BURNT).findFirst().orElse(null);
                h.assertTrue(burnt!=null&&burnt.texture()==Texture.CRISP,"it comes off burnt: "+burnt);
                h.assertTrue(KitchenCooking.lift(p,burnt,true).contains("pan"),"and tastes of the pan");
            })
            .thenSucceed();
    }

    @GameTest(template="empty",batch="kitchen_visitors",timeoutTicks=1200)
    public static void visitorsComeInSitDownAndAnswerWhatTheyAreGiven(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=LabyrinthPlace.FAMILIAR_KITCHEN;var b=f.base(place);var l=f.in;hold(f,place);KitchenFittings.clearAll();
        var p=NativeTestPlayers.survival(h,"kitchen_host");f.players.add(p);var q=NativeTestPlayers.survival(h,"kitchen_other");f.players.add(q);var hand=InteractionHand.MAIN_HAND;
        LiteraryActor[] seen={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the kitchen is resident"))
            .thenExecute(()->{
                FamiliarRoom.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                p.teleportTo(l,b.getX()-3.5,b.getY(),b.getZ()-4.5,0,0);q.teleportTo(l,b.getX()-3.5,b.getY(),b.getZ()-9.5,0,0);
                var data=f.data();var hollo=data.state(HollowayVignette.ID);hollo.putBoolean("ActorDead",false);data.setState(HollowayVignette.ID,hollo);
                var phases=KitchenVisitors.availablePhases(l.getServer());
                h.assertTrue(phases.contains(KitchenVisitors.JOHNNY)&&phases.contains(KitchenVisitors.HOLLOWAY)&&phases.contains(KitchenVisitors.TRAVELLER)&&!phases.contains(KitchenVisitors.TENANT),"Johnny, Holloway and a traveller can come; the tenant has not moved out for anyone here: "+phases);
                var own=LiteraryVignettes.personal(data,p.getUUID(),VacantPossession.PLACE);own.putString("Outcome","carried_the_last_chair_down");LiteraryVignettes.save(data,p.getUUID(),VacantPossession.PLACE,own);
                h.assertTrue(KitchenVisitors.availablePhases(l.getServer()).contains(KitchenVisitors.TENANT),"once a reader here has emptied the flat, the tenant can come");
                seen[0]=KitchenVisitors.arrive(l,b,KitchenVisitors.HOLLOWAY);
                h.assertTrue(seen[0]!=null&&KitchenVisitors.current(l)==seen[0],"Holloway comes in by a far door");
            })
            .thenWaitUntil(()->{var a=KitchenVisitors.current(l);h.assertTrue(a!=null&&TraceBlock.is(l.getBlockState(a.blockPosition()),TraceBlock.Kind.CHAIR),"he sits at the table");})
            .thenExecute(()->{
                var a=seen[0];
                var dish=KitchenCooking.stack(new Dish(Texture.CRISP,Seasoning.SALTY,null,Cook.WELL_DONE,10,false,"kitchen_host"));
                p.setItemInHand(hand,dish);var said=KitchenVisitors.offer(p,a,p.getMainHandItem());
                h.assertTrue(said!=null&&said.contains("proper camp meal")&&said.contains("brother"),"something crisp, salty and well done is what he loves, and the first time he says something of himself: "+said);
                h.assertTrue(p.getMainHandItem().isEmpty()&&a.getMainHandItem().is(KitchenRegistry.DISH.get()),"he takes it and eats it");
                q.setItemInHand(hand,new ItemStack(Items.MUSHROOM_STEW));var slop=KitchenVisitors.offer(q,a,q.getMainHandItem());
                h.assertTrue(slop!=null&&slop.startsWith("Slop"),"a stew is soft, and he cannot stand soft: "+slop);
                h.assertTrue(q.getInventory().countItem(Items.BOWL)==1,"the bowl comes back to the hand that held it out");
                q.setItemInHand(hand,new ItemStack(Items.BREAD));
                h.assertTrue(KitchenVisitors.offer(q,a,q.getMainHandItem())==null&&q.getMainHandItem().is(Items.BREAD),"two is all he will eat on one visit");
                var book=KitchenVisitors.book(l.getServer());
                h.assertTrue(book.size()==2&&book.get(0).startsWith("Holloway ate something crisp and salty, well done, made by kitchen_host"),"the kitchen's book keeps who ate what, who made it and what they said: "+book);
                KitchenVisitors.read(q);
                h.assertTrue(q.containerMenu instanceof net.minecraft.world.inventory.LecternMenu,"anyone can read the book on the counter");q.closeContainer();
                // He remembers the reader who fed him, and not the other way round.
                var host=f.data().stateEntry(KitchenVisitors.STATE,p.getUUID().toString()).getCompound("V"+KitchenVisitors.HOLLOWAY);
                h.assertTrue(host.getInt("Fed")==1&&host.getBoolean("Loved"),"remembered: fed once, and loved it");
                // Fed and done, he goes: up, out by the door he came in by, gone.
                var t=f.data().stateEntry(KitchenVisitors.STATE,KitchenVisitors.SHARED);t.putInt("FedAt",t.getInt("Occ")-KitchenVisitors.AFTER_MEAL-1);f.data().setStateEntry(KitchenVisitors.STATE,KitchenVisitors.SHARED,t);
            })
            .thenWaitUntil(()->h.assertTrue(KitchenVisitors.current(l)==null,"he has gone"))
            .thenExecute(()->{
                var t=f.data().stateEntry(KitchenVisitors.STATE,KitchenVisitors.SHARED);
                h.assertTrue(!t.contains("Who")&&t.getInt("Next")>t.getInt("Occ")&&t.getInt("LastWho")==KitchenVisitors.HOLLOWAY,"the next visit is some time off, and will not be him again straight away");
                // Dead, Holloway does not come.
                var hollo=f.data().state(HollowayVignette.ID);hollo.putBoolean("ActorDead",true);f.data().setState(HollowayVignette.ID,hollo);
                h.assertTrue(!KitchenVisitors.availablePhases(l.getServer()).contains(KitchenVisitors.HOLLOWAY),"while he lives only");
                // Struck, a visitor gets up and goes.
                var j=KitchenVisitors.arrive(l,b,KitchenVisitors.JOHNNY);seen[0]=j;
                KitchenVisitors.struck(q,j);
                h.assertTrue(f.data().stateEntry(KitchenVisitors.STATE,KitchenVisitors.SHARED).getInt("Stage")==3,"struck, Johnny leaves");
                hollo.putBoolean("ActorDead",false);f.data().setState(HollowayVignette.ID,hollo);
            })
            .thenWaitUntil(()->h.assertTrue(KitchenVisitors.current(l)==null,"and is gone"))
            .thenSucceed();
    }
}
