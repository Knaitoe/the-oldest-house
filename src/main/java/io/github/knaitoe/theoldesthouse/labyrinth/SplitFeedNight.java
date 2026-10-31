package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;

/**
 * The split-feed bedroom's one night (0.4.201). The owner: "the player should only need to sleep once. You wake with mud on
 * your feet. The recording shows an empty bed, then someone wearing your face walking toward your parents' room. You look
 * into the actual hallway and see how it really moves." And: "there should be cues from the parents' room - affirmations,
 * dialogue, etc. if the npc makes it, the screen should flash red and audio cues should flare. blood should splatter the
 * player's screen. corpses should be findable inside the room, using the player's skin tone and hair texture. that triggers a
 * fail state, the npc reaching the room."
 * <p>After the one sleep the figure waits a beat at the far end of the hall, then lurches along it in jerks toward the
 * parents' door, their voices coming through it as it nears. At the door it reaches for the knob. A door the reader has shut
 * (having seen it, directly or on the feed) holds: the knob rattles, the parents settle, and the figure folds away. An open
 * door does not: it goes in, the reader's screen goes red with their blood, and the parents, who share the reader's own skin
 * and hair, are found dead in their bed.
 */
public final class SplitFeedNight {
    /** Where the figure stands after the reader wakes, the turn in the hall, and the cell before the parents' door. */
    public static final Vec3 START=new Vec3(1.5,5,-26.5),TURN=new Vec3(1.5,5,-14.5),DOOR_FRONT=new Vec3(3.3,5,-14.5);
    /** The parents' beds: the heads of the two singles pushed together in their room. */
    public static final BlockPos BED_A=new BlockPos(6,5,-26),BED_B=new BlockPos(7,5,-26);
    /** Ticks after waking before it moves; how long it works the knob; how long the night can run unresolved. */
    public static final int WAKE_BEAT=120,KNOB_TICKS=50,NIGHT_LIMIT=2400;
    /** One lurch a reader tick (every five game ticks), round and round: jerks and stillness, about half a block a second. */
    private static final double[] LURCH={.45,.2,0,0,.35,.1,0,0};
    /** Through their door as it comes: affirmations, then calling it in, by how far along the hall it has come. */
    private static final String[] VOICES={"Mm. Is that you, sweetheart?","We're so proud of you. You know that?","Don't stay up too late, okay?","Come here. Let me see you."};
    /**
     * 0.4.201 (owner: "The parents should speak to the reader when they first enter, them telling the reader how sleepy they -
     * the parents are - and that the reader should go to bed too. This instills the proximity of the parents prior to the bit
     * of horror"): from their bed, once, as the reader comes in. The last line is why their door stands open.
     */
    private static final String[] GREETING={"Is that you, sweetheart? We're turning in.","God, I'm wiped. I can barely keep my eyes open.","You should get to bed too. It's late.","Leave our door open, okay? In case you need us."};
    /** Ticks between the greeting's lines. */
    public static final int GREET_GAP=60;
    private SplitFeedNight(){}
    /** Before the night: the parents' greeting, a line every few seconds once the reader is near enough to hear them. */
    static void greet(ServerPlayer p,BlockPos b,CompoundTag own){
        int said=own.getInt("Greeted201");if(said>=GREETING.length)return;
        if(p.distanceToSqr(Vec3.atCenterOf(b.offset(UncannyRooms.PARENTS_DOOR)))>18*18)return;
        int age=own.getInt("UncannyAge");if(said>0&&age-own.getInt("GreetedAt201")<GREET_GAP)return;
        voice(p,b,GREETING[said]);own.putInt("Greeted201",said+1);own.putInt("GreetedAt201",age);
    }
    public static int greeted(CompoundTag own){return own.getInt("Greeted201");}
    public static String greeting(int line){return GREETING[line];}

    static Vec3 at(BlockPos b,Vec3 rel){return rel.add(b.getX(),b.getY(),b.getZ());}
    public static boolean doorOpen(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(UncannyRooms.PARENTS_DOOR));return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}
    /** A line from the parents' room: on the action bar, quoted, for a reader near enough to hear it. */
    private static void voice(ServerPlayer p,BlockPos b,String line){
        if(p.distanceToSqr(Vec3.atCenterOf(b.offset(UncannyRooms.PARENTS_DOOR)))>18*18)return;
        p.displayClientMessage(Component.literal("“"+line+"”").withStyle(net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.ITALIC),true);
        UncannyStories.heard(p,SoundEvents.WOOL_STEP,Vec3.atCenterOf(b.offset(BED_A)),.25F,.7F);
    }

    /** The parents lie asleep in their beds until the night goes wrong; then they lie there dead. */
    static void parents(ServerPlayer p,BlockPos b,CompoundTag own){
        boolean dead=own.contains("Slaughter201");
        for(var key:new String[]{"CastParentA","CastParentB"}){
            var a=UncannyStories.actor(p,own,key);if(a==null)continue;
            a.appearance(LiteraryActor.UNCANNY_PARENT,dead?1:0);
            var head=b.offset(key.endsWith("A")?BED_A:BED_B);
            if(!a.isSleeping()&&p.serverLevel().getBlockState(head).getBlock() instanceof BedBlock)a.startSleeping(head);
        }
    }

    /** One reader tick of the night, from the moment they wake. */
    static void night(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor doll){
        var l=p.serverLevel();int age=own.getInt("UncannyAge"),since=age-own.getInt("SecondSleepAge");
        if(UncannyStories.failed(own)||own.getBoolean("DollGone201")){doll.appearance(LiteraryActor.UNCANNY_MARIONETTE,0);return;}
        if(since<WAKE_BEAT){
            // Waiting at the far end of the hall, facing the way it will go.
            if(own.getInt("DollLeg201")==0&&doll.position().distanceToSqr(at(b,START))>.04)doll.moveTo(at(b,START).x,at(b,START).y,at(b,START).z,180,0);
            return;
        }
        int leg=own.getInt("DollLeg201");
        if(leg<2){
            double lurch=LURCH[Math.floorMod(since/5,LURCH.length)];
            if(lurch>0&&SceneFootpaths.step(doll,at(b,leg==0?TURN:DOOR_FRONT),lurch))own.putInt("DollLeg201",++leg);
            // How far along the hall: the turn is three quarters of the way.
            double done=leg>=1?.75+.25*(1-Math.min(1,doll.position().distanceTo(at(b,DOOR_FRONT))/1.8)):.75*(1-Math.min(1,(doll.getZ()-b.getZ()-TURN.z)/(START.z-TURN.z)));
            int said=own.getInt("ParentsLine201");int due=done<.05?0:done<.35?1:done<.7?2:3;
            if(said<=due&&said<VOICES.length){voice(p,b,VOICES[said]);own.putInt("ParentsLine201",said+1);}
            // "dragging the baseball bat along the drywall": the scrape of it, as it comes.
            if(lurch>0&&since%10==0)UncannyStories.heard(p,SoundEvents.GRINDSTONE_USE,doll.position().add(0,.4,0),.18F,.5F);
            // The wet purr that clicks on and off in its throat.
            if(since%40==0)UncannyStories.heard(p,SoundEvents.CAT_PURR,doll.getEyePosition(),.55F,.45F);
            if(since%40==20)UncannyStories.heard(p,SoundEvents.NOTE_BLOCK_HAT.value(),doll.getEyePosition(),.2F,.5F);
            if(leg==2){own.putInt("AtDoorAge201",age);doll.setYRot(-90);doll.yBodyRot=-90;doll.yHeadRot=-90;}
            return;
        }
        // At their door: the hand goes to the knob.
        doll.gesture(2);doll.setYRot(-90);doll.yBodyRot=-90;
        int there=age-own.getInt("AtDoorAge201");
        if(there%15==0)UncannyStories.heard(p,SoundEvents.IRON_TRAPDOOR_CLOSE,Vec3.atCenterOf(b.offset(UncannyRooms.PARENTS_DOOR)),.35F,1.8F);
        if(!doorOpen(l,b)){
            if(there>=KNOB_TICKS&&own.getBoolean("Ready")){
                voice(p,b,"...It's nothing. Go back to sleep.");
                own.putBoolean("DollGone201",true);doll.gesture(0);doll.appearance(LiteraryActor.UNCANNY_MARIONETTE,0);
            }
            if(since>NIGHT_LIMIT&&!own.getBoolean("Ready")){UncannyStories.setDoor(l,b.offset(UncannyRooms.PARENTS_DOOR),true);slaughter(p,b,own,doll,"waited_out_the_empty_bed");}
            return;
        }
        if(there==KNOB_TICKS/2)voice(p,b,"Honey? You're scaring me.");
        if(there>=KNOB_TICKS)slaughter(p,b,own,doll,"watched_the_hand_reach_the_door");
    }

    /** It goes in. Red on the reader's screen, their blood across it, the noise of it; then quiet, and the door left open. */
    static void slaughter(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor doll,String outcome){
        if(UncannyStories.failed(own)||own.getBoolean("Completed"))return;
        var l=p.serverLevel();
        own.putBoolean("UncannyFailed",true);own.putString("Failure",outcome);own.putBoolean("Ready",false);
        own.putLong("Slaughter201",l.getGameTime());
        var door=Vec3.atCenterOf(b.offset(UncannyRooms.PARENTS_DOOR));var bed=Vec3.atCenterOf(b.offset(BED_A));
        doll.gesture(0);doll.moveTo(b.getX()+6.5,b.getY()+5,b.getZ()-23.5,180,0);doll.appearance(LiteraryActor.UNCANNY_MARIONETTE,0);
        voice(p,b,"No— sweetheart, no—");
        UncannyStories.heard(p,SoundEvents.GOAT_SCREAMING_HURT,bed,1F,.62F);
        UncannyStories.heard(p,SoundEvents.PLAYER_HURT,bed,1F,.7F);
        UncannyStories.heard(p,SoundEvents.HONEY_BLOCK_BREAK,bed,1F,.6F);
        UncannyStories.heard(p,SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR,door,.8F,.7F);
        UncannyStories.heard(p,SoundEvents.FOX_SCREECH,bed,.7F,.5F);
        // Their sheets, soaked.
        for(var head:new BlockPos[]{BED_A,BED_B})for(var part:new BlockPos[]{head,head.south()}){
            var at=b.offset(part);var s=l.getBlockState(at);
            if(s.getBlock() instanceof BedBlock)l.setBlock(at,Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING,s.getValue(BedBlock.FACING)).setValue(BedBlock.PART,s.getValue(BedBlock.PART)).setValue(BedBlock.OCCUPIED,s.getValue(BedBlock.OCCUPIED)),UncannyRooms.FLAGS);
        }
        parents(p,b,own);
        p.displayClientMessage(Component.literal("Their door is open.").withStyle(net.minecraft.ChatFormatting.GRAY),false);
    }
    /** Whether the bed under a parent is part of their pair (for tests and the account). */
    public static boolean parentBed(ServerLevel l,BlockPos b,BlockPos head){var s=l.getBlockState(b.offset(head));return s.getBlock() instanceof BedBlock&&s.getValue(BedBlock.PART)==BedPart.HEAD&&s.getValue(BedBlock.FACING)==Direction.NORTH;}
}
