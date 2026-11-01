package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.opening.CompanionOrders;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.218, the replacement (docs/REPLACEMENT_0_4_218.md), the owner's October 31 idea: a street of boarded houses, dealt only to a
 * reader who has a companion following them.
 *
 * <p>The first moment the reader isn't looking at their companion, it is gone, and something is at their heels in its place: a
 * person, on all fours, who makes its noises (a cat's, or a dog's) and will not be shaken off. If it is left behind it is behind
 * the reader again the next time they look round. It is the reader's own: no one else sees it.
 *
 * <p>The reader's real companion is safe (it cannot be hurt while it is held), sitting shut in a crate in the back bedroom of the
 * one house on the street that isn't boarded, among the blankets and the collars and the ones that weren't so lucky. It can be heard
 * from the street. Opening the crate gives it back: it follows the reader again, and the replacement stands up for the first time,
 * says its name in its own voice, and goes and lies down with the others. Out on the street again, with the companion at their
 * side, the reader is ready: {@code took_it_home}; the account is a page left in the mailbox of the house they came out of.
 *
 * <p>A reader who leaves the street with their companion still in the crate has it sent home to the manor, where it waits for them,
 * sitting; no account that visit. They may come back another time.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Replacement {
    private Replacement(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.REPLACEMENT;
    /** The replacement's skin, on the stranger phase after the arcade's men. */
    public static final int PHASE=43;
    static final String HELD_TAG="Replacement218Held",INVULNERABLE_TAG="Replacement218Invulnerable";
    /** How close it keeps, how far is too far, and how often it makes a noise. */
    static final double HEEL=2.2,LOST=9;
    static final int NOISE_MIN=70,NOISE_SPREAD=90,TAKE_AFTER=20;

    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    @Nullable static BlockPos base(ServerPlayer p){return LiteraryVignettes.base(p,PLACE);}
    static Vec3 rel(Vec3 at,BlockPos b){return at.subtract(b.getX(),b.getY(),b.getZ());}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void heard(ServerPlayer p,Vec3 at,SoundEvent s,float volume,float pitch){p.connection.send(new ClientboundSoundPacket(Holder.direct(s),SoundSource.NEUTRAL,at.x,at.y,at.z,volume,pitch,p.getRandom().nextLong()));}
    static boolean dog(CompoundTag own){return own.getString("Species218").equals("dog");}
    /** The companion's own name, or what it is. */
    public static String name(CompoundTag own){String n=own.getString("Name218");return n.isEmpty()?(dog(own)?"the dog":"the cat"):n;}

    // ------------------------------------------------------------------------------------------------ who it is dealt to
    /** Whether a reader has a companion following them now: the street is dealt only to them. Kept for the dealer, which sees only saved data. */
    public static boolean hasCompanion(LabyrinthData d,UUID id){return LiteraryVignettes.personal(d,id,PLACE).getBoolean("Companion218");}
    static void noteCompanion(ServerPlayer p){
        if(!HouseDimensions.isHouseDimension(p.level().dimension()))return;
        boolean now=!CompanionOrders.followingAll(p).isEmpty();var own=own(p);
        if(own.getBoolean("Companion218")!=now){own.putBoolean("Companion218",now);save(p,own);}
    }

    // ------------------------------------------------------------------------------------------------ arrival
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        HERE.remove(p.getUUID());own.putBoolean("Taken218",false);own.putInt("Here218",0);own.remove("Stood218");own.remove("Lay218");own.remove("HouseCue218");own.remove("Freed218");
        if(own.getBoolean("Ready")&&!own.getBoolean("Completed")){cue(p,"There's something in the mailbox of the house you came out of.");return;}
        cue(p,"A street at midnight. Every house is boarded up, every window and every door. Far down the street, one door is open.");
    }

    // ------------------------------------------------------------------------------------------------ the replacement
    static List<LiteraryActor> replacements(ServerPlayer p,BlockPos b){
        return p.serverLevel().getEntitiesOfClass(LiteraryActor.class,new AABB(b.offset(-ReplacementSite.SIDE,-2,ReplacementSite.FAR)).minmax(new AABB(b.offset(ReplacementSite.SIDE+1,10,1))),
            a->a.role()==LiteraryActor.STRANGER&&a.phase()==PHASE&&a.owner().isPresent()&&a.owner().get().equals(p.getUUID()));
    }
    @Nullable public static LiteraryActor replacement(ServerPlayer p){var b=base(p);if(b==null)return null;var all=replacements(p,b);return all.isEmpty()?null:all.get(0);}
    static boolean sees(ServerPlayer p,Vec3 at){
        var eye=p.getEyePosition();var d=at.subtract(eye);double len=d.length();if(len<.5)return true;
        if(p.getLookAngle().dot(d.normalize())<.5)return false;
        return p.level().clip(new ClipContext(eye,at,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS;
    }
    /** Behind the reader, where they aren't looking. */
    static Vec3 behind(ServerPlayer p,double distance){var look=p.getLookAngle();var flat=new Vec3(look.x,0,look.z);if(flat.lengthSqr()<1e-4)flat=new Vec3(0,0,1);flat=flat.normalize();return p.position().subtract(flat.scale(distance));}
    static LiteraryActor spawn(ServerPlayer p,Vec3 at){
        var a=LiteraryRegistry.ACTOR.get().create(p.serverLevel());if(a==null)return null;
        a.bind(PLACE,p.getUUID());a.appearance(LiteraryActor.STRANGER,PHASE);a.gesture(CostumeNight.CRAWL);a.moveTo(at.x,at.y,at.z,p.getYRot(),0);
        return p.serverLevel().addFreshEntity(a)?a:null;
    }

    /** The first moment the reader isn't looking at it, their companion is taken to the crate, and the replacement is in its place. */
    static void take(ServerPlayer p,BlockPos b,CompoundTag own){
        var pets=CompanionOrders.followingAll(p);
        Vec3 where=null;
        for(var pet:pets)if(sees(p,pet.getEyePosition()))return;
        // A companion still in the crate from an earlier night stays held; a newly followed one joins it.
        var held=own.getList("Held218",Tag.TAG_INT_ARRAY).copy();boolean already=!held.isEmpty();
        for(var pet:pets){
            if(where==null){where=pet.position();own.putString("Species218",pet instanceof Wolf?"dog":"cat");if(pet.hasCustomName())own.putString("Name218",pet.getCustomName().getString());}
            hold(p,b,pet);var id=NbtUtils.createUUID(pet.getUUID());if(!held.contains(id))held.add(id);
        }
        if(pets.isEmpty()&&!already){if(own.getInt("Here218")<TAKE_AFTER*2)return;if(!own.contains("Species218"))own.putString("Species218","cat");}
        own.put("Held218",held);own.putBoolean("Taken218",true);
        if(where==null)where=behind(p,HEEL);
        for(var a:replacements(p,b))a.discard();
        spawn(p,where);
        save(p,own);
        cue(p,dog(own)?"At your heels, something says \"Woof.\"":"At your heels, something says \"Mrrow.\"");
    }
    static void hold(ServerPlayer p,BlockPos b,TamableAnimal pet){
        CompanionOrders.issue(pet,p,CompanionOrders.Order.STAY);pet.setOrderedToSit(true);
        if(!pet.isInvulnerable()){pet.setInvulnerable(true);pet.addTag(INVULNERABLE_TAG);}
        pet.addTag(HELD_TAG);pet.getNavigation().stop();pet.setDeltaMovement(Vec3.ZERO);
        var at=Vec3.atBottomCenterOf(b.offset(ReplacementSite.PEN));pet.teleportTo(at.x,at.y,at.z);
    }
    static void release(TamableAnimal pet){
        pet.removeTag(HELD_TAG);if(pet.getTags().contains(INVULNERABLE_TAG)){pet.removeTag(INVULNERABLE_TAG);pet.setInvulnerable(false);}
    }
    static List<TamableAnimal> held(ServerLevel l,CompoundTag own){
        var out=new ArrayList<TamableAnimal>();
        for(Tag t:own.getList("Held218",Tag.TAG_INT_ARRAY)){var e=l.getEntity(NbtUtils.loadUUID(t));if(e instanceof TamableAnimal pet&&pet.isAlive())out.add(pet);}
        return out;
    }

    /** It keeps at the reader's heels on all fours; left behind, it is behind them again the next time they look round. */
    static void heel(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor a,long now){
        if(own.getBoolean("Freed218")){toBlankets(p,b,own,a);return;}
        var target=behind(p,HEEL);var to=target.subtract(a.position());double d=Math.hypot(to.x,to.z);
        if(a.position().distanceTo(p.position())>LOST&&!sees(p,a.getEyePosition())&&!sees(p,target.add(0,.5,0))){a.moveTo(target.x,p.getY(),target.z,a.getYRot(),0);}
        else if(d>.2){double step=Math.min(d,p.isSprinting()?.42:.3);a.moveTo(a.getX()+to.x/d*step,p.getY(),a.getZ()+to.z/d*step,a.getYRot(),0);}
        var face=p.position().subtract(a.position());float yaw=(float)(Mth.atan2(face.z,face.x)*180/Math.PI)-90;a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);
        a.gesture(CostumeNight.CRAWL);
        // Its noises: now and then, and when the reader stops, sits, or crouches.
        if(now>=own.getLong("Noise218")){
            own.putLong("Noise218",now+NOISE_MIN+p.getRandom().nextInt(NOISE_SPREAD));
            heard(p,a.position(),dog(own)?SoundEvents.WOLF_AMBIENT:SoundEvents.CAT_AMBIENT,.8F,.55F);
            String[] lines=dog(own)?ReplacementTexts.DOG:ReplacementTexts.CAT;cue(p,lines[p.getRandom().nextInt(lines.length)]);
        }
        if(p.isShiftKeyDown()&&!own.getBoolean("Crouched218")){own.putBoolean("Crouched218",true);cue(p,"When you crouch, it lies down, and rolls half over, the way yours does.");}
        if(!p.isShiftKeyDown())own.remove("Crouched218");
        // In the house, it goes ahead of the reader once, to the bedroom door, and waits there.
        if(ReplacementSite.inHouse(rel(p.position(),b))&&!own.getBoolean("HouseCue218")){own.putBoolean("HouseCue218",true);cue(p,"The smell inside is the first thing. The second is how many bowls there are.");}
    }
    /** Done following: it stands, says the companion's name in its own voice, and goes to lie down with the others. */
    static void toBlankets(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor a){
        var to=Vec3.atBottomCenterOf(b.offset(ReplacementSite.LIES_DOWN)).subtract(a.position());double d=Math.hypot(to.x,to.z);
        if(d>.3){a.gesture(0);double step=Math.min(d,.12);a.moveTo(a.getX()+to.x/d*step,a.getY(),a.getZ()+to.z/d*step,a.getYRot(),0);float yaw=(float)(Mth.atan2(to.z,to.x)*180/Math.PI)-90;a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);}
        else if(!own.getBoolean("Lay218")){own.putBoolean("Lay218",true);a.gesture(CostumeNight.CRAWL);}
    }

    // ------------------------------------------------------------------------------------------------ hands
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var kind=ReplacementBlock.kind(l.getBlockState(b.offset(rel)));if(kind==null)return false;
        switch(kind){
            case CRATE->{openCrate(p,b,own);return true;}
            case CRATE_SIDE->{cue(p,held(l,own).isEmpty()?"Wire mesh, and a blanket inside.":"Through the mesh, eyes, and a sound you know.");return true;}
            case BOARDS,BOARD_DOOR->{cue(p,"Boarded from the outside. Nails, new ones, in old wood.");return true;}
            case COLLARS->{cue(p,"Collars on hooks, a long row of them, every one with a tag. Every tag has a name on it.");return true;}
            case BLANKET->{cue(p,"A blanket over something the size of a cat. You don't lift it.");return true;}
            case CAT->{cue(p,"It's lying very still. It has been lying very still for a long time.");return true;}
            case DOG->{cue(p,"Curled in the corner, nose to tail, not breathing.");return true;}
            case BOWLS->{cue(p,"Bowls. Some of them have been licked clean. Some of them never were.");return true;}
            case LITTER->{cue(p,"You don't look.");return true;}
            case PAPERS,BAGS,CANS->{cue(p,"Years of it.");return true;}
            case SOFA->{cue(p,"There's a dip in one end of the sofa, the shape of someone who sleeps curled up.");return true;}
            case LAMP->{cue(p,"A street lamp.");return true;}
            case MAILBOX->{cue(p,"Empty.");return true;}
            default->{return false;}
        }
    }
    static void openCrate(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();var crate=b.offset(ReplacementSite.CRATE);var s=l.getBlockState(crate);
        if(own.getBoolean("Freed218")){cue(p,"The crate is open, and empty, but for the blanket.");return;}
        var pets=held(l,own);
        l.setBlock(crate,s.setValue(ReplacementBlock.STAGE,1),3);l.playSound(null,crate,SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,.8F,1.2F);
        for(var pet:pets){release(pet);CompanionOrders.issue(pet,p,CompanionOrders.Order.FOLLOW);pet.setOrderedToSit(false);}
        own.putBoolean("Freed218",true);own.putInt("Freed218Count",pets.size());save(p,own);
        if(pets.isEmpty())cue(p,"The crate is empty. The blanket inside is still warm.");
        else cue(p,name(own).equals("the cat")||name(own).equals("the dog")?"It's yours. It comes out low and fast, and doesn't leave your side.":name(own)+" comes out low and fast, and doesn't leave your side.");
        var a=replacement(p);
        if(a!=null){a.gesture(0);own.putBoolean("Stood218",true);save(p,own);
            p.displayClientMessage(Component.literal("Behind you, it stands up, for the first time. It says \""+name(own)+"\", in its own voice, as if trying it out.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    }
    @SubscribeEvent public static void touched(PlayerInteractEvent.EntityInteract e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!(e.getTarget() instanceof LiteraryActor a)||a.role()!=LiteraryActor.STRANGER||a.phase()!=PHASE)return;
        if(e.getHand()!=net.minecraft.world.InteractionHand.MAIN_HAND)return;
        cue(p,own(p).getBoolean("Freed218")?"It doesn't look up.":"It pushes its head up under your hand. The hair is greasy, and warm.");e.setCanceled(true);
    }

    // ------------------------------------------------------------------------------------------------ the street
    /** Ticks each reader has been on the street this visit; not saved. */
    static final Map<UUID,Integer> HERE=new HashMap<>();
    public static void clearAll(){HERE.clear();}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();boolean slow=s.getTickCount()%40==3,often=s.getTickCount()%20==7;
        for(var p:s.getPlayerList().getPlayers()){
            if(slow)noteCompanion(p);
            if(!LiteraryVignettes.inside(p,PLACE)){HERE.remove(p.getUUID());if(often){var own=own(p);if(!own.getList("Held218",Tag.TAG_INT_ARRAY).isEmpty()&&!own.getBoolean("Freed218"))leftBehind(p,own);}continue;}
            var b=base(p);if(b==null)continue;long now=p.serverLevel().getGameTime();var own=own(p);
            int here=HERE.merge(p.getUUID(),1,Integer::sum);
            if(!own.getBoolean("Taken218")&&!own.getBoolean("Ready")){if(here>=TAKE_AFTER){own.putInt("Here218",here);take(p,b,own);}continue;}
            if(own.getBoolean("Ready")){if(!own.getBoolean("Freed218"))for(var a:replacements(p,b))a.discard();continue;}
            var before=own.copy();
            var a=replacement(p);
            if(a==null&&!own.getBoolean("Freed218"))a=spawn(p,behind(p,HEEL));
            if(a!=null)heel(p,b,own,a,now);
            // A held companion can be heard from the street, faintly, now and then.
            if(!own.getBoolean("Freed218")&&now%120==60&&!held(p.serverLevel(),own).isEmpty())heard(p,Vec3.atCenterOf(b.offset(ReplacementSite.PEN)),dog(own)?SoundEvents.WOLF_WHINE:SoundEvents.CAT_STRAY_AMBIENT,.5F,1F);
            // Out on the street again with the companion at their side: home.
            if(own.getBoolean("Freed218")&&ReplacementSite.street(rel(p.position(),b))){
                boolean together=true;for(var pet:held(p.serverLevel(),own))if(pet.distanceTo(p)>16)together=false;
                if(together){own.putBoolean("TookHome218",true);LiteraryVignettes.ready(p,PLACE,own,"took_it_home");
                    var mailbox=b.offset(ReplacementSite.MAILBOX);var ms=p.serverLevel().getBlockState(mailbox);if(ReplacementBlock.is(ms,ReplacementBlock.Kind.MAILBOX))p.serverLevel().setBlock(mailbox,ms.setValue(ReplacementBlock.STAGE,1),3);
                    cue(p,"The street is quiet behind you. In the mailbox of the house you came out of, there's a page that wasn't there before.");}
            }
            if(!own.equals(before))save(p,own);
        }
    }
    /** Gone from the street with the companion still in the crate: it is sent home to the manor, and waits there, sitting. */
    /** Where a companion left behind goes to wait: inside the manor's front hall. */
    public static @Nullable Vec3 home(net.minecraft.server.MinecraftServer s){var origin=HouseSavedData.get(s).houseOrigin();return origin==null?null:Vec3.atBottomCenterOf(origin.offset(HouseLayout.AXIS_X,1,HouseLayout.THRESHOLD_Z+4));}
    static void leftBehind(ServerPlayer p,CompoundTag own){
        var s=p.server;var manor=s.getLevel(HouseDimensions.INTERIOR);var street=s.getLevel(NovelRooms.dimension(PLACE));var at=home(s);
        if(street!=null)for(var pet:held(street,own)){release(pet);
            if(at==null||manor==null)continue;
            var moved=pet.changeDimension(new DimensionTransition(manor,at,Vec3.ZERO,0,0,DimensionTransition.PLACE_PORTAL_TICKET));
            if(moved instanceof TamableAnimal home){CompanionOrders.issue(home,p,CompanionOrders.Order.STAY);home.setOrderedToSit(true);}
        }
        own.remove("Held218");own.putBoolean("LeftBehind218",true);own.putInt("TimesLeft218",own.getInt("TimesLeft218")+1);save(p,own);
        cue(p,dog(own)?"Somewhere behind you, something barks in your companion's voice. Yours has gone home ahead of you. It will be waiting.":"Somewhere behind you, something meows in your companion's voice. Yours has gone home ahead of you. It will be waiting.");
    }
}
