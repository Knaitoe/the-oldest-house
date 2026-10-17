package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.ShallowsCuePayload;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Verb: throwing. The memory is personal; crossing its threshold alone never completes it.
 *
 * <p>0.4.101: the reader takes her wrists while a boy holds her feet, and the two swing her out over the water with the
 * reader's own strafe keys, on a count of three. The lake does not take her. She breaks on its surface, lies still, gets up
 * wrong and runs across the water into the woods on all fours. The girl and the boy at her feet are this reader's alone; the
 * three boys on the bank are shared, and speak to each reader privately.
 */
public final class Shallows {
    public static final String ID="shallows", BOY="HouseShallowsBoy", WORDS="HouseLakeWords", CARRIER="HouseShallowsCarrier";
    private static final String OWNER="ShallowsOwner", ACTOR="ShallowsActor";
    /** The recollected girl's phases. */
    public static final int LYING=0, CARRIED=1, FLYING=2, BROKEN=3, RISING=4, SCURRY=5, GONE=6;
    /** The carrier's hold on her feet. */
    public static final int HOLD=1, SWINGING=2, RELEASED=3;
    /** Reactions shown with a line, or alone, by the reader's client. */
    public static final int CALM=0, JEER=1, CHEER=2, FROZEN=3, RECOIL=4, WHISPER=5;
    /** How long the broken body lies still on the water, gets up, and may take to reach the woods. */
    public static final int STILL_TICKS=70, RISE_TICKS=44, SCURRY_TICKS=160, GONE_TICKS=170;
    /** Swing radius (blocks) and the body centre's height over the bank while she hangs between them. */
    public static final double SWING_RADIUS=.75, HANG=.95, SCURRY_SPEED=.3;
    /** Ticks of one stroke; a stroke may be followed once it is this far through. */
    public static final int STROKE_TICKS=12;
    private static final float READY=.65F, RELEASE=.6F;
    /** The swing's peak on each forward stroke (one, two, three) and on each back stroke. */
    private static final float[] FORWARD={.62F,.92F,1.15F}, BACK={.5F,.82F};
    /** Carry slots along the bank, so two readers in the same memory never stand in one place. */
    private static final int[] SLOTS={0,-7,7};
    private static final double LINE_Z=-11.5;
    /** The reader faces the boy across her, east; the lake is on the reader's left. */
    public static final float READER_YAW=-90, CARRIER_YAW=90;
    /** The three boys on the bank, in their saved order, then the boy at her feet and the girl. */
    public static final int BUD=0, HANK=1, CLAY=2, LONNIE=3, STACEY=4;
    private static final String[] WHO={"Bud","Hank","Clay","Lonnie","Stacey"};
    private static final int[] LOOK={0,2,3};
    private record Line(int who,String text,int mood){}
    private record Due(long at,Line line){}
    private static final Line[] OPENING={new Line(LONNIE,"Took you long enough. Grab her arms.",CALM),new Line(BUD,"Careful. She'll put a curse on you.",JEER),
            new Line(STACEY,"I'm not anything. Let me up.",CALM),new Line(HANK,"Witches float. We're only going to see.",JEER)};
    private static final Line[] NUDGES={new Line(LONNIE,"Her arms. Come on, both hands.",CALM),new Line(CLAY,"Everybody's watching.",CALM),
            new Line(BUD,"Ask her where she goes at night.",JEER),new Line(STACEY,"Please let go of my feet.",CALM)};

    /** Transient carry state per recollection: the stroke under way, the count, queued lines and the flight. */
    private static final class Scene {
        int slot,stroke,queued,forward,strokeAge=STROKE_TICKS,strokeLength=STROKE_TICKS,idle,line,wrong;
        float from,to,angle;long next;boolean released;Vec3 velocity=Vec3.ZERO;
        final PriorityQueue<Due> due=new PriorityQueue<>(Comparator.comparingLong(Due::at));
        void later(long at,Line line){due.add(new Due(at,line));}
    }
    private static final Map<UUID,Scene> SCENES=new HashMap<>();
    private Shallows() {}

    // ------------------------------------------------------------------------------------------------ the bank
    public static void build(MinecraftServer server,ServerLevel level,BlockPos base){
        IndianLakeArchitecture.shallows(level,base);LabyrinthData data=LabyrinthData.get(server);CompoundTag state=data.state(ID);
        if(!state.getBoolean("Built"))state.putBoolean("FreshCast0493",true);
        state.putBoolean("Built",true);data.setState(ID,state);boys(level,base);
    }
    private static void boys(ServerLevel level,BlockPos base){
        boolean fresh=LabyrinthData.get(level.getServer()).state(ID).getBoolean("FreshCast0493");
        for(int i=0;i<3;i++){
            String tag=BOY+i;int index=i;
            var boy=LakeOriginals.ensure(level,base,LabyrinthPlace.SHALLOWS,ID,"boy"+i,tag,DrownedTownRegistry.CONGREGANT.get(),fresh,b->{
                b.pose(false,false);b.memoryBoy(true);b.addTag(BOY);b.addTag(tag);
                b.moveTo(base.getX()+new int[]{-4,5,-3}[index]+.5,base.getY(),base.getZ()+new int[]{-10,-11,-5}[index]+.5,180,0);
            });
            // Each original keeps its identity; 0.4.101 only gives the three of them their own faces.
            if(boy!=null)boy.boyLook(LOOK[i]);
        }
    }
    public static @Nullable LakeCongregantEntity onlooker(ServerLevel level,int index){
        var record=LabyrinthData.get(level.getServer()).state(ID).getCompound("NativeOriginals0493").getCompound("boy"+index);
        return record.hasUUID("Id")&&level.getEntity(record.getUUID("Id")) instanceof LakeCongregantEntity boy?boy:null;
    }
    /** Her place on the bank in a carry slot; the reader stands west of her and the boy east, both facing her. */
    public static Vec3 spot(BlockPos base,int slot){return new Vec3(base.getX()+SLOTS[slot]+.5,base.getY(),base.getZ()+LINE_Z);}
    public static Vec3 stance(BlockPos base,int slot){return spot(base,slot).add(-1.5,0,0);}
    public static Vec3 carrierStance(BlockPos base,int slot){return spot(base,slot).add(1.5,0,0);}
    /** Where her body is while she swings: positive angles toward the water, which lies north of the bank. */
    public static Vec3 swung(BlockPos base,int slot,float angle){
        return spot(base,slot).add(0,HANG+SWING_RADIUS*(1-Math.cos(angle)),-SWING_RADIUS*Math.sin(angle));
    }
    private static boolean clearSlot(ServerLevel level,BlockPos base,int slot){
        for(int x=SLOTS[slot]-2;x<=SLOTS[slot]+2;x++){
            var feet=base.offset(x,0,(int)Math.floor(LINE_Z));
            for(var cell:new BlockPos[]{feet,feet.above()})
                if(!level.getBlockState(cell).getCollisionShape(level,cell).isEmpty()||!level.getFluidState(cell).isEmpty())return false;
            if(level.getBlockState(feet.below()).getCollisionShape(level,feet.below()).isEmpty())return false;
        }
        return true;
    }
    private static int chooseSlot(ServerPlayer player,BlockPos base,CompoundTag record){
        Set<Integer> taken=new HashSet<>();var players=LabyrinthData.get(player.server).state(ID).getCompound("Players");
        for(String key:players.getAllKeys())if(!key.equals(player.getUUID().toString())&&players.getCompound(key).getBoolean("Active"))taken.add(players.getCompound(key).getInt("Slot"));
        if(record.contains("Slot")&&!taken.contains(record.getInt("Slot"))&&record.getInt("Slot")<SLOTS.length)return record.getInt("Slot");
        for(int i=0;i<SLOTS.length;i++)if(!taken.contains(i)&&clearSlot(player.serverLevel(),base,i))return i;
        for(int i=0;i<SLOTS.length;i++)if(!taken.contains(i))return i;
        return 0;
    }

    // ------------------------------------------------------------------------------------------------ records
    public static CompoundTag personal(LabyrinthData data,UUID owner){return data.state(ID).getCompound("Players").getCompound(owner.toString()).copy();}
    private static void save(LabyrinthData data,UUID owner,CompoundTag record){
        CompoundTag state=data.state(ID),players=state.getCompound("Players");players.put(owner.toString(),record);state.put("Players",players);data.setState(ID,state);
    }
    public static @Nullable LakeWitchEntity actor(ServerPlayer player){
        CompoundTag record=personal(LabyrinthData.get(player.server),player.getUUID());
        if(!record.hasUUID("Actor"))return null;
        ServerLevel level=player.server.getLevel(HouseDimensions.OUTSIDE);if(level==null)return null;
        Entity actor=level.getEntity(record.getUUID("Actor"));
        return actor instanceof LakeWitchEntity witch&&player.getUUID().equals(witch.memoryOwner())?witch:null;
    }
    /** The boy who holds her feet, for this reader only. */
    public static @Nullable LakeCongregantEntity carrier(ServerPlayer player){
        CompoundTag record=personal(LabyrinthData.get(player.server),player.getUUID());
        if(!record.hasUUID("Carrier"))return null;
        ServerLevel level=player.server.getLevel(HouseDimensions.OUTSIDE);if(level==null)return null;
        return level.getEntity(record.getUUID("Carrier")) instanceof LakeCongregantEntity boy&&player.getUUID().equals(boy.carrierOwner())?boy:null;
    }
    private static @Nullable ServerPlayer owner(ServerLevel level,UUID id){
        ServerPlayer owner=level.getServer().getPlayerList().getPlayer(id);
        // Native fake players used by server tests live in the level rather than the connection list.
        return owner!=null?owner:level.players().stream().filter(p->p.getUUID().equals(id)).findFirst().orElse(null);
    }
    private static Scene scene(LakeWitchEntity girl){
        return SCENES.computeIfAbsent(girl.getUUID(),id->{
            var s=new Scene();var owner=girl.memoryOwner();
            if(owner!=null&&girl.level() instanceof ServerLevel level)s.slot=Math.max(0,Math.min(SLOTS.length-1,personal(LabyrinthData.get(level.getServer()),owner).getInt("Slot")));
            return s;
        });
    }
    private static void discard(LakeWitchEntity girl){SCENES.remove(girl.getUUID());girl.discard();}

    // ------------------------------------------------------------------------------------------------ arrival
    public static void onArrive(ServerPlayer player,LabyrinthPlace place){
        if(place!=LabyrinthPlace.SHALLOWS)return;LabyrinthData data=LabyrinthData.get(player.server);
        if(!IndianLakeProgress.canDealShallows(data,player.getUUID()))return;
        BlockPos base=IndianLakeRooms.base(player.server,place);if(base==null)return;
        IndianLakeRooms.keepLoaded(player.serverLevel(),base,place);boys(player.serverLevel(),base);
        CompoundTag record=personal(data,player.getUUID());
        // Reconnects reuse their saved actors, loaded or not; a new visit cannot duplicate a recollection.
        boolean resumed=record.getBoolean("Active")&&record.hasUUID("Actor");
        if(resumed&&record.hasUUID("Carrier"))return;
        record.putBoolean("Cold",false);record.putBoolean("Active",true);
        int slot=resumed&&record.contains("Slot")?record.getInt("Slot"):chooseSlot(player,base,record);record.putInt("Slot",slot);
        LakeWitchEntity girl=actor(player);
        if(girl==null&&!resumed){
            girl=DrownedTownRegistry.LAKE_WITCH.get().create(player.serverLevel());if(girl==null)return;
            girl.recollection(player.getUUID(),base);var at=spot(base,slot);girl.moveTo(at.x,at.y,at.z,READER_YAW,0);
            if(!player.serverLevel().addFreshEntity(girl))return;record.putUUID("Actor",girl.getUUID());
        }
        // A visit saved before 0.4.101 had nobody at her feet; he joins it now.
        if(!record.hasUUID("Carrier")){
            var boy=DrownedTownRegistry.CONGREGANT.get().create(player.serverLevel());if(boy==null)return;
            boy.pose(false,false);boy.memoryBoy(true);boy.boyLook(1);boy.carrierOwner(player.getUUID());boy.addTag(CARRIER);boy.carry(HOLD);
            var at=carrierStance(base,slot);boy.moveTo(at.x,at.y,at.z,CARRIER_YAW,0);
            if(!player.serverLevel().addFreshEntity(boy))return;record.putUUID("Carrier",boy.getUUID());
        }
        save(data,player.getUUID(),record);
        IndianLakeArchitecture.light(player.serverLevel(),base.offset(2,3,-8),8);IndianLakeArchitecture.light(player.serverLevel(),base.offset(0,2,-13),6);
    }

    // ------------------------------------------------------------------------------------------------ the carry
    public static boolean lift(ServerPlayer player,LakeWitchEntity girl){
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag record=personal(data,player.getUUID());
        if(!IndianLakeProgress.canDealShallows(data,player.getUUID())||!IndianLakeRooms.inside(player,LabyrinthPlace.SHALLOWS)
                ||!player.getUUID().equals(girl.memoryOwner())||player.distanceToSqr(girl)>12||girl.memoryPhase()!=LYING
                ||record.getBoolean("Cold")||!record.getBoolean("Active"))return false;
        if(!player.getMainHandItem().isEmpty()||!player.getOffhandItem().isEmpty()){
            say(player,LONNIE,"Both hands. Put what you're holding away.",CALM,girl);return false;
        }
        LakeCongregantEntity boy=carrier(player);BlockPos base=girl.memoryBase();if(boy==null||base==null)return false;
        Scene s=scene(girl);s.slot=Math.max(0,Math.min(SLOTS.length-1,record.getInt("Slot")));
        ItemStack burden=new ItemStack(DrownedTownRegistry.SHALLOWS_BURDEN.get());
        CustomData.update(DataComponents.CUSTOM_DATA,burden,t->{t.putUUID(OWNER,player.getUUID());t.putUUID(ACTOR,girl.getUUID());});
        player.setItemInHand(InteractionHand.MAIN_HAND,burden);player.inventoryMenu.broadcastChanges();
        // The reader takes her wrists at the west end of her; the boy already has her feet.
        var at=stance(base,s.slot);player.teleportTo(player.serverLevel(),at.x,at.y,at.z,READER_YAW,12);
        // Server-side players without a real connection take the place directly.
        if(player.position().distanceToSqr(at)>1e-4||Math.abs(player.getYRot()-READER_YAW)>.5F)player.moveTo(at.x,at.y,at.z,READER_YAW,12);
        girl.memoryPhase(CARRIED);s.stroke=0;s.queued=0;s.forward=0;s.angle=s.from=s.to=0;s.idle=0;s.released=false;s.strokeAge=s.strokeLength=STROKE_TICKS;
        boy.carry(SWINGING);boy.swing(0);
        long now=player.serverLevel().getGameTime();s.due.clear();
        say(player,LONNIE,"Got her? We swing her on three.",CALM,girl);
        s.later(now+25,new Line(CLAY,"Out past the reeds!",CHEER));s.later(now+50,new Line(STACEY,"No. No, no...",CALM));
        hint(player);return true;
    }
    private static void hint(ServerPlayer player){
        player.displayClientMessage(Component.empty().append(Component.keybind("key.left")).append(" swing toward the water  ·  ")
                .append(Component.keybind("key.right")).append(" swing back  ·  ").append(Component.keybind("key.sneak")).append(" let go").withStyle(ChatFormatting.GRAY),true);
    }
    /** The reader's strafe keys: the lake is on their left, so left swings her out and right brings her back. */
    public static boolean swing(ServerPlayer player,int side){
        LakeWitchEntity girl=actor(player);if(girl==null||girl.memoryPhase()!=CARRIED||!hasBurden(player)||side==0)return false;
        return stroke(player,girl,side<0?1:-1);
    }
    private static boolean stroke(ServerPlayer player,LakeWitchEntity girl,int direction){
        Scene s=scene(girl);if(s.released)return false;
        boolean ready=s.stroke==0||s.strokeAge>=s.strokeLength*READY;
        if(direction==s.stroke){
            if(--s.wrong<0){say(player,LONNIE,"Other way. With me.",CALM,girl);s.wrong=4;}
            return false;
        }
        if(!ready){s.queued=direction;return false;}
        s.queued=0;s.from=s.angle;s.stroke=direction;s.strokeAge=0;s.idle=0;
        long now=player.serverLevel().getGameTime();
        if(direction>0){
            int count=++s.forward;s.to=FORWARD[Math.min(2,count-1)];s.strokeLength=count==1?STROKE_TICKS+2:STROKE_TICKS;
            say(player,LONNIE,count==1?"One!":count==2?"Two!":"Three!",CALM,girl);
            if(count==1)s.later(now+8,new Line(BUD,"Fly, witch!",JEER));
            if(count==2)s.later(now+8,new Line(HANK,"Say a spell, Stacey!",JEER));
        }else{
            s.to=-BACK[Math.min(1,Math.max(0,s.forward-1))];s.strokeLength=STROKE_TICKS;
            if(s.forward==1)s.later(now+2,new Line(STACEY,"Don't...",CALM));
            if(s.forward==2)s.later(now+3,new Line(CLAY,"Higher!",CHEER));
        }
        hear(player,DrownedTownRegistry.SHALLOWS_SWING.get(),girl.position(),.55F+.15F*Math.min(3,s.forward),.9F+player.getRandom().nextFloat()*.15F);
        return true;
    }
    private static void carry(ServerPlayer owner,LakeWitchEntity girl,Scene s,BlockPos base,@Nullable LakeCongregantEntity boy){
        if(!hasBurden(owner)){drop(owner,girl,"You let go of her wrists.");return;}
        var stand=stance(base,s.slot);
        if(owner.isShiftKeyDown()){drop(owner,girl,null);return;}
        if(owner.position().subtract(stand).horizontalDistanceSqr()>.8*.8){drop(owner,girl,"She slips out of your hands.");return;}
        if(s.queued!=0&&(s.stroke==0||s.strokeAge>=s.strokeLength*READY)){int q=s.queued;s.queued=0;stroke(owner,girl,q);}
        if(s.stroke!=0&&s.strokeAge<s.strokeLength){
            s.strokeAge++;float k=(1-Mth.cos(Mth.PI*s.strokeAge/s.strokeLength))/2;s.angle=s.from+(s.to-s.from)*k;
            // On three both boys let go in the middle of the stroke, while she is still rising toward the water.
            if(s.stroke>0&&s.forward>=3&&s.strokeAge>=s.strokeLength*RELEASE){release(owner,girl,s,base,boy);return;}
        }else if(++s.idle>50&&(s.forward>0||Math.abs(s.angle)>.01F)){
            if(s.idle==51){s.forward=0;s.stroke=0;say(owner,LONNIE,"Don't stop. Start again.",CALM,girl);}
            s.angle*=.88F;if(Math.abs(s.angle)<.01F)s.angle=0;
        }
        var body=swung(base,s.slot,s.angle);girl.moveTo(body.x,body.y,body.z,READER_YAW,0);girl.setYHeadRot(READER_YAW);girl.setYBodyRot(READER_YAW);girl.setDeltaMovement(Vec3.ZERO);
        girl.swing(s.angle);if(boy!=null){boy.carry(SWINGING);boy.swing(s.angle);}
        if(girl.memoryTicks%40==0)hint(owner);
    }
    /** Both let go: she leaves the swing with its own motion and the heave behind it. */
    private static void release(ServerPlayer owner,LakeWitchEntity girl,Scene s,BlockPos base,@Nullable LakeCongregantEntity boy){
        s.released=true;removeBurdens(owner);
        float rate=(s.to-s.from)*Mth.PI/(2*s.strokeLength)*Mth.sin(Mth.PI*s.strokeAge/s.strokeLength);
        double tangent=SWING_RADIUS*rate;
        s.velocity=new Vec3(0,Math.sin(s.angle)*tangent+.24,-Math.cos(s.angle)*tangent-.34);
        girl.memoryPhase(FLYING);girl.swing(0);
        if(boy!=null){boy.carry(RELEASED);boy.swing(s.angle);}
        hear(owner,DrownedTownRegistry.SHALLOWS_SWING.get(),girl.position(),1.1F,.7F);
        for(int who:new int[]{BUD,HANK,CLAY})react(owner,who,CHEER,30,girl);
        say(owner,BUD,"There she goes!",CHEER,girl);
    }
    /** She falls as anything thrown falls, and stops at the first thing under her: the lake's surface, not its water. */
    private static void fly(ServerPlayer owner,LakeWitchEntity girl,Scene s,BlockPos base){
        var level=(ServerLevel)girl.level();Vec3 at=girl.position(),next=at.add(s.velocity);
        s.velocity=s.velocity.add(0,-.08,0).scale(.98);
        double floor=floor(level,base,next);
        if(next.y<=floor||girl.memoryTicks>80){
            double k=at.y-next.y>1e-6?Mth.clamp((at.y-floor)/(at.y-next.y),0,1):1;var hit=at.lerp(next,k);
            float yaw=(float)(Mth.atan2(-s.velocity.x,s.velocity.z)*Mth.RAD_TO_DEG)+35;
            girl.moveTo(hit.x,floor,hit.z,yaw,0);girl.setYHeadRot(yaw);girl.setYBodyRot(yaw);impact(owner,girl);return;
        }
        float yaw=girl.getYRot();girl.moveTo(next.x,next.y,next.z,yaw,0);girl.setDeltaMovement(Vec3.ZERO);
    }
    /** The water's surface where there is water under her, otherwise the top of the ground. */
    static double floor(ServerLevel level,BlockPos base,Vec3 at){
        for(int y=Mth.floor(at.y)+1;y>=base.getY()-4;y--){
            var cell=BlockPos.containing(at.x,y,at.z);
            if(level.getFluidState(cell).is(FluidTags.WATER))return y+level.getFluidState(cell).getHeight(level,cell);
            var shape=level.getBlockState(cell).getCollisionShape(level,cell);
            if(!shape.isEmpty())return y+shape.max(Direction.Axis.Y);
        }
        return base.getY()-1;
    }
    private static void impact(ServerPlayer owner,LakeWitchEntity girl){
        var level=(ServerLevel)girl.level();girl.memoryPhase(BROKEN);
        hear(owner,DrownedTownRegistry.SHALLOWS_BREAK.get(),girl.position(),1.4F,1);
        level.sendParticles(owner,ParticleTypes.SPLASH,true,girl.getX(),girl.getY()+.1,girl.getZ(),36,.7,.02,.7,.1);
        level.sendParticles(owner,ParticleTypes.FALLING_WATER,true,girl.getX(),girl.getY()+.3,girl.getZ(),12,.5,.25,.5,0);
        IndianLakeProgress.completedShallows(LabyrinthData.get(level.getServer()),owner.getUUID(),owner.getGameProfile().getName());
        WitnessAccount.resolve(owner,WitnessAccount.Story.SHALLOWS,"threw_her");
        updateEssays(owner);
        for(int who:new int[]{BUD,HANK,CLAY,LONNIE})react(owner,who,FROZEN,150,girl);
    }
    /** Put her back down on the bank, the boy still holding her feet; the count starts again. */
    private static void drop(ServerPlayer owner,LakeWitchEntity girl,@Nullable String why){
        removeBurdens(owner);BlockPos base=girl.memoryBase();if(base==null)return;Scene s=scene(girl);
        lay(girl,base,s.slot);var boy=carrier(owner);if(boy!=null){boy.carry(HOLD);boy.swing(0);}
        s.stroke=0;s.queued=0;s.forward=0;s.angle=0;s.released=false;s.line=OPENING.length;s.next=owner.serverLevel().getGameTime()+300;
        hear(owner,SoundEvents.PLAYER_SMALL_FALL,girl.position(),.8F,.8F);
        if(why!=null)owner.displayClientMessage(Component.literal(why).withStyle(ChatFormatting.GRAY),true);
        say(owner,LONNIE,"What are you doing? Pick her up.",CALM,girl);s.later(owner.serverLevel().getGameTime()+20,new Line(BUD,"Chicken.",JEER));
    }
    private static void lay(LakeWitchEntity girl,BlockPos base,int slot){
        girl.memoryPhase(LYING);var at=spot(base,slot);girl.moveTo(at.x,at.y,at.z,READER_YAW,0);girl.setYHeadRot(READER_YAW);girl.setYBodyRot(READER_YAW);
        girl.setDeltaMovement(Vec3.ZERO);girl.swing(0);
    }

    // ------------------------------------------------------------------------------------------------ the burden
    public static boolean validBurden(ServerPlayer player,ItemStack stack){
        if(!stack.is(DrownedTownRegistry.SHALLOWS_BURDEN.get()))return false;
        CustomData custom=stack.get(DataComponents.CUSTOM_DATA);if(custom==null)return false;CompoundTag t=custom.copyTag();
        LakeWitchEntity girl=actor(player);
        return t.hasUUID(OWNER)&&player.getUUID().equals(t.getUUID(OWNER))&&t.hasUUID(ACTOR)&&girl!=null
                &&girl.getUUID().equals(t.getUUID(ACTOR))&&girl.memoryPhase()==CARRIED
                &&IndianLakeRooms.inside(player,LabyrinthPlace.SHALLOWS)
                &&!personal(LabyrinthData.get(player.server),player.getUUID()).getBoolean("Cold");
    }
    private static boolean hasBurden(ServerPlayer player){
        for(int i=0;i<player.getInventory().getContainerSize();i++)if(validBurden(player,player.getInventory().getItem(i)))return true;
        return false;
    }
    public static void removeBurdens(ServerPlayer player){
        for(int i=0;i<player.getInventory().getContainerSize();i++)if(player.getInventory().getItem(i).is(DrownedTownRegistry.SHALLOWS_BURDEN.get()))player.getInventory().setItem(i,ItemStack.EMPTY);
        if(player.containerMenu.getCarried().is(DrownedTownRegistry.SHALLOWS_BURDEN.get()))player.containerMenu.setCarried(ItemStack.EMPTY);
        player.inventoryMenu.broadcastChanges();
    }

    // ------------------------------------------------------------------------------------------------ ticking
    /** Called from the actor's own tick: the carry, the flight and everything after. */
    public static void tickActor(LakeWitchEntity girl){
        if(!(girl.level() instanceof ServerLevel level)||girl.memoryOwner()==null||girl.memoryBase()==null)return;
        LabyrinthData data=LabyrinthData.get(level.getServer());girl.memoryTicks++;
        ServerPlayer owner=owner(level,girl.memoryOwner());
        if(IndianLakeProgress.hasThrown(data,girl.memoryOwner())){
            // A finished body plays out even if its owner disconnects during the aftermath; nothing earlier survives.
            if(girl.memoryPhase()<BROKEN){discard(girl);return;}
            aftermath(level,girl,owner!=null&&IndianLakeRooms.inside(owner,LabyrinthPlace.SHALLOWS)?owner:null);return;
        }
        CompoundTag live=personal(data,girl.memoryOwner());
        if(!live.getBoolean("Active")||!live.hasUUID("Actor")||!girl.getUUID().equals(live.getUUID("Actor"))){discard(girl);return;}
        BlockPos base=girl.memoryBase();Scene s=scene(girl);
        LakeCongregantEntity boy=owner==null?null:carrier(owner);
        if(owner==null||!IndianLakeRooms.inside(owner,LabyrinthPlace.SHALLOWS)){
            // Interrupted recollections are retryable, and never count a throw after their owner leaves.
            if(girl.memoryPhase()!=LYING||s.released){s.released=false;s.stroke=0;s.forward=0;s.angle=0;}
            lay(girl,base,s.slot);return;
        }
        long now=level.getGameTime();
        while(!s.due.isEmpty()&&s.due.peek().at()<=now){var line=s.due.poll().line();say(owner,line.who(),line.text(),line.mood(),girl);}
        switch(girl.memoryPhase()){
            case CARRIED->carry(owner,girl,s,base,boy);
            case FLYING->fly(owner,girl,s,base);
            case LYING->{
                lay(girl,base,s.slot);if(boy!=null){boy.carry(HOLD);boy.swing(0);}
                if(s.next==0)s.next=now+10;
                if(now>=s.next){
                    var line=s.line<OPENING.length?OPENING[s.line]:NUDGES[(s.line-OPENING.length)%NUDGES.length];
                    say(owner,line.who(),line.text(),line.mood(),girl);s.line++;s.next=now+(s.line<OPENING.length?55:s.line==OPENING.length?160:300);
                }
            }
            // The landing records the throw, so a later phase here can only be a reload between the two.
            default->aftermath(level,girl,owner);
        }
    }
    /** The broken body, the getting up and the crossing; then the boys on the bank. Driven by saved phase time. */
    private static void aftermath(ServerLevel level,LakeWitchEntity girl,@Nullable ServerPlayer owner){
        int t=girl.memoryTicks;BlockPos base=girl.memoryBase();if(base==null)return;
        girl.setDeltaMovement(Vec3.ZERO);
        if(owner!=null){var boy=carrier(owner);if(boy!=null){
            if(boy.carry()==RELEASED&&(girl.memoryPhase()>BROKEN||t>16))boy.carry(0);
            float yaw=(float)(Mth.atan2(-(girl.getX()-boy.getX()),girl.getZ()-boy.getZ())*Mth.RAD_TO_DEG);boy.setYRot(yaw);boy.setYHeadRot(yaw);boy.setYBodyRot(yaw);
        }}
        switch(girl.memoryPhase()){
            case BROKEN->{
                if(owner!=null){
                    if(t==36)say(owner,CLAY,"She didn't go under.",WHISPER,girl);
                    if(t==60)say(owner,HANK,"Stacey?",FROZEN,girl);
                }
                if(t>=STILL_TICKS)girl.memoryPhase(RISING);
            }
            case RISING->{
                if(owner!=null){
                    if(t==2||t==9||t==17||t==26||t==35)hear(owner,DrownedTownRegistry.SHALLOWS_RISE.get(),girl.position(),1,.85F+.06F*(t%4));
                    if(t==18)for(int who:new int[]{HANK,CLAY,LONNIE})react(owner,who,RECOIL,120,girl);
                    if(t==22)say(owner,BUD,"Her arm's on backwards.",RECOIL,girl);
                }
                if(t>=RISE_TICKS)girl.memoryPhase(SCURRY);
            }
            case SCURRY->{
                // Straight for the far woods, weaving, over the water as if it were ground and then up the bank.
                double x=girl.getX()+.09*Math.sin(t*.42),z=girl.getZ()-SCURRY_SPEED;
                double y=floor(level,base,new Vec3(x,girl.getY()+2,z));
                float yaw=(float)(Mth.atan2(-(x-girl.getX()),z-girl.getZ())*Mth.RAD_TO_DEG);
                girl.moveTo(x,y,z,yaw,0);girl.setYHeadRot(yaw);girl.setYBodyRot(yaw);
                if(owner!=null){
                    if(t%4==1)hear(owner,DrownedTownRegistry.SHALLOWS_SCURRY.get(),girl.position(),.9F,.9F+owner.getRandom().nextFloat()*.2F);
                    if(t==1)say(owner,HANK,"Jesus.",RECOIL,girl);
                    if(t==24)say(owner,CLAY,"She's running on it.",RECOIL,girl);
                }
                if(z<=base.getZ()-37.5||t>=SCURRY_TICKS)girl.memoryPhase(GONE);
            }
            case GONE->{
                if(owner!=null){
                    if(t==30)say(owner,BUD,"Nobody saw that.",WHISPER,girl);
                    if(t==85)say(owner,HANK,"Witches float. She floated, that's all.",CALM,girl);
                    if(t==145)say(owner,LONNIE,"That was all of us.",CALM,girl);
                }
                if(t>=GONE_TICKS)discard(girl);
            }
            default->{}
        }
    }
    /** The boy at her feet leaves with his reader's visit; a stale one from an old visit cannot return. */
    public static void tickCarrier(LakeCongregantEntity boy){
        if(!(boy.level() instanceof ServerLevel level)||boy.carrierOwner()==null)return;
        CompoundTag record=personal(LabyrinthData.get(level.getServer()),boy.carrierOwner());
        if(!record.getBoolean("Active")||!record.hasUUID("Carrier")||!boy.getUUID().equals(record.getUUID("Carrier")))boy.discard();
    }

    // ------------------------------------------------------------------------------------------------ voices
    private static @Nullable Entity speaker(ServerPlayer player,int who,@Nullable LakeWitchEntity girl){
        return who==STACEY?girl:who==LONNIE?carrier(player):onlooker(player.serverLevel(),who);
    }
    /** A line said to this reader alone, over the one who says it. */
    static void say(ServerPlayer player,int who,String line,int mood,@Nullable LakeWitchEntity girl){
        Entity speaker=speaker(player,who,girl);
        player.displayClientMessage(Component.literal(WHO[who]+": “"+line+"”"),false);
        HousePackets.send(player,new ShallowsCuePayload(speaker==null?-1:speaker.getId(),line,mood,Math.max(50,line.length()*3+30)));
    }
    /** A reaction without words, seen by this reader alone. */
    static void react(ServerPlayer player,int who,int mood,int ticks,@Nullable LakeWitchEntity girl){
        Entity speaker=speaker(player,who,girl);if(speaker!=null)HousePackets.send(player,new ShallowsCuePayload(speaker.getId(),"",mood,ticks));
    }
    /** A sound only this reader hears. */
    static void hear(ServerPlayer player,SoundEvent sound,Vec3 at,float volume,float pitch){
        player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),SoundSource.NEUTRAL,at.x,at.y,at.z,volume,pitch,player.getRandom().nextLong()));
    }
    private static void updateEssays(ServerPlayer player){
        for(int i=0;i<player.getInventory().getContainerSize();i++){
            ItemStack stack=player.getInventory().getItem(i);if(stack.getItem() instanceof DrownedEssayItem essay)essay.refresh(player,stack);
        }
        player.inventoryMenu.broadcastChanges();
    }

    // ------------------------------------------------------------------------------------------------ leaving, refusing
    public static void onDepart(ServerPlayer player){
        LakeWitchEntity girl=actor(player);if(girl!=null)discard(girl);LakeCongregantEntity boy=carrier(player);if(boy!=null)boy.discard();removeBurdens(player);
        LabyrinthData data=LabyrinthData.get(player.server);CompoundTag record=personal(data,player.getUUID());record.putBoolean("Active",false);record.remove("Actor");record.remove("Carrier");save(data,player.getUUID(),record);
    }
    public static void onToss(ItemTossEvent event){
        if(!(event.getPlayer() instanceof ServerPlayer player)||!event.getEntity().getItem().is(DrownedTownRegistry.SHALLOWS_BURDEN.get()))return;
        event.setCanceled(true);event.getEntity().discard();LakeWitchEntity girl=actor(player);
        if(girl!=null&&girl.memoryPhase()==CARRIED)drop(player,girl,null);
        removeBurdens(player);
    }
    public static void onAttack(AttackEntityEvent event){
        if(!(event.getEntity() instanceof ServerPlayer player)||!IndianLakeRooms.inside(player,LabyrinthPlace.SHALLOWS))return;
        var target=event.getTarget();
        if(!(target instanceof LakeWitchEntity w&&w.memory())&&!target.getTags().contains(BOY)&&!target.getTags().contains(CARRIER))return;
        event.setCanceled(true);LabyrinthData data=LabyrinthData.get(player.server);CompoundTag record=personal(data,player.getUUID());
        // A finished throw is not undone by a blow afterward.
        if(IndianLakeProgress.hasThrown(data,player.getUUID()))return;
        record.putBoolean("Cold",true);save(data,player.getUUID(),record);
        LakeWitchEntity girl=actor(player);if(girl!=null)discard(girl);LakeCongregantEntity boy=carrier(player);if(boy!=null)boy.discard();removeBurdens(player);
        BlockPos base=IndianLakeRooms.base(player.server,LabyrinthPlace.SHALLOWS);IndianLakeArchitecture.light(player.serverLevel(),base.offset(2,3,-8),0);IndianLakeArchitecture.light(player.serverLevel(),base.offset(0,2,-13),0);
    }
    /** Old saved words on the bank (before 0.4.101 the scene spoke through floating text) still expire. */
    public static void words(ServerPlayer player,BlockPos at,String text){
        var display=EntityType.TEXT_DISPLAY.create(player.serverLevel());if(display==null)return;
        CompoundTag tag=new CompoundTag();display.saveWithoutId(tag);tag.putString("text",Component.Serializer.toJson(Component.literal(text),player.registryAccess()));
        tag.putString("billboard","center");tag.putInt("line_width",210);tag.putInt("background",0x44000000);tag.putBoolean("see_through",false);display.load(tag);
        display.moveTo(Vec3.atCenterOf(at));display.addTag(WORDS);display.getPersistentData().putLong("Until",player.serverLevel().getGameTime()+120);player.serverLevel().addFreshEntity(display);
    }
    public static void onServerTick(ServerTickEvent.Post event){
        if(event.getServer().getTickCount()%20!=0)return;ServerLevel level=event.getServer().getLevel(HouseDimensions.OUTSIDE);if(level==null)return;
        BlockPos base=IndianLakeRooms.base(event.getServer(),LabyrinthPlace.SHALLOWS);
        if(base!=null){
            var visitors=IndianLakeRooms.visitors(level,base,LabyrinthPlace.SHALLOWS);
            if(!visitors.isEmpty()){IndianLakeRooms.keepLoaded(level,base,LabyrinthPlace.SHALLOWS);boys(level,base);}
            Set<ServerPlayer> players=new HashSet<>(event.getServer().getPlayerList().getPlayers());players.addAll(level.players());
            for(ServerPlayer player:players){
                if(!IndianLakeRooms.inside(player,LabyrinthPlace.SHALLOWS)&&personal(LabyrinthData.get(player.server),player.getUUID()).getBoolean("Active"))onDepart(player);
                for(int i=0;i<player.getInventory().getContainerSize();i++)if(player.getInventory().getItem(i).is(DrownedTownRegistry.SHALLOWS_BURDEN.get())&&!validBurden(player,player.getInventory().getItem(i)))player.getInventory().setItem(i,ItemStack.EMPTY);
            }
        }
        expireWords(level);
    }
    /** Discard only after traversing the native live lookup; removal can shift its backing map. */
    public static void expireWords(ServerLevel level){
        List<Entity> expired=new ArrayList<>();
        for(Entity entity:level.getAllEntities()){
            if(entity.getTags().contains(WORDS)&&entity.getPersistentData().getLong("Until")<=level.getGameTime())expired.add(entity);
        }
        for(Entity entity:expired)entity.discard();
    }
    public static boolean reset(ServerPlayer player){onDepart(player);LabyrinthData data=LabyrinthData.get(player.server);CompoundTag state=data.state("indian_lake"),boys=state.getCompound("Boys");boys.remove(player.getUUID().toString());state.put("Boys",boys);data.setState("indian_lake",state);save(data,player.getUUID(),new CompoundTag());return true;}

    // ------------------------------------------------------------------------------------------------ for tests
    /** The carry's current swing angle and forward count, as the server holds them. */
    public static float swingAngle(LakeWitchEntity girl){return scene(girl).angle;}
    public static int count(LakeWitchEntity girl){return scene(girl).forward;}
    /** True once the current stroke is far enough along for the next key to start the next one. */
    public static boolean readyForStroke(LakeWitchEntity girl){var s=scene(girl);return s.stroke==0||s.strokeAge>=s.strokeLength*READY;}
}
