package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.228, Vacant Possession (docs/VACANT_POSSESSION_0_4_228.md). A first-floor flat on moving day. The tenant asks for everything
 * on their list to go down to the van. Each piece is lifted with a right-click and carried down the stairs in the reader's arms, and
 * set down in the van; what it was hiding is there where it stood. Each piece set down takes something of the tenant with it: their
 * words come shorter and fainter and they are harder to see. When everything else has gone they are sitting in the last chair, and
 * stand when the reader comes close: "That's the last of it." Carry it down and the flat is empty, its window dark; put it back where
 * it stood and they sit down in it again, the light still on. Either way the story ends on the street.
 * <p>
 * The flat's state belongs to the flat (its base), so readers who come in together empty it together; a reader on their own has their
 * own copy (SceneInstances). It starts again from the beginning when nobody has been in it for five seconds. Nothing in it can hurt.
 * <p>
 * 0.4.260: the pieces have weight. The bed and the wardrobe are heavy, the dresser, table and armchair a weight, the rest light: a
 * reader carrying one alone is slowed by it, jumps lower with anything but the light things, cannot run with the heavy ones and drags
 * them. Another reader in the flat with empty hands takes the other end with a right-click on the one carrying it (a third reader the
 * middle), and the weight is shared between them; walking more than four blocks apart, or a second right-click, lets go.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class VacantPossession {
    private VacantPossession(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.VACANT_POSSESSION;
    public static final int TENANT=53;
    /** How much of the tenant each piece in the van takes, out of a hundred. */
    public static final int FADE_EACH=8;
    public static final int EMPTY_RESET=100;
    /** The tenant at the window (0), in the last chair (1), standing beside it (2), sitting again after it was put back (3), gone (4). */
    public static final int STANDING=0,SITTING=1,STOOD=2,KEPT=3,GONE=4;
    private static final String TAG="Vacant228";
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    /** One flat's moving day. */
    static final class Flat {
        final BlockPos base;long tick;int empty,seat=STANDING,sittingSince=-1;@Nullable UUID tenant;
        /**
         * 0.4.244: the flat coming back to readers who have all finished it, rather than moving day. The flat itself is as the last chair
         * left it when carried down: empty to the walls, the bulbs off. Each reader who put the chair back sees it there, the light on, and
         * the one who lived here in it: their own view and their own figure, so two readers who chose differently each find their own.
         */
        boolean after;
        /** Each reader who put the chair back, and the figure in it only they see. */
        final Map<UUID,UUID> shown=new HashMap<>();
        final EnumMap<VacantBlock.Kind,Integer> van=new EnumMap<>(VacantBlock.Kind.class);
        final EnumMap<VacantBlock.Kind,UUID> carried=new EnumMap<>(VacantBlock.Kind.class);
        /** 0.4.260: who has the other end of what each reader is carrying, in the order they took hold. */
        final Map<UUID,List<UUID>> helping=new HashMap<>();
        /** 0.4.260: where each reader carrying something alone stood last tick, to hear it dragged. */
        final Map<UUID,Vec3> last=new HashMap<>();
        Flat(BlockPos b){base=b;}
        /** How many of the eight pieces on the list are in the van. */
        int loaded(){int n=0;for(var k:van.keySet())if(k!=VacantBlock.Kind.CHAIR)n++;return n;}
        boolean ended(){return seat==KEPT||seat==GONE;}
    }
    private static final Map<Long,Flat> FLATS=new HashMap<>();
    public static void clearAll(){FLATS.clear();}
    public static boolean tenant(int phase){return phase==TENANT;}

    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void say(ServerPlayer p,Component line){p.displayClientMessage(line,true);}
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static Vec3 rel(Vec3 at,BlockPos b){return at.subtract(b.getX(),b.getY(),b.getZ());}

    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        if(own.getBoolean("Completed"))return;
        cue(p,"A landing, a bare bulb, and a door standing open. Inside, someone has been waiting for help.");
    }
    @Nullable static Flat flat(BlockPos b){return FLATS.get(b.asLong());}
    /** For tests: this flat's moving day, from the base. */
    public static int loaded(BlockPos b){var f=flat(b);return f==null?0:f.loaded();}
    public static int seat(BlockPos b){var f=flat(b);return f==null?-1:f.seat;}
    public static boolean inVan(BlockPos b,VacantBlock.Kind kind){var f=flat(b);return f!=null&&f.van.containsKey(kind);}
    @Nullable public static LiteraryActor tenantActor(ServerLevel l,BlockPos b){var f=flat(b);return f==null?null:actor(l,f.tenant);}
    /**
     * 0.4.239: the flat through its door. With nobody in it, it is as it was before moving day: the tenant waiting, someone moving
     * about. While it is being emptied, they are fainter with every piece that goes. Once the last chair has gone down, nothing.
     */
    @Nullable public static DoorSounds.Through heard(@Nullable BlockPos b){
        var f=b==null?null:flat(b);
        if(f==null)return new DoorSounds.Through(DoorSounds.Carry.SOMEONE,1F);
        if(f.seat==GONE)return new DoorSounds.Through(DoorSounds.Carry.EMPTY,1F);
        if(f.after)return new DoorSounds.Through(DoorSounds.Carry.EMPTY,1F);
        return new DoorSounds.Through(DoorSounds.Carry.SOMEONE,Math.max(.3F,1F-f.loaded()*FADE_EACH/100F));
    }
    /** 0.4.244: how faintly the one left in the kept chair is heard through the door, and how faintly they are drawn. */
    public static final float KEPT_HEARD=.3F;public static final int KEPT_FADE=VacantSite.PIECES.size()*FADE_EACH-24;
    /** 0.4.244: and for a reader who finished it, with nobody else emptying it, what they would find. */
    @Nullable public static DoorSounds.Through heard(@Nullable BlockPos b,LabyrinthData data,UUID listener){
        if((b==null||flat(b)==null||flat(b).after)&&Aftermath.returning(data,listener,PLACE))
            return kept(LiteraryVignettes.personal(data,listener,PLACE).getString("Outcome"))?new DoorSounds.Through(DoorSounds.Carry.SOMEONE,KEPT_HEARD):new DoorSounds.Through(DoorSounds.Carry.EMPTY,1F);
        return heard(b);
    }
    static boolean kept(@Nullable String outcome){return "put_the_chair_back".equals(outcome);}
    /** For tests: whether this flat is standing as its aftermath, and the figure a reader who put the chair back sees in it. */
    public static boolean after(BlockPos b){var f=flat(b);return f!=null&&f.after;}
    @Nullable public static LiteraryActor shownTo(ServerLevel l,BlockPos b,UUID reader){var f=flat(b);return f==null?null:actor(l,f.shown.get(reader));}

    // ------------------------------------------------------------------------------------------------ the piece in the reader's arms
    /** The piece as an item in the reader's arms: drawn as itself, and marked as the tenant's and the reader's. */
    static ItemStack carried(ServerPlayer p,VacantSite.Piece piece){
        var stack=new ItemStack(VacantRegistry.PIECE.get());
        stack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(piece.kind().ordinal()+1));
        var name=piece.name();stack.set(DataComponents.ITEM_NAME,Component.literal(Character.toUpperCase(name.charAt(0))+name.substring(1)));
        var t=new CompoundTag();t.putString(TAG,piece.kind().name());t.putUUID("Owner",p.getUUID());stack.set(DataComponents.CUSTOM_DATA,CustomData.of(t));
        return stack;
    }
    static boolean loan(ItemStack s){if(!s.is(VacantRegistry.PIECE.get()))return false;var c=s.get(DataComponents.CUSTOM_DATA);return c!=null&&c.copyTag().contains(TAG);}
    static boolean holding(ServerPlayer p){for(int i=0;i<p.getInventory().getContainerSize();i++)if(loan(p.getInventory().getItem(i)))return true;return loan(p.containerMenu.getCarried());}
    /** Takes the piece out of the reader's arms and gives back what they were holding before. */
    static void release(ServerPlayer p,CompoundTag own){
        var inv=p.getInventory();
        for(int i=0;i<inv.getContainerSize();i++)if(loan(inv.getItem(i)))inv.setItem(i,ItemStack.EMPTY);
        if(loan(p.containerMenu.getCarried()))p.containerMenu.setCarried(ItemStack.EMPTY);
        if(own.contains("Before228")){
            var before=ItemStack.parseOptional(p.registryAccess(),own.getCompound("Before228"));int slot=own.getInt("Slot228");
            if(!before.isEmpty()){if(slot>=0&&slot<inv.getContainerSize()&&inv.getItem(slot).isEmpty())inv.setItem(slot,before);else inv.placeItemBackInInventory(before);}
        }
        own.remove("Before228");own.remove("Slot228");own.remove("Carrying228");
        p.inventoryMenu.broadcastChanges();
    }
    static void take(ServerPlayer p,CompoundTag own,VacantSite.Piece piece){
        var inv=p.getInventory();int slot=inv.selected;
        own.put("Before228",inv.getItem(slot).saveOptional(p.registryAccess()));own.putInt("Slot228",slot);own.putString("Carrying228",piece.kind().name());
        inv.setItem(slot,carried(p,piece));p.inventoryMenu.broadcastChanges();
    }
    @Nullable static VacantSite.Piece carrying(CompoundTag own){
        if(!own.contains("Carrying228"))return null;
        try{return VacantSite.piece(VacantBlock.Kind.valueOf(own.getString("Carrying228")));}catch(IllegalArgumentException e){return null;}
    }

    // ------------------------------------------------------------------------------------------------ 0.4.260, weight
    /** How a piece weighs in the arms: what it takes from walking, and from a jump, carried alone. */
    public enum Weight{
        LIGHT(-.1,0),MEDIUM(-.25,-.2),HEAVY(-.45,-.45);
        public final double speed,jump;Weight(double speed,double jump){this.speed=speed;this.jump=jump;}
    }
    public static Weight weight(VacantBlock.Kind k){return switch(k){case BED,WARDROBE->Weight.HEAVY;case DRESSER,TABLE,ARMCHAIR->Weight.MEDIUM;default->Weight.LIGHT;};}
    /** Each reader's share of the weight with this many others holding it: alone all of it, two a third each, three a sixth. */
    public static double share(int others){return others<=0?1:others==1?1/3D:1/6D;}
    /** How far apart two readers carrying one piece can be before the other end is let go. */
    public static final double HELP_REACH=4;
    public static final int MAX_HELPERS=2;
    static final ResourceLocation CARRY_SPEED=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"vacant_carry_speed");
    static final ResourceLocation CARRY_JUMP=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"vacant_carry_jump");
    /** Running with a heavy piece alone gets nothing for it: this undoes the sprint's own 0.3. */
    static final ResourceLocation CARRY_RUN=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"vacant_carry_run");
    static final double NO_RUN=1/1.3-1;
    /** For tests: what a reader's walking is multiplied by now (1 when they carry nothing). */
    public static double burden(ServerPlayer p){var a=p.getAttribute(Attributes.MOVEMENT_SPEED);var m=a==null?null:a.getModifier(CARRY_SPEED);return m==null?1:1+m.amount();}
    public static double jumpBurden(ServerPlayer p){var a=p.getAttribute(Attributes.JUMP_STRENGTH);var m=a==null?null:a.getModifier(CARRY_JUMP);return m==null?1:1+m.amount();}
    /** For tests: who has the other end of what this reader carries. */
    public static List<UUID> helpers(BlockPos b,UUID carrier){var f=flat(b);return f==null?List.of():List.copyOf(f.helping.getOrDefault(carrier,List.of()));}
    private static void modifier(ServerPlayer p,net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr,ResourceLocation id,double amount){
        var a=p.getAttribute(attr);if(a==null)return;var m=a.getModifier(id);
        if(amount==0){if(m!=null)a.removeModifier(id);return;}
        if(m!=null&&Math.abs(m.amount()-amount)<1e-6)return;
        a.removeModifier(id);a.addTransientModifier(new AttributeModifier(id,amount,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
    static void weigh(ServerPlayer p,@Nullable Weight w,int others){
        double s=w==null?0:share(others);
        modifier(p,Attributes.MOVEMENT_SPEED,CARRY_SPEED,w==null?0:w.speed*s);
        modifier(p,Attributes.JUMP_STRENGTH,CARRY_JUMP,w==null?0:w.jump*s);
        modifier(p,Attributes.MOVEMENT_SPEED,CARRY_RUN,w==Weight.HEAVY&&others==0&&p.isSprinting()?NO_RUN:0);
    }
    static void unweigh(ServerPlayer p){weigh(p,null,0);}
    /**
     * The other end: a reader in the flat with empty hands takes hold of what another is carrying, or lets go of it again. Light things
     * need no second pair of hands.
     */
    public static boolean help(ServerPlayer helper,ServerPlayer carrier){
        if(helper==carrier||LiteraryVignettes.current(helper)!=PLACE||LiteraryVignettes.current(carrier)!=PLACE)return false;
        var b=LiteraryVignettes.base(carrier,PLACE);if(b==null||!b.equals(LiteraryVignettes.base(helper,PLACE)))return false;
        var flat=flat(b);if(flat==null)return false;
        var piece=carrying(own(carrier));if(piece==null)return false;
        var list=flat.helping.computeIfAbsent(carrier.getUUID(),k->new ArrayList<>());
        if(list.remove(helper.getUUID())){
            cue(helper,"You let go of the other end of "+piece.name()+".");cue(carrier,helper.getGameProfile().getName()+" lets go of "+piece.name()+".");
            return true;
        }
        if(carrying(own(helper))!=null){cue(helper,"Your arms are full.");return true;}
        if(!helper.getMainHandItem().isEmpty()){cue(helper,"You'd need both hands free.");return true;}
        if(weight(piece.kind())==Weight.LIGHT){cue(helper,capital(piece.name())+" doesn't need two.");return true;}
        if(list.size()>=MAX_HELPERS){cue(helper,"There's no room for another pair of hands on it.");return true;}
        for(var other:flat.helping.values())other.remove(helper.getUUID());
        list.add(helper.getUUID());
        cue(helper,list.size()==1?"You take the other end of "+piece.name()+".":"You get your hands under the middle of "+piece.name()+".");
        cue(carrier,helper.getGameProfile().getName()+(list.size()==1?" takes the other end.":" takes some of the weight too."));
        return true;
    }
    @SubscribeEvent public static void interact(PlayerInteractEvent.EntityInteract e){
        if(e.getHand()!=InteractionHand.MAIN_HAND||!(e.getEntity() instanceof ServerPlayer helper)||!(e.getTarget() instanceof ServerPlayer carrier))return;
        if(LiteraryVignettes.current(helper)!=PLACE)return;
        if(help(helper,carrier)){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}
    }
    /** Every reader in the flat feels what they are carrying, and what they are helping with; anyone else, nothing. */
    static void weights(ServerLevel l,Flat flat){
        var here=readers(l,flat.base);var byId=new HashMap<UUID,ServerPlayer>();for(var r:here)byId.put(r.getUUID(),r);
        var held=new HashMap<UUID,VacantSite.Piece>();for(var r:here){var piece=carrying(own(r));if(piece!=null)held.put(r.getUUID(),piece);}
        // Hands come off what nobody is carrying any more, and off a carrier who has walked too far from them.
        for(var it=flat.helping.entrySet().iterator();it.hasNext();){
            var e=it.next();var carrier=byId.get(e.getKey());var piece=held.get(e.getKey());
            if(carrier==null||piece==null){it.remove();continue;}
            for(var h=e.getValue().iterator();h.hasNext();){
                var helper=byId.get(h.next());
                if(helper==null){h.remove();continue;}
                if(held.containsKey(helper.getUUID())||helper.distanceToSqr(carrier)>HELP_REACH*HELP_REACH){
                    h.remove();cue(helper,"You've let go of "+piece.name()+".");cue(carrier,"You have all the weight of "+piece.name()+" again.");}
            }
            if(e.getValue().isEmpty())it.remove();
        }
        var helpingWith=new HashMap<UUID,UUID>();for(var e:flat.helping.entrySet())for(var h:e.getValue())helpingWith.put(h,e.getKey());
        for(var r:here){
            var piece=held.get(r.getUUID());
            if(piece!=null){
                int others=flat.helping.getOrDefault(r.getUUID(),List.of()).size();var w=weight(piece.kind());weigh(r,w,others);
                // A heavy piece carried alone scrapes and knocks along the floor.
                var was=flat.last.put(r.getUUID(),r.position());
                if(w==Weight.HEAVY&&others==0&&was!=null&&r.onGround()&&was.distanceToSqr(r.position())>.0016&&flat.tick%8==0)
                    l.playSound(null,r.blockPosition(),SoundEvents.WOOD_HIT,SoundSource.BLOCKS,.5F,.45F+l.random.nextFloat()*.1F);
                continue;
            }
            flat.last.remove(r.getUUID());
            var carrierId=helpingWith.get(r.getUUID());var carried=carrierId==null?null:held.get(carrierId);
            if(carried!=null)weigh(r,weight(carried.kind()),flat.helping.get(carrierId).size());else unweigh(r);
        }
    }

    // ------------------------------------------------------------------------------------------------ right-clicks
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var s=l.getBlockState(b.offset(rel));var kind=VacantBlock.kind(s);var flat=flat(b);
        if(flat==null)return false;
        var held=carrying(own);
        if(held!=null){
            if(VacantSite.van(rel))return setDown(p,b,own,flat,held);
            // Back where it stood: its own cells, the floor under them, or what it hid above them.
            if(held.cells().contains(rel)||held.cells().contains(rel.above())||held.cells().contains(rel.below()))return putBack(p,b,own,flat,held,true);
            if(kind!=null){cue(p,"Your arms are full. Set "+held.name()+" down in the van, or back where it stood.");return true;}
            return false;
        }
        if(kind==null){
            if(s.is(VacantRegistry.material("boards_new"))){own.putBoolean("Floor228",true);cue(p,VacantTexts.trace(VacantBlock.Kind.RUG));return true;}
            return false;
        }
        switch(kind){
            case MARKS->{own.putBoolean("Marks228",true);cue(p,VacantTexts.trace(kind));return true;}
            case SCRATCHES->{own.putBoolean("Scratch228",true);cue(p,VacantTexts.trace(kind));return true;}
            case SQUARE->{own.putBoolean("Square228",true);cue(p,VacantTexts.trace(kind));return true;}
            case BULB->{cue(p,"A bare bulb. No shade: that must have gone already.");return true;}
            case RADIATOR->{cue(p,"Cold. Turned off at the valve.");return true;}
            case HOOKS->{cue(p,"One coat on the hook by the door. It isn't on the list.");return true;}
            case MAILBOXES->{cue(p,"Four mailboxes. The one for the first floor has tape across its slot.");return true;}
            case DOCKET->{cue(p,own.getBoolean("Completed")?"The board on the van floor, where the docket was clipped.":"A removal docket, clipped to a board on the van floor, half filled in.");return true;}
            case VAN_DOOR->{cue(p,"The van's back doors, swung right round and hooked back against its sides.");return true;}
            case VAN_WHEEL->{cue(p,"A van wheel, the tread worn smooth on the curb side.");return true;}
            case STREETLAMP,LIST->{return false;}
            default->{}
        }
        if(!kind.piece())return false;
        if(VacantSite.van(rel)){cue(p,"It's in the van.");return true;}
        var piece=VacantSite.pieceAt(rel);if(piece==null)return false;
        if(piece.kind()==VacantBlock.Kind.CHAIR){
            if(flat.seat==STANDING){say(p,Component.literal("\"Leave the chair. The chair can wait.\"").withStyle(ChatFormatting.GRAY));return true;}
            if(flat.seat==SITTING){cue(p,"They're sitting in it.");return true;}
            if(flat.seat!=STOOD){cue(p,flat.seat==KEPT?"They're sitting in it again.":"It's only a chair.");return true;}
        }
        return lift(p,b,own,flat,piece);
    }
    static boolean lift(ServerPlayer p,BlockPos b,CompoundTag own,Flat flat,VacantSite.Piece piece){
        var l=p.serverLevel();
        VacantSite.lift(l,b,piece);flat.carried.put(piece.kind(),p.getUUID());take(p,own,piece);
        l.playSound(null,b.offset(piece.at()),SoundEvents.WOOD_HIT,SoundSource.BLOCKS,.9F,.6F);
        String hid=switch(piece.kind()){
            case DRESSER->" Behind it, pencil marks climb the wall.";
            case RUG->" The boards under it are paler, newer.";
            case BED->" Underneath, the floor is worn where its legs stood.";
            case WARDROBE->" The wallpaper behind it is brighter.";
            default->"";
        };
        var w=weight(piece.kind());boolean others=readers(l,b).size()>1;
        String feel=switch(w){case HEAVY->others?" It's heavy. Someone could take the other end.":" It's heavy.";case MEDIUM->others?" Awkward on your own.":"";default->"";};
        cue(p,"You lift "+piece.name()+"."+hid+feel);
        return true;
    }
    static boolean setDown(ServerPlayer p,BlockPos b,CompoundTag own,Flat flat,VacantSite.Piece piece){
        var l=p.serverLevel();int slot=-1;
        var used=new HashSet<>(flat.van.values());
        for(int i=0;i<VacantSite.SLOTS.size();i++){
            if(used.contains(i))continue;
            var at=b.offset(VacantSite.SLOTS.get(i));
            if(!l.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new AABB(at).expandTowards(0,1,0)).isEmpty())continue;
            slot=i;break;
        }
        if(slot<0){cue(p,"There's someone in the way.");return true;}
        VacantSite.stow(l,b,piece.kind(),slot);flat.van.put(piece.kind(),slot);flat.carried.remove(piece.kind());release(p,own);
        own.putInt("Loaded228",own.getInt("Loaded228")+1);
        l.playSound(null,b.offset(VacantSite.SLOTS.get(slot)),SoundEvents.WOOD_PLACE,SoundSource.BLOCKS,1F,.7F);
        if(piece.kind()!=VacantBlock.Kind.CHAIR){
            int n=flat.loaded();var line=VacantTexts.after(n);
            for(var r:readers(l,b))say(r,line);
            var t=actor(l,flat.tenant);if(t!=null)t.fade(n*FADE_EACH);
            if(n>=VacantSite.PIECES.size()&&flat.seat==STANDING){flat.seat=SITTING;flat.sittingSince=(int)flat.tick;}
            return true;
        }
        // The last chair is in the van: the flat is empty.
        flat.seat=GONE;var t=actor(l,flat.tenant);if(t!=null)t.discard();flat.tenant=null;
        VacantSite.lights(l,b,false,true);
        ready(l,flat,p,own,"carried_out_the_last_chair");
        cue(p,"You look up at the window. It's dark.");
        return true;
    }
    static boolean putBack(ServerPlayer p,BlockPos b,CompoundTag own,Flat flat,VacantSite.Piece piece,boolean chosen){
        var l=p.serverLevel();
        VacantSite.place(l,b,piece,true);flat.carried.remove(piece.kind());release(p,own);
        l.playSound(null,b.offset(piece.at()),SoundEvents.WOOD_PLACE,SoundSource.BLOCKS,.9F,.8F);
        if(piece.kind()!=VacantBlock.Kind.CHAIR||!chosen){if(chosen)cue(p,"You set "+piece.name()+" back where it stood.");return true;}
        // The last chair, put back: they sit down in it again.
        flat.seat=KEPT;var t=actor(l,flat.tenant);if(t!=null){sit(t,b);t.fade(Math.max(0,t.fade()-24));}
        ready(l,flat,p,own,"put_the_chair_back");
        for(var r:readers(l,b))say(r,VacantTexts.faint("They sit back down in it. \"Thank you.\""));
        return true;
    }
    /** Both choices end the story on the street; everyone in the flat when it was made goes down with it. */
    static void ready(ServerLevel l,Flat flat,ServerPlayer chooser,CompoundTag chosen,String outcome){
        mark(chosen,outcome);
        for(var r:readers(l,flat.base)){if(r==chooser)continue;var own=own(r);if(own.getBoolean("Completed"))continue;mark(own,outcome);save(r,own);}
    }
    private static void mark(CompoundTag own,String outcome){if(own.getBoolean("Ready"))return;own.putBoolean("Ready",true);own.putString("Outcome",outcome);own.putInt("Beat",own.getInt("Visit"));}

    // ------------------------------------------------------------------------------------------------ the flat's clock
    static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players())if(!p.isSpectator()&&p.isAlive()&&LiteraryVignettes.current(p)==PLACE&&b.equals(LiteraryVignettes.base(p,PLACE))&&VacantSite.inside(rel(p.position(),b)))out.add(p);
        return out;
    }
    @Nullable static LiteraryActor actor(ServerLevel l,@Nullable UUID id){if(id==null)return null;var e=l.getEntity(id);return e instanceof LiteraryActor a&&a.isAlive()?a:null;}
    static void clearActors(ServerLevel l,BlockPos b){
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,new AABB(b.offset(-7,-8,-19)).minmax(new AABB(b.offset(19,9,1))),a->a.role()==LiteraryActor.STRANGER&&a.phase()==TENANT))a.discard();
    }
    static void start(ServerLevel l,Flat flat){
        var b=flat.base;clearActors(l,b);VacantSite.reset(l,b,true);
        if(flat.after){aftermath(l,flat);return;}
        var a=LiteraryRegistry.ACTOR.get().create(l);
        if(a!=null){a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,TENANT);var at=Vec3.atBottomCenterOf(b.offset(VacantSite.TENANT_START));a.moveTo(at.x,at.y,at.z,90,0);if(l.addFreshEntity(a))flat.tenant=a.getUUID();}
    }
    /**
     * 0.4.244: the flat as the reader left it. Everything on the list gone, with what it hid there to see. The chair carried down:
     * nothing at all, and the bulbs off. The chair put back: the one who lived here in it, facing the window, no more there than when
     * the reader left, and the light on. Nobody asks anything.
     */
    static void aftermath(ServerLevel l,Flat flat){
        var b=flat.base;for(var piece:VacantSite.PIECES)VacantSite.lift(l,b,piece);
        VacantSite.lift(l,b,VacantSite.CHAIR_PIECE);VacantSite.lights(l,b,false,true);flat.seat=GONE;
    }
    /** Whether a flat comes back as its aftermath: everyone in it has finished it. Otherwise it is moving day. */
    static boolean afterOf(ServerLevel l,BlockPos b){return Aftermath.alone(readers(l,b),PLACE);}
    /**
     * Each reader in an aftermath flat who put the chair back has it back where it stood, the bulbs on, and the one who lived here in it,
     * no more there than when they left: the chair and the bulbs sent to them alone (again every five seconds, so a section loaded
     * afresh agrees), the figure seen by them alone. A reader who has gone takes their figure with them.
     */
    static void keptViews(ServerLevel l,Flat flat){
        var b=flat.base;var here=new HashSet<UUID>();
        for(var r:readers(l,b)){
            var own=own(r);if(!own.getBoolean("Completed")||!kept(own.getString("Outcome")))continue;here.add(r.getUUID());
            var figure=actor(l,flat.shown.get(r.getUUID()));
            if(figure==null){
                var a=LiteraryRegistry.ACTOR.get().create(l);
                if(a!=null){a.bind(PLACE,r.getUUID());a.appearance(LiteraryActor.STRANGER,TENANT);a.fade(KEPT_FADE);sit(a,b);if(l.addFreshEntity(a)){flat.shown.put(r.getUUID(),a.getUUID());figure=a;}}
                showKept(r,b);
            }else if(flat.tick%100==0)showKept(r,b);
            if(figure!=null)sit(figure,b);
        }
        for(var it=flat.shown.entrySet().iterator();it.hasNext();){var e=it.next();if(here.contains(e.getKey()))continue;var a=actor(l,e.getValue());if(a!=null)a.discard();it.remove();}
    }
    /** The chair where it stood and the flat's two bulbs on, for this reader's eyes only. */
    static void showKept(ServerPlayer r,BlockPos b){
        var chair=VacantSite.CHAIR_PIECE;
        r.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(b.offset(chair.at()),VacantBlock.of(chair.kind(),chair.facing())));
        for(var at:List.of(VacantSite.LIVING_BULB,VacantSite.BEDROOM_BULB))r.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(b.offset(at),VacantBlock.of(VacantBlock.Kind.BULB,net.minecraft.core.Direction.NORTH,0)));
    }
    static void sit(LiteraryActor a,BlockPos b){var at=Vec3.atBottomCenterOf(b.offset(VacantSite.CHAIR_PIECE.at()));a.moveTo(at.x,at.y,at.z,-90,0);a.setYHeadRot(-90);a.setYBodyRot(-90);a.setDeltaMovement(Vec3.ZERO);}

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();var seen=new HashSet<Long>();
        for(var p:server.getPlayerList().getPlayers()){
            if(p.isSpectator()||!p.isAlive()||LiteraryVignettes.current(p)!=PLACE)continue;
            var b=LiteraryVignettes.base(p,PLACE);if(b==null||!VacantSite.inside(rel(p.position(),b)))continue;
            var own=own(p);var flat=FLATS.computeIfAbsent(b.asLong(),k->new Flat(b));
            // 0.4.244: someone who has not finished it comes into its aftermath: it is moving day again, from the beginning.
            if(flat.after&&!own.getBoolean("Completed")){flat=new Flat(b);FLATS.put(b.asLong(),flat);}
            if(seen.add(b.asLong()))step(p.serverLevel(),flat);
            if(!own.getBoolean("Completed"))reader(p,b,own,flat);
            else if(flat.after&&Aftermath.counted(p,PLACE))cue(p,kept(own.getString("Outcome"))?VacantTexts.AFTER_KEPT:VacantTexts.AFTER_EMPTY);
        }
        // 0.4.260: nobody outside a flat carries any of its weight.
        for(var p:server.getPlayerList().getPlayers()){
            var b=LiteraryVignettes.current(p)==PLACE?LiteraryVignettes.base(p,PLACE):null;
            if(b==null||p.isSpectator()||!p.isAlive()||!VacantSite.inside(rel(p.position(),b)))unweigh(p);
        }
        for(var it=FLATS.values().iterator();it.hasNext();){var flat=it.next();if(seen.contains(flat.base.asLong())){flat.empty=0;continue;}
            if(++flat.empty>=EMPTY_RESET){var l=server.getLevel(NovelRooms.dimension(PLACE));if(l!=null&&l.isLoaded(flat.base)){clearActors(l,flat.base);VacantSite.reset(l,flat.base,true);}it.remove();}}
        // A piece carried out of the flat some other way (a door, a death, a logout) goes back to the reader's own things.
        if(server.getTickCount()%20==0)for(var p:server.getPlayerList().getPlayers()){
            boolean in=LiteraryVignettes.current(p)==PLACE&&LiteraryVignettes.base(p,PLACE)!=null&&VacantSite.inside(rel(p.position(),LiteraryVignettes.base(p,PLACE)))&&p.isAlive();
            if(in)continue;
            boolean has=holding(p);var own=has?own(p):null;
            if(!has){own=LiteraryVignettes.personal(LabyrinthData.get(server),p.getUUID(),PLACE);if(!own.contains("Carrying228"))continue;}
            var piece=carrying(own);release(p,own);save(p,own);
            if(piece!=null)for(var flat:FLATS.values())if(p.getUUID().equals(flat.carried.get(piece.kind()))){var l=server.getLevel(NovelRooms.dimension(PLACE));if(l!=null)VacantSite.place(l,flat.base,piece,true);flat.carried.remove(piece.kind());}
        }
    }
    static void step(ServerLevel l,Flat flat){
        var b=flat.base;
        if(flat.tick==0){flat.after=afterOf(l,b);start(l,flat);}
        flat.tick++;
        if(flat.after)keptViews(l,flat);
        weights(l,flat);
        var t=actor(l,flat.tenant);
        if(t!=null){
            switch(flat.seat){
                case STANDING,STOOD->{var r=closest(l,b,t.position());if(r!=null)SceneFootpaths.face(t,r.position(),10);}
                case SITTING->{
                    var chair=Vec3.atBottomCenterOf(b.offset(VacantSite.CHAIR_PIECE.at()));
                    // They go to the chair while nobody in the flat is looking at them.
                    if(t.position().distanceToSqr(chair)>.04){boolean watched=false;for(var r:readers(l,b))if(VacantSite.inFlat(rel(r.position(),b))&&r.hasLineOfSight(t))watched=true;if(!watched||flat.tick-flat.sittingSince>200)sit(t,b);}
                    else{sit(t,b);
                        // They get up when someone comes close: that's the last of it.
                        for(var r:readers(l,b))if(VacantSite.inFlat(rel(r.position(),b))&&r.position().distanceToSqr(chair)<6.25){
                            flat.seat=STOOD;var stand=Vec3.atBottomCenterOf(b.offset(VacantSite.STAND));t.moveTo(stand.x,stand.y,stand.z,0,0);
                            for(var each:readers(l,b))say(each,VacantTexts.faint(VacantTexts.LAST));break;}
                    }
                }
                case KEPT->sit(t,b);
                default->{}
            }
        }
        // Nothing of the tenant's is left lying about: a piece dropped from someone's arms goes back where it stood.
        if(flat.tick%10==0)for(var item:l.getEntitiesOfClass(ItemEntity.class,new AABB(b.offset(-7,-8,-19)).minmax(new AABB(b.offset(19,9,1))),i->loan(i.getItem())))item.discard();
    }
    @Nullable static ServerPlayer closest(ServerLevel l,BlockPos b,Vec3 from){ServerPlayer best=null;double d=Double.MAX_VALUE;for(var r:readers(l,b)){double dd=r.position().distanceToSqr(from);if(dd<d){d=dd;best=r;}}return best;}

    // ------------------------------------------------------------------------------------------------ each reader
    static void reader(ServerPlayer p,BlockPos b,CompoundTag own,Flat flat){
        var r=rel(p.position(),b);boolean changed=false;
        if(VacantSite.inFlat(r)&&!own.getBoolean("Flat228")){own.putBoolean("Flat228",true);changed=true;say(p,Component.literal(VacantTexts.GREETING));}
        if(VacantSite.inFlat(r)&&flat.seat==SITTING&&seated(p.serverLevel(),flat)&&!own.getBoolean("Sat228")){own.putBoolean("Sat228",true);changed=true;cue(p,VacantTexts.SITTING);}
        // A piece that has left the reader's arms (dropped, or put somewhere else) goes back where it stood.
        var piece=carrying(own);
        if(piece!=null&&!holding(p)){putBack(p,b,own,flat,piece,false);changed=true;cue(p,capital(piece.name())+" is back where it stood.");}
        // On the street, with the choice made, the story is over.
        if(own.getBoolean("Ready")&&!own.getBoolean("Completed")&&VacantSite.onStreet(r)){complete(p,own);changed=true;}
        if(changed)save(p,own);
    }
    static boolean seated(ServerLevel l,Flat flat){var t=actor(l,flat.tenant);return t!=null&&t.position().distanceToSqr(Vec3.atBottomCenterOf(flat.base.offset(VacantSite.CHAIR_PIECE.at())))<=.04;}
    static String capital(String s){return Character.toUpperCase(s.charAt(0))+s.substring(1);}
    /** The ending: the removal docket in the reader's hand, and their Witness line. */
    public static void complete(ServerPlayer p,CompoundTag own){
        if(own.getBoolean("Completed")||!own.getBoolean("Ready"))return;
        var book=LiteraryTexts.ending(p.getGameProfile().getName(),PLACE,own);
        if(!p.getInventory().add(book))p.drop(book,false);
        var story=WitnessAccount.Story.of(PLACE.id());if(story!=null)WitnessAccount.resolve(p,story,own.getString("Outcome"));
        own.putBoolean("Completed",true);LabyrinthData.get(p.server).setCompleted(PLACE.id(),true);
        cue(p,"carried_out_the_last_chair".equals(own.getString("Outcome"))?"The docket on the van floor is filled in, all but where it's going. You take it.":"Above you, the light's still on in the window. The docket on the van floor is filled in. You take it.");
    }
}
