package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.labyrinth.Artifacts.Kind;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSpawnPhantomsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;

/** 0.4.199, the audit's build 7: twenty artifacts, each given by the ending that earns it and each doing one thing for its own reader. */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ArtifactTests {
    private ArtifactTests(){}
    static List<ItemStack> held(ServerPlayer p,Kind k){var out=new ArrayList<ItemStack>();for(var s:p.getInventory().items)if(s.is(k.item()))out.add(s);return out;}
    static ItemStack own(ServerPlayer p,Kind k){return LiteraryVignettes.mark(p,k.place(),k.key,new ItemStack(k.item()));}
    static void tick(ServerPlayer p){p.tickCount=400;Artifacts.tick(new PlayerTickEvent.Post(p));}
    static void floor(GameTestHelper h,int size){for(int x=0;x<size;x++)for(int z=0;z<size;z++)h.setBlock(new BlockPos(x,0,z),Blocks.STONE);}

    @GameTest(template="empty",timeoutTicks=200)
    public static void eachRoomsEndingGivesItsArtifactOnceAndOnlyTheEndingThatEarnsIt(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"artifact_given");var data=LabyrinthData.get(h.getLevel().getServer());
        try{
            // The rooms that choose: only the ending that earns it.
            Artifacts.ended(p,WitnessAccount.Story.SEANCE,"heard_the_medium");Artifacts.ended(p,WitnessAccount.Story.HOTEL,HotelVignette.WALKED);Artifacts.ended(p,WitnessAccount.Story.FAMILY_COPY,"refused_the_familys_request_to_leave");
            h.assertTrue(held(p,Kind.RECORD).isEmpty()&&held(p,Kind.KEY_TAG).isEmpty()&&held(p,Kind.PHOTO).isEmpty(),"hearing the medium, walking out of the hotel and refusing the family give none of those three");
            h.assertTrue(held(p,Kind.DOOR_KEY).size()==1,"refusing the family gives the front-door key instead");
            var failed=LiteraryVignettes.personal(data,p.getUUID(),Kind.BLADE.place());failed.putBoolean("UncannyFailed",true);LiteraryVignettes.save(data,p.getUUID(),Kind.BLADE.place(),failed);
            Artifacts.ended(p,WitnessAccount.Story.LAYERED_MAN,"closed_the_cellar_after_the_blade_broke");
            h.assertTrue(held(p,Kind.BLADE).isEmpty(),"a failed uncanny room gives nothing");
            failed.remove("UncannyFailed");LiteraryVignettes.save(data,p.getUUID(),Kind.BLADE.place(),failed);
            Artifacts.ended(p,WitnessAccount.Story.SEANCE,"drove_the_family_out");Artifacts.ended(p,WitnessAccount.Story.HOTEL,HotelVignette.SIGNED);Artifacts.ended(p,WitnessAccount.Story.FAMILY_COPY,"left_when_the_family_asked");
            for(var k:Kind.values())if(k!=Kind.RECORD&&k!=Kind.KEY_TAG&&k!=Kind.PHOTO&&k!=Kind.VIAL&&k!=Kind.DOOR_KEY)Artifacts.ended(p,k.story,"came_through");
            h.assertTrue(held(p,Kind.DOOR_KEY).size()==1,"leaving the family copy when asked gives the photograph, and no second key");
            // The silo's comes through the account itself, as every room's does.
            WitnessAccount.resolve(p,WitnessAccount.Story.SILO,"opened_the_roof_and_returned_to_the_grain");
            for(var k:Kind.values()){
                var got=held(p,k);
                h.assertTrue(got.size()==1,k.id+" is given once: "+got.size());var s=got.get(0);
                h.assertTrue(Artifacts.mine(s,p,k)&&k.place().id().equals(VignetteYields.of(s)),k.id+" is the reader's own and offerable to the Mother");
                var lore=s.get(DataComponents.LORE);h.assertTrue(lore!=null&&lore.lines().stream().anyMatch(l->l.getString().equals(k.line)),k.id+" says what it does: "+lore);
                h.assertTrue(!k.line.matches("(?i).*\\b(red|yellow|white|black|blue|green|gold|grey|gray|orange|purple|pink|brown|silver|he|she|him|her|his|hers|boy|girl|man|woman|old|young)\\b.*")||k==Kind.DOLL,k.id+"'s line names no colour, age or gender of the reader: "+k.line);
            }
            Artifacts.ended(p,WitnessAccount.Story.SEANCE,"drove_the_family_out");Artifacts.ended(p,WitnessAccount.Story.BLY_ROUTE,"waited_in_a_shut_closet_until_the_lake");
            h.assertTrue(held(p,Kind.RECORD).size()==1&&held(p,Kind.DOLL).size()==1,"an ending recorded twice gives no second artifact");
            h.assertTrue(WitnessAccount.count(data,p.getUUID())==1,"the artifacts add no Witness credit: only the silo's own ending counts");
        }finally{NativeTestPlayers.remove(p);}
        h.succeed();
    }

    /** What each carried artifact does in its reader's own world, and that a copy in anyone else's hands does nothing. */
    @GameTest(template="empty",timeoutTicks=200)
    public static void theCarriedArtifactsWorkInTheirReadersWorldAndForNobodyElse(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"artifact_keeper");var other=NativeTestPlayers.survival(h,"artifact_other");var l=h.getLevel();
        floor(h,12);var at=h.absoluteVec(new Vec3(2.5,1,2.5));p.teleportTo(l,at.x,at.y,at.z,0,0);other.teleportTo(l,at.x+6,at.y,at.z,0,0);
        try{
            for(var k:Kind.values())p.getInventory().add(own(p,k));
            for(var k:Kind.values())other.getInventory().add(own(p,k));
            // The number card, the essay page and the doll: witches, the drowned and phantoms will not take them.
            var witch=h.spawn(EntityType.WITCH,new BlockPos(5,1,5));var drowned=h.spawn(EntityType.DROWNED,new BlockPos(6,1,5));var phantom=h.spawn(EntityType.PHANTOM,new BlockPos(5,3,6));
            witch.setTarget(p);drowned.setTarget(p);phantom.setTarget(p);
            h.assertTrue(witch.getTarget()==null&&drowned.getTarget()==null&&phantom.getTarget()==null,"the witch, the drowned and the phantom cannot fix on the keeper");
            witch.setTarget(other);drowned.setTarget(other);
            h.assertTrue(witch.getTarget()==other&&drowned.getTarget()==other,"anyone else carrying the same copies is fair game");
            var night=NeoForge.EVENT_BUS.post(new PlayerSpawnPhantomsEvent(p,2));var otherNight=NeoForge.EVENT_BUS.post(new PlayerSpawnPhantomsEvent(other,2));
            h.assertTrue(night.getResult()==PlayerSpawnPhantomsEvent.Result.DENY&&otherNight.getResult()!=PlayerSpawnPhantomsEvent.Result.DENY,"no phantoms come for the doll's keeper");
            h.assertTrue(!BlyFollows.carry(p),"and the Lady passes them by");
            // The hunter's tag: crouched, half as noticeable.
            var zombie=h.spawn(EntityType.ZOMBIE,new BlockPos(8,1,8));p.setPose(Pose.CROUCHING);other.setPose(Pose.CROUCHING);
            var seen=NeoForge.EVENT_BUS.post(new LivingEvent.LivingVisibilityEvent(p,zombie,1));var seenOther=NeoForge.EVENT_BUS.post(new LivingEvent.LivingVisibilityEvent(other,zombie,1));
            h.assertTrue(Math.abs(seen.getVisibilityModifier()-Artifacts.TAG_SEEN)<1e-6&&seenOther.getVisibilityModifier()==1,"crouched, the tag halves how far a monster sees its keeper: "+seen.getVisibilityModifier());
            p.setPose(Pose.STANDING);other.setPose(Pose.STANDING);
            h.assertTrue(NeoForge.EVENT_BUS.post(new LivingEvent.LivingVisibilityEvent(p,zombie,1)).getVisibilityModifier()==1,"standing, it does nothing");
            // The scrawled note: no zombie breaks a door near them.
            var door=new BlockPos(4,1,9);h.setBlock(door,Blocks.OAK_DOOR);var state=l.getBlockState(h.absolutePos(door));
            h.assertTrue(NeoForge.EVENT_BUS.post(new LivingDestroyBlockEvent(zombie,h.absolutePos(door),state)).isCanceled(),"a door near the note's keeper holds against the zombie");
            p.teleportTo(l,at.x+40,at.y,at.z,0,0);other.teleportTo(l,at.x+40,at.y,at.z,0,0);
            h.assertTrue(!NeoForge.EVENT_BUS.post(new LivingDestroyBlockEvent(zombie,h.absolutePos(door),state)).isCanceled(),"far off, it does not");
            p.teleportTo(l,at.x,at.y,at.z,0,0);other.teleportTo(l,at.x+6,at.y,at.z,0,0);
            // The receipt: one more point from food.
            p.getFoodData().setFoodLevel(10);other.getFoodData().setFoodLevel(10);
            NeoForge.EVENT_BUS.post(new LivingEntityUseItemEvent.Finish(p,new ItemStack(Items.BREAD),0,ItemStack.EMPTY));NeoForge.EVENT_BUS.post(new LivingEntityUseItemEvent.Finish(other,new ItemStack(Items.BREAD),0,ItemStack.EMPTY));
            h.assertTrue(p.getFoodData().getFoodLevel()==11&&other.getFoodData().getFoodLevel()==10,"the receipt's keeper is filled a point more");
            // The watch: the keeper's steps make no vibration.
            h.assertTrue(NeoForge.EVENT_BUS.post(new VanillaGameEvent(l,GameEvent.STEP,p.position(),GameEvent.Context.of(p))).isCanceled()
                    &&!NeoForge.EVENT_BUS.post(new VanillaGameEvent(l,GameEvent.STEP,other.position(),GameEvent.Context.of(other))).isCanceled()
                    &&!NeoForge.EVENT_BUS.post(new VanillaGameEvent(l,GameEvent.BLOCK_PLACE,p.position(),GameEvent.Context.of(p))).isCanceled(),"only the watch-keeper's footsteps go unheard");
            // The invitation: poison and hunger do not take; the blindfold only in the off hand.
            h.assertTrue(!p.addEffect(new MobEffectInstance(MobEffects.POISON,100))&&!p.addEffect(new MobEffectInstance(MobEffects.HUNGER,100))&&other.addEffect(new MobEffectInstance(MobEffects.POISON,100)),"poison and hunger do not reach the invitation's keeper");
            h.assertTrue(p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,100)),"the blindfold carried in the pack does nothing");p.removeEffect(MobEffects.BLINDNESS);
            p.setItemInHand(InteractionHand.OFF_HAND,own(p,Kind.BLINDFOLD));
            h.assertTrue(!p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,100))&&!p.addEffect(new MobEffectInstance(MobEffects.DARKNESS,100)),"in the off hand, nothing blinds them");
            // The key tag: a night's sleep in their world, and they wake fed.
            p.getFoodData().setFoodLevel(4);NeoForge.EVENT_BUS.post(new PlayerWakeUpEvent(p,false,false));
            h.assertTrue(p.getFoodData().getFoodLevel()==20,"the key tag's keeper wakes fed");
            // Bud's note: ice grips and powder snow bears; the ticket stub: a stronger stroke.
            h.assertTrue(Artifacts.friction(p,.98F)==Artifacts.NOTE_GRIP&&Artifacts.friction(other,.98F)==.98F&&Artifacts.friction(p,.6F)==.6F,"ice grips under the note's keeper like ordinary ground");
            h.assertTrue(Artifacts.snowBears(p)&&!Artifacts.snowBears(other),"powder snow bears the note's keeper");
            var boat=EntityType.BOAT.create(l);boat.moveTo(at.x,at.y,at.z+3);l.addFreshEntity(boat);p.startRiding(boat,true);
            h.assertTrue(Math.abs(Artifacts.boatPush(boat,.04F)-.05F)<1e-6&&Artifacts.boatPush(boat,-.005F)==-.005F,"the stub's keeper paddles a quarter harder forward, never backward");
            p.stopRiding();boat.discard();
            // The essay page and the stone: quicker in water, quicker crawling.
            tick(p);tick(other);
            h.assertTrue(p.getAttribute(NeoForgeMod.SWIM_SPEED).getValue()>1.3&&other.getAttribute(NeoForgeMod.SWIM_SPEED).getValue()==1,"the essay's keeper swims faster");
            p.setPose(Pose.SWIMMING);tick(p);
            h.assertTrue(Math.abs(p.getAttributeValue(Attributes.SNEAKING_SPEED)-.6)<1e-6,"crawling with the stone, twice the pace: "+p.getAttributeValue(Attributes.SNEAKING_SPEED));
            p.setPose(Pose.STANDING);tick(p);
            h.assertTrue(Math.abs(p.getAttributeValue(Attributes.SNEAKING_SPEED)-.3)<1e-6,"standing again, it goes");
            // The photograph near their bed, and the bracelet on two-thirds of a stomach: they mend.
            p.setRespawnPosition(l.dimension(),p.blockPosition(),0,true,false);p.setHealth(10);p.getFoodData().setFoodLevel(20);tick(p);
            h.assertTrue(p.getHealth()==11,"near their bed, the photograph's keeper mends: "+p.getHealth());
            p.setRespawnPosition(l.dimension(),p.blockPosition().offset(100,0,0),0,true,false);p.getFoodData().setFoodLevel(15);p.setHealth(10);tick(p);
            h.assertTrue(p.getHealth()==11,"away from it, on fifteen of twenty, the bracelet mends them: "+p.getHealth());
            other.setHealth(10);other.getFoodData().setFoodLevel(15);tick(other);
            h.assertTrue(other.getHealth()==10,"neither works for anyone else");
        }finally{NativeTestPlayers.remove(p);NativeTestPlayers.remove(other);}
        h.succeed();
    }

    /** The used artifacts: the whistle, the sitting record, the candle and the vial, and the blade that slows what it cuts. */
    @GameTest(template="empty",timeoutTicks=200)
    public static void theUsedArtifactsCallDriveBackShowAndSlow(GameTestHelper h){
        var p=NativeTestPlayers.survival(h,"artifact_user");var l=h.getLevel();
        floor(h,14);var at=h.absoluteVec(new Vec3(2.5,1,2.5));p.teleportTo(l,at.x,at.y,at.z,0,0);
        try{
            // The whistle: the keeper's own wandering companions come; one told to stay stays.
            var wolf=h.spawn(EntityType.WOLF,new BlockPos(11,1,11));wolf.tame(p);var sitting=h.spawn(EntityType.WOLF,new BlockPos(10,1,2));sitting.tame(p);sitting.setOrderedToSit(true);
            var stray=h.spawn(EntityType.WOLF,new BlockPos(9,1,9));
            h.assertTrue(Artifacts.whistle(p)==1&&(wolf.distanceTo(p)<6||wolf.getNavigation().isInProgress())&&sitting.isOrderedToSit(),"only the keeper's own free companion answers the whistle: "+wolf.distanceTo(p));
            // The sitting record: monsters within six blocks are driven back.
            var zombie=h.spawn(EntityType.ZOMBIE,new BlockPos(5,1,2));zombie.setTarget(p);var far=h.spawn(EntityType.ZOMBIE,new BlockPos(12,1,12));
            int driven=Artifacts.record(p);
            // Neighbouring test plots can stand monsters within six blocks too, so the count is only a floor; the far zombie is the check.
            h.assertTrue(driven>=1&&zombie.getDeltaMovement().x>0&&zombie.getTarget()==null&&far.getDeltaMovement().horizontalDistanceSqr()==0,"the record drives the near zombie back and off the keeper, and not the far one: "+driven+" driven, moving "+zombie.getDeltaMovement()+", target "+zombie.getTarget()+", far "+far.getDeltaMovement());
            var record=own(p,Kind.RECORD);p.setItemInHand(InteractionHand.MAIN_HAND,record);record.use(l,p,InteractionHand.MAIN_HAND);
            h.assertTrue(p.getCooldowns().isOnCooldown(record.getItem()),"then rests five minutes");
            var copy=record.copy();var other=NativeTestPlayers.survival(h,"artifact_borrower");other.teleportTo(l,at.x+1,at.y,at.z,0,0);other.setItemInHand(InteractionHand.MAIN_HAND,copy);
            h.assertTrue(!copy.use(l,other,InteractionHand.MAIN_HAND).getResult().consumesAction()&&!other.getCooldowns().isOnCooldown(copy.getItem()),"in anyone else's hand it does nothing");
            NativeTestPlayers.remove(other);
            // The blade: what it cuts is slowed.
            p.setItemInHand(InteractionHand.MAIN_HAND,own(p,Kind.BLADE));far.hurt(p.damageSources().playerAttack(p),1);
            h.assertTrue(far.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"the blade slows what it cuts");
            // The candle: lit, it shows the unseen nearby and burns a second a second; out, it keeps its wick.
            var candle=own(p,Kind.CANDLE);p.setItemInHand(InteractionHand.MAIN_HAND,candle);var hidden=h.spawn(EntityType.ZOMBIE,new BlockPos(4,1,5));hidden.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,400));
            candle.use(l,p,InteractionHand.MAIN_HAND);h.assertTrue(Artifacts.lit(candle),"the candle lights");
            p.tickCount=400;Artifacts.tick(new PlayerTickEvent.Post(p));
            h.assertTrue(hidden.hasEffect(MobEffects.GLOWING)&&candle.getDamageValue()==1,"lit, it shows the invisible zombie and burns a second");
            candle.use(l,p,InteractionHand.MAIN_HAND);Artifacts.tick(new PlayerTickEvent.Post(p));
            h.assertTrue(!Artifacts.lit(candle)&&candle.getDamageValue()==1,"blown out, it keeps what is left");
            candle.setDamageValue(Artifacts.CANDLE_SECONDS-1);candle.use(l,p,InteractionHand.MAIN_HAND);Artifacts.tick(new PlayerTickEvent.Post(p));
            h.assertTrue(p.getMainHandItem().isEmpty(),"at the end of its wick it is gone");
            // The vial: three bursts that slow and weaken what is near, then it is spent.
            var vial=own(p,Kind.VIAL);p.setItemInHand(InteractionHand.MAIN_HAND,vial);var near=h.spawn(EntityType.SKELETON,new BlockPos(4,1,3));
            vial.use(l,p,InteractionHand.MAIN_HAND);
            h.assertTrue(near.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)&&near.hasEffect(MobEffects.WEAKNESS)&&!stray.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)&&vial.getDamageValue()==1,"the dust slows and weakens the skeleton, not the wolf");
            vial.use(l,p,InteractionHand.MAIN_HAND);vial.use(l,p,InteractionHand.MAIN_HAND);
            h.assertTrue(p.getMainHandItem().isEmpty(),"after the third burst the vial is spent");
        }finally{NativeTestPlayers.remove(p);}
        h.succeed();
    }

    static void face(ServerPlayer p,float yaw){p.setYRot(yaw);p.yRotO=yaw;p.setYHeadRot(yaw);p.setXRot(0);p.xRotO=0;}
    static ItemStack planchette(ServerPlayer p){var s=new ItemStack(ClassicsRegistry.PLANCHETTE.get());net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putUUID(ClassicsVignettes.OWNER,p.getUUID());t.putString(ClassicsVignettes.KEY,"Planchette");});return s;}
    private static LiteraryTests.Fixture choices;
    @AfterBatch(batch="artifact_choices") public static void choicesDone(net.minecraft.server.level.ServerLevel l){if(choices!=null){choices.close();choices=null;}}
    /**
     * 0.4.199: the séance's and the family copy's other endings were left empty once the record and the photograph went to one
     * ending each. Hearing the medium out wakes the planchette, and the family is heard on the manor stairs; refusing to leave
     * gives the front-door key, and the family wakes them in their own bed one night in three.
     */
    @GameTest(template="empty",batch="artifact_choices",timeoutTicks=300)
    public static void theSeanceAndFamilyCopyEndingsThatGaveNothingNowEachGiveAndTake(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);choices=f;var l=h.getLevel();
        var heard=NativeTestPlayers.survival(h,"artifact_heard");var drove=NativeTestPlayers.survival(h,"artifact_drove");var refused=NativeTestPlayers.survival(h,"artifact_refused");var left=NativeTestPlayers.survival(h,"artifact_left");
        f.players.add(heard);f.players.add(drove);f.players.add(refused);f.players.add(left);
        floor(h,12);var at=h.absoluteVec(new Vec3(5.5,1,5.5));for(var p:List.of(heard,drove,refused,left))p.teleportTo(l,at.x,at.y,at.z,0,0);
        // The séance.
        WitnessAccount.resolve(heard,WitnessAccount.Story.SEANCE,Artifacts.HEARD);WitnessAccount.resolve(drove,WitnessAccount.Story.SEANCE,"drove_the_family_out");
        h.assertTrue(held(heard,Kind.RECORD).isEmpty()&&held(drove,Kind.RECORD).size()==1,"hearing the medium out gives no record; driving the family out does");
        heard.setItemInHand(InteractionHand.MAIN_HAND,planchette(heard));drove.setItemInHand(InteractionHand.MAIN_HAND,planchette(drove));
        h.assertTrue(Artifacts.planchetteWakes(heard)&&!Artifacts.planchetteWakes(drove),"the planchette wakes only for the one who heard her out");
        // Faced by the zombie's actual position: test plots may be placed turned.
        var behind=h.spawn(EntityType.ZOMBIE,new BlockPos(5,1,1));behind.setNoAi(true);
        var toward=behind.position().subtract(heard.position());float yaw=(float)(net.minecraft.util.Mth.atan2(toward.z,toward.x)*180/Math.PI)-90;
        face(heard,yaw+180);
        h.assertTrue(Artifacts.unseenHarm(heard)==behind,"it drifts toward the zombie behind them");
        face(heard,yaw);
        // Neighbouring test plots can hold monsters within sixteen blocks, so only this zombie is the check.
        h.assertTrue(Artifacts.unseenHarm(heard)!=behind,"turned to face it, it no longer drifts toward the zombie: facing "+heard.getViewVector(1)+", zombie at "+toward+", in sight "+heard.hasLineOfSight(behind));
        heard.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);h.assertTrue(!Artifacts.planchetteWakes(heard),"put away, it is still");
        // The cost: the family on the manor stairs, once a House day, for that reader only.
        var manor=f.origin.offset(io.github.knaitoe.theoldesthouse.house.HouseLayout.AXIS_X,1,io.github.knaitoe.theoldesthouse.house.HouseLayout.FRONT_DOOR_Z+3);
        h.assertTrue(!Artifacts.hearsTheSitting(heard),"outside the manor nobody is on the stairs");
        heard.teleportTo(f.in,manor.getX()+.5,manor.getY(),manor.getZ()+.5,0,0);heard.hasChangedDimension();drove.teleportTo(f.in,manor.getX()+.5,manor.getY(),manor.getZ()+.5,0,0);drove.hasChangedDimension();
        h.assertTrue(Artifacts.hearsTheSitting(heard)&&!Artifacts.hearsTheSitting(drove),"in the manor, the one who heard her out may hear the family; the one who drove them out never");
        h.assertTrue(Artifacts.sitting(heard)&&!Artifacts.hearsTheSitting(heard),"once a House day");
        // The family copy.
        WitnessAccount.resolve(refused,WitnessAccount.Story.FAMILY_COPY,Artifacts.REFUSED);WitnessAccount.resolve(left,WitnessAccount.Story.FAMILY_COPY,"left_when_the_family_asked");
        h.assertTrue(held(refused,Kind.DOOR_KEY).size()==1&&held(refused,Kind.PHOTO).isEmpty()&&held(left,Kind.PHOTO).size()==1&&held(left,Kind.DOOR_KEY).isEmpty(),"refusing gives the key, leaving the photograph");
        var key=held(refused,Kind.DOOR_KEY).get(0);var lore=key.get(DataComponents.LORE);
        h.assertTrue(Artifacts.mine(key,refused,Kind.DOOR_KEY)&&lore!=null&&lore.lines().stream().anyMatch(line->line.getString().equals(Kind.DOOR_KEY.line)),"the key is theirs and says what it does");
        var bed=h.absolutePos(new BlockPos(2,1,2));
        var night=NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent(refused,bed,net.minecraft.world.entity.player.Player.BedSleepingProblem.NOT_SAFE));
        var other=NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent(left,bed,net.minecraft.world.entity.player.Player.BedSleepingProblem.NOT_SAFE));
        h.assertTrue(night.getProblem()==null&&other.getProblem()==net.minecraft.world.entity.player.Player.BedSleepingProblem.NOT_SAFE,"with the key, monsters nearby do not keep them from a bed");
        refused.setHealth(6);NeoForge.EVENT_BUS.post(new PlayerWakeUpEvent(refused,false,false));
        h.assertTrue(refused.getHealth()==refused.getMaxHealth(),"and they wake mended");
        // The cost: in their own bed, one night in three, the family wakes them before dawn.
        long family=-1,quiet=-1;for(long d=0;d<40&&(family<0||quiet<0);d++){if(Artifacts.familyNight(refused.getUUID(),d)){if(family<0)family=d;}else if(quiet<0)quiet=d;}
        refused.setRespawnPosition(l.dimension(),bed,0,true,false);refused.startSleeping(bed);left.setRespawnPosition(l.dimension(),bed,0,true,false);left.startSleeping(bed);
        h.assertTrue(Artifacts.familyWakes(refused,family)&&!Artifacts.familyWakes(refused,quiet)&&!Artifacts.familyWakes(left,family),"asleep in their own bed on one of the family's nights, and only then, the one who refused is woken");
        refused.stopSleeping();refused.startSleeping(bed.east(4));
        h.assertTrue(!Artifacts.familyWakes(refused,family),"in any other bed they sleep through");
        refused.stopSleeping();refused.startSleeping(bed);Artifacts.wakeByFamily(refused);
        h.assertTrue(!refused.isSleeping()&&refused.getHealth()==refused.getMaxHealth(),"woken, never hurt");
        h.succeed();
    }
}
