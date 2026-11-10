package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;

/**
 * 0.4.439: the last night at the machine. Agreed with the owner (rework 4): the workers' talk by the depot's side door draws the reader
 * in; two of them walk a woman out of the depot and lay her on the belt, tired and dead-eyed, not resisting; the belt carries her into
 * the housing on the three strokes the reader has heard since childhood; what comes out of the far end goes on the pile with her
 * slippers and the band from her wrist. She is Dana, 141-A, from room 2, whose pasted note is under the depot ("Rm 2. No more
 * deliveries. Up before the shift." in the night book). The owner, November 9: "gore is appropriate... It should horrify the reader not
 * just on an intellectual level but a visceral, reactionary one too". It is an adult; nothing of a child is ever shown this way, and
 * nothing sexual is ever shown.
 * <p>
 * The owner, on the breaker: "The player should be able to shut the belt off to save her; the account remembers that, but Dana does not
 * get off the belt. She does not respond to the player and just stares ahead, arms swaying slightly off the edge of the belt and eyes
 * occasionally looking to one side." Pulled before her feet reach the slot, the breaker stops the belt and she stays where they put
 * her, for the rest of the night (SAVED). Pulled once the machine has her, it stops with her half in (LATE). Pulled before the men
 * fetch her, she stays on her cot in room 2. Her clothing is in scraps (the owner: "She doesn't need to be in the nude, but her
 * clothing should be in scraps"; "bare torso too, using the navel shadowing"), painted on her own skin (borrasca_dana).
 * <p>
 * Everything here is the reader's own site and record. Called from {@link BorrascaStory#reader} every five ticks on the last visit.
 * <p>
 * 0.4.444, the owner: "She should spawn on this on day 4, not spawn elsewhere then walk onto it. You should have operators that are
 * watching her go in, referencing the shiny gentleman taking care of her." She is on the belt, at its head, when the night begins, and
 * the two men stand beside it. The belt starts when the reader comes up to the yard; the men watch her go and say who has her now.
 * Nobody fetches her, walks her out or lays her down any more. Pulled before the belt starts, the breaker leaves her where she lies.
 */
public final class BorrascaFeeding {
    private BorrascaFeeding(){}
    public static final int IDLE=0,RIDE=4,INTAKE=5,OUTFLOW=6,SAVED=7,LATE=8;
    /** Room 2, where she was kept. Her cot is empty on the last night: she is already up on the belt. */
    public static final int DANA_ROOM=2;
    /** The head of the belt, where she lies when the night begins, and where the two men stand to watch her go. */
    public static final BlockPos BELT_HEAD=new BlockPos(0,12,-180);
    static final Vec3 WATCH_NEAR=new Vec3(-1.6,12,-183.5),WATCH_FAR=new Vec3(-1.6,12,-181.0);
    /** The belt's rubber is nine sixteenths up its block (BorrascaFittingBlock BELT). She lies on it with her feet to the north. */
    public static final double BELT_TOP=12+9/16.0,BELT_X=.5;
    /** Where her feet are laid, the slot's mouth (the housing's south face), and where her head has gone in after her. */
    public static final double LAID_Z=-179.9,SLOT_Z=-186.0,GONE_Z=-188.0;
    static final double RIDE_SPEED=.02,OUT=.03,OUT_START=-191.8,OUT_END=-200.2;
    /**
     * 0.4.439, the owner: "when she hits the blade it shouldn't be quick... the game should track the splitting/tearing of her body so it
     * doesn't tear all at once, it should be a really gruesome affair". The slot's mouth is the blade as the reader sees it: from there
     * the belt draws her in at a crawl, her body cut where the mouth is, a slice at a time (LiteraryActor.torn, drawn by
     * client.BorrascaNativeArt). She is two blocks long, feet first.
     */
    public static final double BODY=2.0,INTAKE_SPEED=.0028;
    /** Where the machine catches on bone and the belt strains before it lurches on: ankles, knees, hips, ribs, shoulders, jaw. */
    static final double[] BONES={.10,.38,.78,1.12,1.36,1.58};
    /** Her voice as it goes in (the owner: "her awwwoo turning into an AAHHHHH and then -"), each from the depth it starts at. */
    static final double[] SCREAM_AT={.02,.12,.24,.36,.50,.66,.84,1.02,1.16};
    static final String[] SCREAM={"awwwoo","awwwwooo","awwwooOOO","AWWOOAAAH","AAHHHHH","AAAHHHHHHH","AAAAHHHHHHHHHH","AAHHH-HH","-"};
    /** From here on she makes no sound and lies still. */
    public static final double SILENT=1.16;
    /**
     * The owner: "Can you overlay the machinery with the shiny gentleman 'eating' her? ... Just a flash for about two seconds". When the
     * machine reaches her chest a reader who can see the slot sees him there for two seconds instead of the machine
     * (client.BorrascaFeedingClient draws it from her synced depth). The account remembers it (GentlemanAtMachine).
     */
    public static final double GENTLEMAN_AT=1.02;
    /** The pile at the end of the belt, between the belt and the first row of stakes. */
    public static final BlockPos PILE_REMAINS=new BlockPos(0,12,-201),PILE_HAIR=new BlockPos(-1,12,-201),PILE_BAND=new BlockPos(1,12,-201);
    /** The men's lines. They only ever talk to each other. */
    public static final String[] WATCH={"There she goes. The Gentleman'll look after her now.","He looks after all of them.","My dad used to say that. Same words."};
    static final String BREAKER="Breaker's gone.",LEAVE="Leave her. She won't get off.";
    static final String STALL="It catches on the bones. Give it a second.",HOSE="Hose the belt down after.";

    public static int state(CompoundTag own){return own.getInt("Feed");}
    /** Whether the two men are out in the yard for this (so BorrascaStory.cast leaves them visible). */
    public static boolean workersOut(CompoundTag own){return own.getBoolean("FeedPlaced")&&!own.getBoolean("FeedWorkersGone");}

    public static void tick(ServerPlayer p,BlockPos b,CompoundTag own,@Nullable LiteraryActor dana,@Nullable LiteraryActor w0,@Nullable LiteraryActor w1){
        var l=p.serverLevel();boolean power=!own.getBoolean("PowerOff");int state=state(own);
        int t=own.getInt("FeedTicks")+5;own.putInt("FeedTicks",t);
        outflow(p,b,own,power);
        if(state>=INTAKE&&state!=SAVED)dust(p,b,own);
        if(dana==null)return;
        switch(state){
            case IDLE->{
                lie(dana,b,new Vec3(BELT_X,BELT_TOP,LAID_Z),180,false);
                if(w0!=null&&w1!=null&&!own.getBoolean("FeedPlaced")&&l.isLoaded(BorrascaRooms.at(b,BELT_HEAD))){
                    place(w0,b,WATCH_NEAR,-90);place(w1,b,WATCH_FAR,-90);
                    for(var w:new LiteraryActor[]{w0,w1}){w.setInvisible(false);w.gesture(0);BorrascaStory.resync(p,w);}
                    own.putBoolean("FeedPlaced",true);own.remove("FeedWorkersGone");
                }
                watch(dana,w0,w1);
                if(!ready(p,b))return;
                own.putDouble("FeedZ",LAID_Z);
                if(power)set(own,RIDE);
                else{own.putBoolean("DanaSaved",true);set(own,SAVED);if(w0!=null)BorrascaStory.say(p,w0,BREAKER);BorrascaStory.save(p,own);}
            }
            case RIDE->{
                double z=own.getDouble("FeedZ");
                if(!power){own.putBoolean("DanaSaved",true);set(own,SAVED);lie(dana,b,new Vec3(BELT_X,BELT_TOP,z),180,false);if(w0!=null)BorrascaStory.say(p,w0,BREAKER);BorrascaStory.save(p,own);return;}
                z-=RIDE_SPEED*5;own.putDouble("FeedZ",z);lie(dana,b,new Vec3(BELT_X,BELT_TOP,z),180,false);
                if(t==10&&w0!=null)BorrascaStory.say(p,w0,WATCH[0]);else if(t==60&&w1!=null)BorrascaStory.say(p,w1,WATCH[1]);else if(t==110&&w0!=null)BorrascaStory.say(p,w0,WATCH[2]);
                watch(dana,w0,w1);
                if(z<=SLOT_Z)set(own,INTAKE);
            }
            case INTAKE->{watch(dana,w0,w1);intake(p,b,own,dana,w0,power,t);}
            case OUTFLOW->{if(t==60&&w1!=null)BorrascaStory.say(p,w1,HOSE);leave(p,b,own,w0,w1,t);}
            case LATE->{late(p,b,own,dana,w0,t);leave(p,b,own,w0,w1,t);}
            case SAVED->{
                lie(dana,b,new Vec3(BELT_X,BELT_TOP,own.getDouble("FeedZ")),180,false);
                if(t==25&&w1!=null)BorrascaStory.say(p,w1,LEAVE);
                leave(p,b,own,w0,w1,t);
            }
            default->{}
        }
    }
    private static void set(CompoundTag own,int state){own.putInt("Feed",state);own.putInt("FeedTicks",0);}
    /** An authored place on this reader's hill, in the world (the ridge is an authored region, so this is the site's own shift). */
    static Vec3 world(BlockPos b,Vec3 rel){return Vec3.atLowerCornerOf(b).add(BorrascaRooms.world(rel));}

    /** The reader is close enough to the head of the belt to see her on it, out in the yard and not in the depot or under it. */
    static boolean ready(ServerPlayer p,BlockPos b){
        var r=BorrascaRooms.rel(p,b);
        if(r.y<10||r.y>24)return false;
        boolean depot=r.x>=-29&&r.x<-13&&r.z>=-205&&r.z<-168&&r.y<19;if(depot)return false;
        double hx=r.x-.5,hz=r.z+177.5;
        return hx*hx+hz*hz<26*26;
    }
    /** The men, while they stand, keep their eyes on her. */
    private static void watch(LiteraryActor dana,@Nullable LiteraryActor w0,@Nullable LiteraryActor w1){
        for(var w:new LiteraryActor[]{w0,w1}){
            if(w==null||w.isInvisible()||w.getPersistentData().contains("BorrascaSpeed"))continue;
            face(w,(float)Math.toDegrees(Math.atan2(-(dana.getX()-w.getX()),dana.getZ()+1-w.getZ())));
        }
    }
    private static void place(LiteraryActor a,BlockPos b,Vec3 rel,float yaw){BorrascaCast.stop(a);var at=world(b,rel);a.moveTo(at.x,at.y,at.z,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);}
    private static void face(LiteraryActor a,float yaw){a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);}

    /** Lying on her back with her feet toward the yaw she faces: on the cot, and on the belt. */
    static void lie(LiteraryActor dana,BlockPos b,Vec3 rel,float yaw,boolean still){lie(dana,b,rel,yaw,still?BorrascaStory.LAID_STILL:BorrascaStory.LAID);}
    static void lie(LiteraryActor dana,BlockPos b,Vec3 rel,float yaw,int g){
        BorrascaCast.stop(dana);dana.setNoGravity(true);dana.setDeltaMovement(Vec3.ZERO);
        if(dana.gesture()!=g)dana.gesture(g);
        var at=world(b,rel);if(dana.position().distanceToSqr(at)>1e-4||dana.getYRot()!=yaw){dana.moveTo(at.x,at.y,at.z,yaw,0);}
        face(dana,yaw);if(dana.isInvisible())dana.setInvisible(false);
    }

    /**
     * Into the slot, slowly. The belt draws her in at a crawl; at each bone it catches, the motor labours and she jerks against it, then
     * it gives with a crack and a jump, and what it broke rides out of the far end while she is still on the belt. Her body is cut where
     * the mouth is (torn, in thousandths of a block from her feet), the belt under her soaks back toward her head, and she screams until
     * it has her chest. Pulled now, the breaker stops it there (LATE).
     */
    private static void intake(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor dana,@Nullable LiteraryActor w0,boolean power,int t){
        double z=own.getDouble("FeedZ"),d=SLOT_Z-z;
        if(!power){own.putBoolean("DanaLate",true);own.putInt("DanaLateAt",(int)Math.round(d*1000));set(own,LATE);
            lie(dana,b,new Vec3(BELT_X,BELT_TOP,z),180,d<SILENT?BorrascaStory.TORN_LIVE:BorrascaStory.TORN_DEAD);BorrascaStory.save(p,own);return;}
        var rnd=p.getRandom();var l=p.serverLevel();
        var mouth=world(b,new Vec3(BELT_X,BELT_TOP+.3,SLOT_Z+.25));var at=BlockPos.containing(mouth);
        int stall=own.getInt("FeedStall");boolean straining=stall>0;
        if(straining){
            stall-=5;own.putInt("FeedStall",stall);
            LiteraryVignettes.heard(p,BorrascaRegistry.GRINDER.get(),at,.8F,.42F+rnd.nextFloat()*.08F);
            if(stall<=0){
                z-=.07;
                LiteraryVignettes.heard(p,net.minecraft.sounds.SoundEvents.BONE_BLOCK_BREAK,at,1.5F,.45F+rnd.nextFloat()*.2F);
                particles(l,p,new ItemParticleOption(ParticleTypes.ITEM,new ItemStack(Items.BONE)),mouth,8,.2,.15,.2,.22);
                particles(l,p,new DustParticleOptions(new Vector3f(.42F,.02F,.03F),2.4F),mouth,26,.4,.3,.3,.08);
                // What the knees, the hips and the shoulders broke comes out of the far end while she is still on the belt.
                int bone=own.getInt("FeedBones");if(bone==2||bone==3||bone==5)spawnOut(p,b,own,BorrascaShopBlock.Kind.REMAINS,0);
            }
        }else{
            z-=INTAKE_SPEED*5;
            int bone=own.getInt("FeedBones");
            if(bone<BONES.length&&SLOT_Z-z>=BONES[bone]){own.putInt("FeedBones",bone+1);own.putInt("FeedStall",30+rnd.nextInt(30));if(bone==0&&w0!=null)BorrascaStory.say(p,w0,STALL);}
        }
        double before=SLOT_Z-own.getDouble("FeedZ");own.putDouble("FeedZ",z);d=SLOT_Z-z;
        if(before<GENTLEMAN_AT&&d>=GENTLEMAN_AT&&sees(p,mouth))own.putBoolean("GentlemanAtMachine",true);
        boolean alive=d<SILENT;
        lie(dana,b,new Vec3(BELT_X,BELT_TOP,z),180+(rnd.nextFloat()-.5F)*(straining?12:5),alive?BorrascaStory.TORN_LIVE:BorrascaStory.TORN_DEAD);
        dana.torn((int)Math.round(d*1000));
        // Her voice: each new sound as the machine reaches it, said again while it goes on so it stays up; nothing after the last.
        int at2=-1;for(int i=0;i<SCREAM.length;i++)if(d>=SCREAM_AT[i])at2=i;
        int said=own.getInt("FeedScream");
        if(at2>=said){BorrascaStory.say(p,dana,SCREAM[at2]);own.putInt("FeedScream",at2+1);own.putInt("FeedSaidAt",t);}
        else if(alive&&at2>=0&&t-own.getInt("FeedSaidAt")>=80){BorrascaStory.say(p,dana,SCREAM[at2]);own.putInt("FeedSaidAt",t);}
        LiteraryVignettes.heard(p,BorrascaRegistry.MACHINE_WET.get(),at,1.3F,.55F+rnd.nextFloat()*.35F);
        particles(l,p,new DustParticleOptions(new Vector3f(.42F,.02F,.03F),1.9F),mouth,6+(int)(d*6),.35,.2,.25,.04);
        particles(l,p,new ItemParticleOption(ParticleTypes.ITEM,new ItemStack(Items.BEEF)),mouth,2+(int)(d*2),.2,.15,.2,.16);
        if(rnd.nextInt(3)==0)particles(l,p,new ItemParticleOption(ParticleTypes.ITEM,new ItemStack(Items.BONE)),mouth,2,.15,.1,.15,.14);
        // The belt under her soaks, back from the slot toward her head.
        smear(p,b,own,-186,d>.4?1:0);
        if(d>.6)smear(p,b,own,-185,d>1.0?1:0);
        if(d>1.1)smear(p,b,own,-184,0);
        if(d>=BODY){
            dana.setInvisible(true);var store=world(b,new Vec3(36,-4,-56));dana.moveTo(store.x,store.y,store.z);dana.setNoGravity(false);
            own.putBoolean("DanaFed",true);set(own,OUTFLOW);
            BorrascaStory.heard(p,BorrascaRegistry.CHIME.get(),BorrascaRooms.at(b,BorrascaRooms.MACHINE),1.2F);
            BorrascaStory.save(p,own);
        }
    }
    /** Whether the reader can see the slot's mouth from where they stand, as the client judges it for the flash. */
    static boolean sees(ServerPlayer p,Vec3 at){
        var eye=p.getEyePosition();if(eye.distanceToSqr(at)>48*48)return false;
        var ray=p.level().clip(new net.minecraft.world.level.ClipContext(eye,at,net.minecraft.world.level.ClipContext.Block.VISUAL,net.minecraft.world.level.ClipContext.Fluid.NONE,p));
        return ray.getType()==net.minecraft.world.phys.HitResult.Type.MISS||ray.getLocation().distanceToSqr(at)<2.25;
    }
    /** Stopped with part of her taken. Alive, she makes one more sound, and a little after goes still. */
    private static void late(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor dana,@Nullable LiteraryActor w0,int t){
        double z=own.getDouble("FeedZ");boolean alive=SLOT_Z-z<SILENT&&t<160;
        lie(dana,b,new Vec3(BELT_X,BELT_TOP,z),180,alive?BorrascaStory.TORN_LIVE:BorrascaStory.TORN_DEAD);
        if(t==10&&w0!=null)BorrascaStory.say(p,w0,BREAKER);
        if(t==60&&alive)BorrascaStory.say(p,dana,"aww... ooo");
    }
    /** After: the men walk back in at the side door, and are gone from the yard. */
    private static void leave(ServerPlayer p,BlockPos b,CompoundTag own,@Nullable LiteraryActor w0,@Nullable LiteraryActor w1,int t){
        if(own.getBoolean("FeedWorkersGone")||t<40)return;
        var door=world(b,new Vec3(-12.5,12,-178.5));boolean home=true;
        for(var w:new LiteraryActor[]{w0,w1}){if(w==null||w.isInvisible())continue;w.gesture(0);
            if(w.position().distanceToSqr(door.x,w.getY(),door.z)>.5&&t<600){home=false;if(!w.getPersistentData().contains("BorrascaSpeed"))BorrascaCast.walk(w,door,.3);}}
        if(home){own.putBoolean("FeedWorkersGone",true);for(var w:new LiteraryActor[]{w0,w1})if(w!=null){BorrascaCast.stop(w);w.setInvisible(true);}}
    }

    // ------------------------------------------------------------------ what comes out of the far end
    /** One piece riding the outflow belt: its display, what it lands as and where its block goes. */
    private static void spawnOut(ServerPlayer p,BlockPos b,CompoundTag own,BorrascaShopBlock.Kind kind,int stage){
        var d=display(p.serverLevel(),BorrascaShopBlock.of(kind,Direction.NORTH,stage),world(b,new Vec3(0,BELT_TOP+.005,OUT_START)),5);if(d==null)return;
        var list=own.getList("FeedOut",Tag.TAG_COMPOUND);var e=new CompoundTag();e.putUUID("Id",d.getUUID());e.putString("Kind",kind.name());e.putInt("Stage",stage);e.putDouble("Z",OUT_START);list.add(e);own.put("FeedOut",list);
    }
    private static void outflow(ServerPlayer p,BlockPos b,CompoundTag own,boolean power){
        int state=state(own);int t=own.getInt("FeedTicks");
        if(state==OUTFLOW){
            if(t==40)spawnOut(p,b,own,BorrascaShopBlock.Kind.REMAINS,1);
            if(t==75)spawnOut(p,b,own,BorrascaShopBlock.Kind.WRISTBAND,0);
        }
        if(!own.contains("FeedOut")||!power)return;
        var l=p.serverLevel();var list=own.getList("FeedOut",Tag.TAG_COMPOUND);var keep=new ListTag();
        for(int i=0;i<list.size();i++){
            var e=list.getCompound(i);double z=e.getDouble("Z")-OUT*5;
            var d=e.hasUUID("Id")?l.getEntity(e.getUUID("Id")):null;
            if(z<=OUT_END){
                if(d!=null)d.discard();
                var kind=BorrascaShopBlock.Kind.valueOf(e.getString("Kind"));
                var at=BorrascaRooms.at(b,kind==BorrascaShopBlock.Kind.WRISTBAND?PILE_BAND:e.getInt("Stage")==0?PILE_REMAINS:PILE_HAIR);
                if(l.getBlockState(at).isAir())l.setBlock(at,BorrascaShopBlock.of(kind,Direction.NORTH,e.getInt("Stage")),Block.UPDATE_CLIENTS);
                LiteraryVignettes.heard(p,BorrascaRegistry.MACHINE_WET.get(),at,.5F,1.1F);
                continue;
            }
            int cell=(int)Math.floor(z);if(cell!=(int)Math.floor(e.getDouble("Z"))&&cell>=-200)smear(p,b,own,cell,1);
            e.putDouble("Z",z);keep.add(e);
            if(d!=null){var at=world(b,new Vec3(0,BELT_TOP+.005,z));d.moveTo(at.x,at.y,at.z);}
        }
        // Everything is on the pile once the last of it (the band) has come out and landed.
        if(keep.isEmpty()){own.remove("FeedOut");if(state==OUTFLOW&&t>75)own.putBoolean("FeedPiled",true);}else own.put("FeedOut",keep);
    }
    /** The belt's rubber soaked at one cell, drawn over it by a display that stays. */
    private static void smear(ServerPlayer p,BlockPos b,CompoundTag own,int cell,int stage){
        String key="FeedSmear"+(-cell);if(own.getInt(key)>stage)return;
        if(own.hasUUID(key+"Id")){var old=p.serverLevel().getEntity(own.getUUID(key+"Id"));if(old!=null){if(own.getInt(key)==stage+1)return;old.discard();}}
        var d=display(p.serverLevel(),BorrascaShopBlock.of(BorrascaShopBlock.Kind.SMEAR,Direction.NORTH,stage),world(b,new Vec3(0,BELT_TOP+.002,cell)),0);
        if(d!=null){own.putUUID(key+"Id",d.getUUID());own.putInt(key,stage+1);}
    }
    static @Nullable Display.BlockDisplay display(ServerLevel l,net.minecraft.world.level.block.state.BlockState state,Vec3 at,int glide){
        var d=EntityType.BLOCK_DISPLAY.create(l);if(d==null)return null;var tag=new CompoundTag();d.saveWithoutId(tag);
        tag.put("block_state",NbtUtils.writeBlockState(state));tag.putInt("teleport_duration",glide);tag.putFloat("view_range",1.5F);d.load(tag);d.addTag("borrasca_feeding");
        d.moveTo(at.x,at.y,at.z,0,0);return l.addFreshEntity(d)?d:null;
    }
    /** Red dust off the top of the housing, drifting over the yard, for a while after. */
    private static void dust(ServerPlayer p,BlockPos b,CompoundTag own){
        int n=own.getInt("FeedDust");if(n>=900)return;own.putInt("FeedDust",n+5);if(n%10!=0)return;
        particles(p.serverLevel(),p,new DustParticleOptions(new Vector3f(.5F,.12F,.08F),2.2F),world(b,new Vec3(.5,17.3,-188.5)),5,.8,.4,.8,.02);
    }
    private static void particles(ServerLevel l,ServerPlayer p,ParticleOptions type,Vec3 at,int count,double dx,double dy,double dz,double speed){l.sendParticles(p,type,true,at.x,at.y,at.z,count,dx,dy,dz,speed);}

    // ------------------------------------------------------------------ hands
    /** The band on the pile, or on the belt where it stopped. Returns whether this was it. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        boolean band=rel.equals(PILE_BAND)&&BorrascaShopBlock.is(p.serverLevel().getBlockState(BorrascaRooms.at(b,rel)),BorrascaShopBlock.Kind.WRISTBAND);
        if(!band&&rel.getX()==0&&rel.getY()==12&&rel.getZ()<=-192&&rel.getZ()>=-200&&own.contains("FeedOut")){
            var list=own.getList("FeedOut",Tag.TAG_COMPOUND);
            for(int i=0;i<list.size();i++){var e=list.getCompound(i);if(e.getString("Kind").equals("WRISTBAND")&&Math.abs(e.getDouble("Z")+.5-(rel.getZ()+.5))<1.2)band=true;}
        }
        if(band){own.putBoolean("BandRead",true);BorrascaStory.cue(p,"A band off a wrist, cut through. The card on it is stamped 141-A.");BorrascaStory.save(p,own);return true;}
        if(rel.equals(PILE_REMAINS)||rel.equals(PILE_HAIR)){if(BorrascaShopBlock.is(p.serverLevel().getBlockState(BorrascaRooms.at(b,rel)),BorrascaShopBlock.Kind.REMAINS)){BorrascaStory.cue(p,rel.equals(PILE_HAIR)?"Hair, long, going grey, and a felt slipper.":"Pulp, with bone through it. It is still warm.");return true;}}
        return false;
    }
    /** Her, on the belt or the cot. She does not answer, take a hand, or look round. */
    public static void reached(ServerPlayer p,CompoundTag own){
        int s=state(own);if(s==SAVED)own.putBoolean("DanaIgnored",true);
    }
}
