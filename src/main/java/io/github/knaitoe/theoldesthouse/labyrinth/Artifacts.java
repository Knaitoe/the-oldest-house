package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSpawnPhantomsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

/**
 * The audit's build 7 (docs/ARTIFACTS_0_4_199.md): twenty rooms that gave nothing to carry out now give one artifact each, and
 * each does one small thing from its room's story. The owner: "most of the items you provided the player with don't really have
 * an effect on the gameplay, either inside or outside of the house."
 *
 * <p>Each is given once, when the room's ending is recorded ({@link WitnessAccount}), and only on the ending that earns it. Like
 * the keepsakes ({@link Keepsakes}) it is marked as its reader's own, works only for them, is offerable to the Mother and adds no
 * Witness credit. "Their world" means outside the House; the House's own dark, hunters and rooms are never touched by an effect
 * that is only for their world.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Artifacts {
    public static final String STATE="artifacts_0199";
    public static final int CANDLE_SECONDS=1200, VIAL_USES=3, WHISTLE_COOLDOWN=1200, RECORD_COOLDOWN=6000, BLADE_SLOW=30, DUST_TICKS=200;
    public static final double WHISTLE_REACH=64, RECORD_REACH=6, VIAL_REACH=5, SCRAWL_REACH=16, BED_REACH=32, CANDLE_REACH=12, TAG_SEEN=.5;
    /** A quarter more push on the paddle; ice no slicker than ordinary ground; a third more speed in water; crawling at twice the pace. */
    public static final float STUB_PUSH=1.25F, NOTE_GRIP=.6F;
    public static final double ESSAY_SWIM=.33, STONE_CRAWL=.3;
    /** On two-thirds of a full stomach, mending as a full one would. */
    public static final int BRACELET_FOOD=14;
    static final ResourceLocation SWIM=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"essay_swim"),CRAWL=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"stone_crawl");
    private Artifacts(){}

    /** Each artifact: its item, the room whose ending gives it, the key it is marked with, and a line in the room's words saying what it does. */
    public enum Kind {
        DOLL("flora_doll",WitnessAccount.Story.BLY_ROUTE,"Doll","She waited with you. Kept close, the Lady passes you by, and nothing comes down out of a sleepless sky."),
        TAG("hunter_tag",WitnessAccount.Story.ELK_CARCASSES,"HunterTag","Cut from the hide that hid you. Low and still, you are twice as hard to notice."),
        CARD("number_card",WitnessAccount.Story.COSTUME_NIGHT,"NumberCard","An entrant, not the thirteenth. Out in your world, witches let an entrant be."),
        STUB("ticket_stub",WitnessAccount.Story.MOVIE_NIGHT,"TicketStub","Admit one. You rowed out and back; a boat goes a little quicker for you."),
        NOTE("bud_note",WitnessAccount.Story.WINTER_LAKE,"BudNote","Signed by all four: the dare was done. Out in your world, the ice holds you and the snow bears you up."),
        WHISTLE("counsellor_whistle",WitnessAccount.Story.CAMP_BLOOD,"Whistle","Blown, it calls your own animals in, as the counsellors called the campers. Once a minute."),
        ESSAY("essay_page",WitnessAccount.Story.DROWNED_TOWN,"EssayPage","The town's story in a student's hand. Out in your world, the drowned let you pass, and the water is quicker for you."),
        SCRAWL("scrawled_note",WitnessAccount.Story.MOTION_LIGHT,"ScrawledNote","The warning that got you out. Out in your world, nothing dead can break a door down near you."),
        BLADE("broken_blade",WitnessAccount.Story.LAYERED_MAN,"Blade","The shift is finished. What it cuts is slow to follow."),
        RECEIPT("diner_receipt",WitnessAccount.Story.SECOND_WAITRESS,"Receipt","The meal, paid for. Out in your world, what you eat fills you a little more."),
        WATCH("old_watch",WitnessAccount.Story.FLOORBOARDS,"Watch","Wrapped in cotton, quiet in your pocket. Nothing listening in the ground hears you walk."),
        BLINDFOLD("folded_blindfold",WitnessAccount.Story.CLAP,"Blindfold","The game's cloth, folded away. In your other hand, out in your world, nothing blinds you."),
        INVITATION("masque_invitation",WitnessAccount.Story.MASQUE,"Invitation","The party you left alive. Out in your world, poison and hunger do not reach you."),
        RECORD("sitting_record",WitnessAccount.Story.SEANCE,"SittingRecord","Read aloud, it drives back what crowds you, as it ended the sittings. Once every five minutes."),
        KEY_TAG("key_tag",WitnessAccount.Story.HOTEL,"KeyTag","To match the master key. Waking in your own world, you wake fed."),
        PHOTO("family_photograph",WitnessAccount.Story.FAMILY_COPY,"FamilyPhotograph","The house they remembered, as it was. Near your own bed, you mend faster."),
        STONE("initial_stone",WitnessAccount.Story.BARN_WELL,"InitialStone","The wait, kept. Low gaps are easier going."),
        BRACELET("patient_bracelet",WitnessAccount.Story.HOSPITAL,"Bracelet","The night on the ward. Out in your world, you mend on less than a full stomach."),
        CANDLE("dare_candle",WitnessAccount.Story.DARE_IN_THE_DARK,"Candle","The dare done. Lit, it shows what hides by not being seen. Twenty minutes of wick."),
        VIAL("dust_vial",WitnessAccount.Story.SILO,"DustVial","The roof, opened. Broken open, it slows and weakens what is near you. Three times."),
        /** The other ending of the family copy (0.4.199): the reader would not leave the house they remembered. */
        DOOR_KEY("front_door_key",WitnessAccount.Story.FAMILY_COPY,"FrontDoorKey","You would not leave the house they remembered. Carried, any bed in your own world is home: nothing nearby keeps you from sleeping, and you wake mended.");
        public final String id,key,line;public final WitnessAccount.Story story;
        Kind(String id,WitnessAccount.Story story,String key,String line){this.id=id;this.story=story;this.key=key;this.line=line;}
        public LabyrinthPlace place(){return LabyrinthPlace.byId(story.id);}
        public Item item(){return ArtifactRegistry.item(this);}
    }
    /** The line under an artifact's name, for {@link Keepsakes#line}. */
    public static String line(LabyrinthPlace place,String key){for(var k:Kind.values())if(k.key.equals(key)&&k.story.id.equals(place.id()))return k.line;return null;}

    // ------------------------------------------------------------------ giving

    /**
     * Whether this ending earns the room's artifact. Most rooms record an ending only when the reader came through, and the
     * experiences only when it was lived through; these choose: the séance's record only for driving the family out, the hotel's
     * tag only for settling or signing, the family's photograph only for leaving when they asked, and none for a failed uncanny room.
     */
    static boolean earned(Kind k,ServerPlayer p,String outcome){
        return switch(k){
            case RECORD->"drove_the_family_out".equals(outcome);
            case KEY_TAG->HotelVignette.SETTLED.equals(outcome)||HotelVignette.SIGNED.equals(outcome);
            case PHOTO->"left_when_the_family_asked".equals(outcome);
            case DOOR_KEY->REFUSED.equals(outcome);
            case SCRAWL,BLADE,RECEIPT->!UncannyStories.failed(LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),k.place()));
            default->true;
        };
    }
    /** A room's ending, as WitnessAccount records it: its artifact, if this ending earns one. */
    public static void ended(ServerPlayer p,WitnessAccount.Story story,String outcome){for(var k:Kind.values())if(k.story==story&&earned(k,p,outcome))give(p,k);}
    /** The artifact, marked as this reader's own, once ever. */
    public static boolean give(ServerPlayer p,Kind k){
        var data=LabyrinthData.get(p.server);var own=data.stateEntry(STATE,p.getStringUUID());
        if(own.getBoolean(k.key))return false;own.putBoolean(k.key,true);data.setStateEntry(STATE,p.getStringUUID(),own);
        LiteraryVignettes.give(p,LiteraryVignettes.mark(p,k.place(),k.key,new ItemStack(k.item())));return true;
    }
    public static boolean given(ServerPlayer p,Kind k){return LabyrinthData.get(p.server).stateEntry(STATE,p.getStringUUID()).getBoolean(k.key);}

    // ------------------------------------------------------------------ whose it is

    /** This reader's own artifact of that kind; read on either side, so the client's movement can use it too. */
    public static boolean mine(ItemStack s,Player p,Kind k){
        if(s.isEmpty()||!s.is(k.item()))return false;var data=s.get(DataComponents.CUSTOM_DATA);if(data==null)return false;var t=data.copyTag();
        return t.hasUUID(LiteraryVignettes.OWNER)&&t.getUUID(LiteraryVignettes.OWNER).equals(p.getUUID());
    }
    public static boolean carries(Player p,Kind k){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(mine(inv.getItem(i),p,k))return true;return false;}
    public static boolean ownWorld(Entity e){return !HouseDimensions.isHouseDimension(e.level().dimension());}
    /** Only the game's own creatures are driven back, slowed or shown; the House's are its own. */
    static boolean vanilla(Entity e){return BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getNamespace().equals("minecraft");}

    // ------------------------------------------------------------------ movement, read on both sides (mixins)

    /** The ticket stub: a boat paddled forward gets a quarter more push. */
    public static float boatPush(Boat boat,float push){return push>0&&boat.getControllingPassenger() instanceof Player p&&carries(p,Kind.STUB)?push*STUB_PUSH:push;}
    /** Bud's note: in their world, ice is no slicker than ordinary ground under them. */
    public static float friction(LivingEntity e,float friction){return friction>NOTE_GRIP&&e instanceof Player p&&ownWorld(p)&&carries(p,Kind.NOTE)?NOTE_GRIP:friction;}
    /** Bud's note: in their world, powder snow bears them as it bears a fox. */
    public static boolean snowBears(Entity e){return e instanceof Player p&&ownWorld(p)&&carries(p,Kind.NOTE);}

    // ------------------------------------------------------------------ carried

    @SubscribeEvent public static void tick(PlayerTickEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!p.isAlive()||p.isSpectator())return;
        steps(p);
        if(p.tickCount%10!=0)return;
        boolean world=ownWorld(p);
        if(planchetteWakes(p)){var harm=unseenHarm(p);if(harm!=null)drift(p,harm);}
        if(p.tickCount%200==0&&hearsTheSitting(p)&&p.getRandom().nextInt(SITTING_CHANCE)==0)sitting(p);
        if(world&&p.getSleepTimer()>=FAMILY_WAKES_AT&&familyWakes(p,day(p))){wakeByFamily(p);return;}
        modifier(p,NeoForgeMod.SWIM_SPEED,SWIM,ESSAY_SWIM,AttributeModifier.Operation.ADD_MULTIPLIED_BASE,world&&carries(p,Kind.ESSAY));
        modifier(p,Attributes.SNEAKING_SPEED,CRAWL,STONE_CRAWL,AttributeModifier.Operation.ADD_VALUE,p.getPose()==Pose.SWIMMING&&!p.isInWater()&&carries(p,Kind.STONE));
        if(world&&mine(p.getOffhandItem(),p,Kind.BLINDFOLD)){p.removeEffect(MobEffects.BLINDNESS);p.removeEffect(MobEffects.DARKNESS);}
        if(world&&carries(p,Kind.INVITATION)){p.removeEffect(MobEffects.POISON);p.removeEffect(MobEffects.HUNGER);}
        if(p.tickCount%20==0)candle(p);
        if(world&&p.tickCount%50==0&&p.isHurt()&&nearBed(p)&&carries(p,Kind.PHOTO))p.heal(1);
        if(world&&p.tickCount%80==0&&p.isHurt()&&p.getFoodData().getFoodLevel()>=BRACELET_FOOD&&p.getFoodData().getFoodLevel()<18
                &&p.serverLevel().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)&&carries(p,Kind.BRACELET)){p.heal(1);p.causeFoodExhaustion(6);}
    }
    static void modifier(ServerPlayer p,Holder<Attribute> attribute,ResourceLocation id,double amount,AttributeModifier.Operation op,boolean on){
        var inst=p.getAttribute(attribute);if(inst==null)return;boolean has=inst.hasModifier(id);
        if(on&&!has)inst.addTransientModifier(new AttributeModifier(id,amount,op));else if(!on&&has)inst.removeModifier(id);
    }
    static boolean nearBed(ServerPlayer p){var bed=p.getRespawnPosition();return bed!=null&&p.getRespawnDimension().equals(p.level().dimension())&&bed.distSqr(p.blockPosition())<=BED_REACH*BED_REACH;}

    /** The doll keeps phantoms away; in their world the number card keeps witches off and the essay page the drowned. */
    @SubscribeEvent public static void phantoms(PlayerSpawnPhantomsEvent e){if(carries(e.getEntity(),Kind.DOLL))e.setResult(PlayerSpawnPhantomsEvent.Result.DENY);}
    @SubscribeEvent public static void target(LivingChangeTargetEvent e){
        if(!(e.getNewAboutToBeSetTarget() instanceof ServerPlayer p))return;var mob=e.getEntity();
        if(mob instanceof Phantom&&carries(p,Kind.DOLL)||ownWorld(p)&&(mob instanceof Witch&&carries(p,Kind.CARD)||mob instanceof Drowned&&carries(p,Kind.ESSAY)))e.setCanceled(true);
    }
    @SubscribeEvent public static void hurt(LivingIncomingDamageEvent e){
        if(e.getEntity() instanceof ServerPlayer p&&ownWorld(p)){var by=e.getSource().getEntity();
            if(by instanceof Witch&&carries(p,Kind.CARD)||by instanceof Drowned&&carries(p,Kind.ESSAY)||by instanceof Phantom&&carries(p,Kind.DOLL)){e.setCanceled(true);return;}}
        // The worker's blade: what it cuts is slowed a moment.
        if(e.getSource().getDirectEntity() instanceof ServerPlayer p&&e.getEntity()!=p&&mine(p.getMainHandItem(),p,Kind.BLADE))
            e.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,BLADE_SLOW,1),p);
    }
    /** The hunter's tag: crouched, monsters notice them at half the distance. */
    @SubscribeEvent public static void seen(LivingEvent.LivingVisibilityEvent e){
        if(e.getEntity() instanceof Player p&&p.isCrouching()&&e.getLookingEntity() instanceof Enemy&&carries(p,Kind.TAG))e.modifyVisibility(TAG_SEEN);
    }
    /** The scrawled note: in their world, no zombie breaks a door within sixteen blocks of them. */
    @SubscribeEvent public static void doors(LivingDestroyBlockEvent e){
        if(!(e.getEntity() instanceof Zombie)||!(e.getState().getBlock() instanceof DoorBlock)||!(e.getEntity().level() instanceof ServerLevel l)||HouseDimensions.isHouseDimension(l.dimension()))return;
        for(var p:l.players())if(p.blockPosition().distSqr(e.getPos())<=SCRAWL_REACH*SCRAWL_REACH&&carries(p,Kind.SCRAWL)){e.setCanceled(true);return;}
    }
    /** The receipt: in their world, food fills one more point. */
    @SubscribeEvent public static void ate(LivingEntityUseItemEvent.Finish e){
        if(e.getEntity() instanceof ServerPlayer p&&ownWorld(p)&&e.getItem().getFoodProperties(p)!=null&&carries(p,Kind.RECEIPT))p.getFoodData().eat(1,0F);
    }
    /** The old man's watch: their footsteps make no vibration for anything listening. */
    @SubscribeEvent public static void steps(VanillaGameEvent e){
        if(e.getCause() instanceof ServerPlayer p&&e.getVanillaEvent().value()==GameEvent.STEP.value()&&carries(p,Kind.WATCH))e.setCanceled(true);
    }
    /** The blindfold, in the off hand, keeps blindness and darkness off in their world; the invitation, carried, poison and hunger. */
    @SubscribeEvent public static void effects(MobEffectEvent.Applicable e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!ownWorld(p))return;MobEffect effect=e.getEffectInstance().getEffect().value();
        if((effect==MobEffects.BLINDNESS.value()||effect==MobEffects.DARKNESS.value())&&mine(p.getOffhandItem(),p,Kind.BLINDFOLD)
                ||(effect==MobEffects.POISON.value()||effect==MobEffects.HUNGER.value())&&carries(p,Kind.INVITATION))e.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
    }
    /** The hotel's key tag: waking in their world after a night's sleep, they are fed in full; the front-door key, mended. */
    @SubscribeEvent public static void woke(PlayerWakeUpEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.wakeImmediately()||e.updateLevel()||!ownWorld(p))return;
        if(carries(p,Kind.KEY_TAG)){var food=p.getFoodData();food.setFoodLevel(20);food.setSaturation(Math.max(food.getSaturationLevel(),10F));}
        // The front-door key: they wake mended.
        if(carries(p,Kind.DOOR_KEY))p.setHealth(p.getMaxHealth());
    }

    // ------------------------------------------------------------------ 0.4.199: the two choices that gave nothing

    /**
     * Giving the séance's record and the family's photograph to one ending each left the other endings empty, so those choices
     * read as wrong answers. Each of those endings now gives and takes something of its own (docs/ARTIFACTS_0_4_199.md).
     * <ul>
     * <li><b>Heard the medium out:</b> the séance's planchette, which every ending gives, wakes for that reader. Held, it drifts
     * toward whatever means them harm and is out of their sight. The family goes on sitting, and in the manor that reader hears
     * them on the stairs now and then.</li>
     * <li><b>Would not leave the family copy:</b> the front-door key. Carried, any bed in their own world is home: nothing nearby
     * keeps them from sleeping, and they wake mended. But the family is still in the house: one night in three, in their own bed,
     * someone moving about wakes them before dawn.</li>
     * </ul>
     */
    public static final String HEARD="heard_the_medium",REFUSED="refused_the_familys_request_to_leave";
    public static final String PLANCHETTE_LINE="She was heard out to the end. Held, it drifts toward what means you harm and cannot be seen.";
    public static final double PLANCHETTE_REACH=16;
    /** One chance in this many, every ten seconds in the manor, that the family is on the stairs; at most once a House day. */
    public static final int SITTING_CHANCE=12, STEP_TICKS=9, STEPS=5, FAMILY_NIGHTS=3, FAMILY_WAKES_AT=50;
    record Steps(BlockPos from,Vec3 dir,int start){}
    private static final Map<UUID,Steps> STAIRS=new HashMap<>();

    public static long day(ServerPlayer p){return p.server.overworld().getDayTime()/24000L;}
    static String ending(ServerPlayer p,WitnessAccount.Story story){return WitnessAccount.record(LabyrinthData.get(p.server),p.getUUID()).getCompound("Stories").getString(story.id);}
    public static boolean heardHerOut(ServerPlayer p){return HEARD.equals(ending(p,WitnessAccount.Story.SEANCE));}
    public static boolean refused(ServerPlayer p){return REFUSED.equals(ending(p,WitnessAccount.Story.FAMILY_COPY));}
    /** The séance's own planchette, given to this reader. */
    public static boolean planchette(ItemStack s,Player p){
        if(!s.is(ClassicsRegistry.PLANCHETTE.get()))return false;var data=s.get(DataComponents.CUSTOM_DATA);if(data==null)return false;var t=data.copyTag();
        return t.hasUUID(ClassicsVignettes.OWNER)&&t.getUUID(ClassicsVignettes.OWNER).equals(p.getUUID());
    }
    public static boolean planchetteWakes(ServerPlayer p){return (planchette(p.getMainHandItem(),p)||planchette(p.getOffhandItem(),p))&&heardHerOut(p);}
    /** The nearest thing within sixteen blocks that means them harm and that they cannot see: in the House only what is hunting them. */
    public static Mob unseenHarm(ServerPlayer p){
        boolean world=ownWorld(p);Mob best=null;double near=Double.MAX_VALUE;var look=p.getViewVector(1);
        for(var m:p.serverLevel().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(PLANCHETTE_REACH),m->m.isAlive()&&(m.getTarget()==p||world&&m instanceof Enemy))){
            var to=m.getEyePosition().subtract(p.getEyePosition());double d=to.lengthSqr();
            if(to.normalize().dot(look)>.5&&p.hasLineOfSight(m))continue;
            if(d<near){best=m;near=d;}
        }
        return best;
    }
    /** A faint pull from the hand toward it, and now and then the dry scrape of the planchette, seen and heard by the holder alone. */
    static void drift(ServerPlayer p,Mob harm){
        var from=p.position().add(0,1.1,0);var dir=harm.getEyePosition().subtract(from).normalize();
        for(int i=1;i<=3;i++){var at=from.add(dir.scale(.45+i*.3));p.serverLevel().sendParticles(p,ParticleTypes.SOUL,true,at.x,at.y,at.z,1,0,0,0,0);}
        if(p.tickCount%40==0)LiteraryVignettes.heard(p,SoundEvents.WOOD_HIT,BlockPos.containing(from.add(dir.scale(1.5))),.2F,1.7F);
    }
    static boolean inManor(ServerPlayer p){
        var origin=io.github.knaitoe.theoldesthouse.house.HouseSavedData.get(p.server).houseOrigin();
        return origin!=null&&p.level().dimension().equals(HouseDimensions.INTERIOR)&&io.github.knaitoe.theoldesthouse.house.HouseLayout.isInsideDomesticVolume(p.getX()-origin.getX(),p.getY()-origin.getY(),p.getZ()-origin.getZ());
    }
    /** One who heard the medium out, in the manor, not yet today. */
    public static boolean hearsTheSitting(ServerPlayer p){
        if(!heardHerOut(p)||!inManor(p))return false;var own=LabyrinthData.get(p.server).stateEntry(STATE,p.getStringUUID());return own.getLong("SittingDay")!=day(p)+1;
    }
    /** The family on the stairs: a few steps climbing somewhere behind them, then nothing. Theirs alone to hear. */
    public static boolean sitting(ServerPlayer p){
        var data=LabyrinthData.get(p.server);var own=data.stateEntry(STATE,p.getStringUUID());own.putLong("SittingDay",day(p)+1);data.setStateEntry(STATE,p.getStringUUID(),own);
        var behind=p.getViewVector(1).multiply(-1,0,-1).normalize();var side=new Vec3(-behind.z,0,behind.x);
        STAIRS.put(p.getUUID(),new Steps(BlockPos.containing(p.position().add(behind.scale(6)).add(side.scale(2)).add(0,-2,0)),side.scale(-.7),p.tickCount));
        return true;
    }
    static void steps(ServerPlayer p){
        var s=STAIRS.get(p.getUUID());if(s==null)return;int age=p.tickCount-s.start();
        if(age<0||age>STEP_TICKS*STEPS){STAIRS.remove(p.getUUID());return;}
        if(age%STEP_TICKS!=0)return;int n=age/STEP_TICKS;
        LiteraryVignettes.heard(p,SoundEvents.WOOD_STEP,BlockPos.containing(Vec3.atCenterOf(s.from()).add(s.dir().scale(n)).add(0,n*.5,0)),.35F,.85F+n*.02F);
    }
    /** One night in three, by the day and the reader, the family is up and about. */
    public static boolean familyNight(UUID id,long day){return Math.floorMod(java.util.Objects.hash(id,day),FAMILY_NIGHTS)==0;}
    /** One who would not leave, asleep in their own bed on one of the family's nights, long enough to be woken. */
    public static boolean familyWakes(ServerPlayer p,long day){
        if(!p.isSleeping()||!refused(p)||!familyNight(p.getUUID(),day))return false;
        var bed=p.getRespawnPosition();var at=p.getSleepingPos().orElse(null);
        if(bed==null||at==null||!bed.equals(at)||!p.getRespawnDimension().equals(p.level().dimension()))return false;
        var own=LabyrinthData.get(p.server).stateEntry(STATE,p.getStringUUID());return own.getLong("WokenDay")!=day+1;
    }
    /** Woken before dawn by someone moving about in the house: no night passes. Never hurt. */
    public static void wakeByFamily(ServerPlayer p){
        var data=LabyrinthData.get(p.server);var own=data.stateEntry(STATE,p.getStringUUID());own.putLong("WokenDay",day(p)+1);data.setStateEntry(STATE,p.getStringUUID(),own);
        var at=p.blockPosition();p.stopSleepInBed(true,true);
        LiteraryVignettes.heard(p,SoundEvents.WOOD_STEP,at.offset(3,-1,2),.5F,.9F);LiteraryVignettes.heard(p,SoundEvents.WOODEN_DOOR_CLOSE,at.offset(-4,0,3),.4F,.8F);
        p.displayClientMessage(Component.literal("Someone is moving about in the house.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);
    }
    /** The key: in their own world a bed is not refused them for monsters nearby. */
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST) public static void bed(net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent e){
        var p=e.getEntity();if(e.getProblem()==Player.BedSleepingProblem.NOT_SAFE&&ownWorld(p)&&carries(p,Kind.DOOR_KEY))e.setProblem(null);
    }
    @SubscribeEvent public static void stopping(net.neoforged.neoforge.event.server.ServerStoppingEvent e){STAIRS.clear();}

    // ------------------------------------------------------------------ used

    /** The whistle, the sitting record, the candle and the vial: used from the hand, only by their own reader. */
    public static final class Used extends Item {
        final Kind kind;
        public Used(Properties props,Kind kind){super(props);this.kind=kind;}
        @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
            var s=player.getItemInHand(hand);
            if(!(player instanceof ServerPlayer p))return InteractionResultHolder.sidedSuccess(s,level.isClientSide);
            if(!mine(s,p,kind)){p.displayClientMessage(Component.literal("It isn't yours.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);return InteractionResultHolder.fail(s);}
            switch(kind){
                case WHISTLE->{whistle(p);p.getCooldowns().addCooldown(this,WHISTLE_COOLDOWN);}
                case RECORD->{record(p);p.getCooldowns().addCooldown(this,RECORD_COOLDOWN);}
                case CANDLE->light(p,s);
                case VIAL->dust(p,s,hand);
                default->{}
            }
            return InteractionResultHolder.consume(s);
        }
        @Override public boolean isFoil(ItemStack s){return kind==Kind.CANDLE&&lit(s)||super.isFoil(s);}
    }
    /** The whistle: the reader's own companions within reach come to them, unless told to stay. */
    public static int whistle(ServerPlayer p){
        var level=p.serverLevel();int came=0;
        for(var pet:level.getEntitiesOfClass(TamableAnimal.class,p.getBoundingBox().inflate(WHISTLE_REACH),a->a.isAlive()&&p.getUUID().equals(a.getOwnerUUID())&&!a.isOrderedToSit()&&!a.isPassenger()&&!a.isLeashed()&&!a.getTags().contains(MotherOfStrays.PET))){
            // Far off, or with no way through, it comes to the reader's side as a called animal does; near, it walks.
            if(pet.distanceToSqr(p)>12*12||!pet.getNavigation().moveTo(p,1.3))fetch(pet,p);
            came++;
        }
        level.playSound(null,p.blockPosition(),SoundEvents.NOTE_BLOCK_FLUTE.value(),SoundSource.PLAYERS,1.4F,2F);
        return came;
    }
    /** A clear, supported spot within two blocks of the reader, for a called companion; it stays where it is if there is none. */
    static void fetch(TamableAnimal pet,ServerPlayer p){
        var level=p.serverLevel();var base=p.blockPosition();
        for(int r=1;r<=2;r++)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){
            if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;var at=base.offset(dx,0,dz);
            if(!level.getBlockState(at.below()).isFaceSturdy(level,at.below(),net.minecraft.core.Direction.UP))continue;
            var box=pet.getType().getDimensions().makeBoundingBox(Vec3.atBottomCenterOf(at));
            if(!level.noCollision(pet,box))continue;
            pet.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,pet.getYRot(),pet.getXRot());pet.getNavigation().stop();return;
        }
    }
    /** The sitting record, read aloud: the game's monsters within six blocks are driven back and lose hold of the reader. */
    public static int record(ServerPlayer p){
        var level=p.serverLevel();int driven=0;
        for(var m:level.getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(RECORD_REACH),m->m instanceof Enemy&&m.isAlive()&&vanilla(m))){
            m.knockback(1.6,p.getX()-m.getX(),p.getZ()-m.getZ());m.hurtMarked=true;if(m.getTarget()==p)m.setTarget(null);driven++;
        }
        level.playSound(null,p.blockPosition(),SoundEvents.BOOK_PAGE_TURN,SoundSource.PLAYERS,1F,.6F);
        level.sendParticles(ParticleTypes.ENCHANT,p.getX(),p.getY()+1.2,p.getZ(),40,1.5,.6,1.5,.4);
        return driven;
    }
    static final String LIT="CandleLit0199";
    public static boolean lit(ItemStack s){var data=s.get(DataComponents.CUSTOM_DATA);return data!=null&&data.copyTag().getBoolean(LIT);}
    static void light(ServerPlayer p,ItemStack s){
        boolean on=!lit(s);CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putBoolean(LIT,on));
        p.serverLevel().playSound(null,p.blockPosition(),on?SoundEvents.FLINTANDSTEEL_USE:SoundEvents.CANDLE_EXTINGUISH,SoundSource.PLAYERS,.7F,1.2F);
    }
    /** A lit candle burns a second a second while carried, and shows what is near and unseen; at the end of its wick it is gone. */
    static void candle(ServerPlayer p){
        var inv=p.getInventory();
        for(int i=0;i<inv.getContainerSize();i++){var s=inv.getItem(i);if(!mine(s,p,Kind.CANDLE)||!lit(s))continue;
            s.setDamageValue(s.getDamageValue()+1);
            if(s.getDamageValue()>=s.getMaxDamage()){inv.setItem(i,ItemStack.EMPTY);p.displayClientMessage(Component.literal("The candle burns down to nothing.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);continue;}
            reveal(p);
        }
    }
    public static int reveal(ServerPlayer p){
        int shown=0;
        for(var e:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(CANDLE_REACH),e->e!=p&&!(e instanceof Player pl&&pl.isSpectator())&&(e.hasEffect(MobEffects.INVISIBILITY)||e.isInvisible()&&vanilla(e)))){
            e.addEffect(new MobEffectInstance(MobEffects.GLOWING,30,0,true,false));shown++;
        }
        return shown;
    }
    /** The vial, broken open: the dust slows and weakens the game's monsters within five blocks for ten seconds. Three times. */
    public static int dust(ServerPlayer p,ItemStack s,InteractionHand hand){
        var level=p.serverLevel();int touched=0;
        level.sendParticles(new DustParticleOptions(new Vector3f(.86F,.74F,.32F),1.6F),p.getX(),p.getY()+1,p.getZ(),70,2.2,.8,2.2,0);
        level.playSound(null,p.blockPosition(),SoundEvents.GLASS_BREAK,SoundSource.PLAYERS,.6F,1.4F);
        for(var m:level.getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(VIAL_REACH),m->m instanceof Enemy&&m.isAlive()&&vanilla(m))){
            m.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,DUST_TICKS,1),p);m.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,DUST_TICKS,0),p);touched++;
        }
        s.hurtAndBreak(1,p,LivingEntity.getSlotForHand(hand));
        return touched;
    }
}
