package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 0.4.764, the porch read two ways (docs/PORCH_EVENING_PROPOSAL.md; the owner, November 25: "Something should immediately attract
 * the reader and beckon them inside - a silhouette through the window - it should seem simultaneously comforting and dangerous.
 * Outside, there should similarly be things both distracting and potentially warding them away from the danger. The dialogue, cues,
 * and interactions have to be deliberate and intentional and allow players their own interpretation of danger or safety.").
 * <p>
 * Every cue here is one thing that can be taken as safe or as a threat, and nothing says which. The one who cooked is only ever a
 * shape: at the counter behind the kitchen window, in the doorway against the light, on the top step. Their ten lines are said once
 * each, flat, on the action bar. The yard's things pull the other way: a swing, a dropped bicycle, the den under the porch, a dog that
 * knows the whistle, a small shape at the window upstairs, a neighbor at the fence. The truck at ninety seconds and the doorway after
 * it press a reader who has decided nothing. HOME is still read only from the answer (in, under, down the road); what the reader did
 * with the room's doubles is kept beside it and recited without comment.
 * <p>
 * Everything here belongs to one reader: the actors are owner-bound, the dog is in their own yard, the sounds are theirs alone.
 */
@EventBusSubscriber(modid=io.github.knaitoe.theoldesthouse.TheOldestHouse.MOD_ID)
public final class HouseReadPorch {
    private HouseReadPorch(){}
    /** The shapes, on the silhouette's role: the one who cooked, the small one upstairs, the one in the front-room chair, next door. */
    public static final int COOK=7,SMALL=8,SITTER=9,NEIGHBOR=10;
    public static boolean cast(int phase){return phase>=COOK&&phase<=NEIGHBOR;}
    /** The cook's poses: standing and swaying, chopping, a hand to the glass, carrying, stock still, sat on the step, an arm raised, at
     *  the door with one hand low and the other out of the light. Never five, which is the shared crawl. */
    public static final int IDLE=0,CHOP=1,GLASS=2,CARRY=3,STILL=4,SIT=6,RAISED=7,DOORWAY=8;
    /** Ninety seconds in, the truck; thirty seconds after it, the doorway; fifteen there, then the step; thirty on the step. */
    public static final int TRUCK=1800,DOORWAY_AFTER=600,ON_STEP=300,STEP_WAIT=600;
    /** The ten lines, each said once (docs/PORCH_EVENING_PROPOSAL.md, "What is said"). The first is the owner's (HouseReadTexts.SUPPER). */
    public static final String SEE_YOU="I can see you out there.",SWING="Five minutes. I mean it.",ROAD="Don't make me come out there.",
        AGAIN="I'm not calling again.",HOME="Look who's home.",THERE="There you are.",BITE="Well, come on. I won't bite.",SHUT="Shut it behind you. Lock it.",
        OVEN="Your plate's in the oven.",NEXT_DOOR="You go on in now. You know how they get.";
    public static final List<String> LINES=List.of(SEE_YOU,SWING,ROAD,AGAIN,HOME,THERE,BITE,SHUT,OVEN,NEXT_DOOR);
    /** The letter in the mailbox. */
    public static final String LETTER="A letter addressed to the house, not opened. The postmark is from a long way off.";
    // Where things are, from the yard's base.
    static final Vec3 COUNTER=new Vec3(-1.5,3,-9.75),TABLE_SIDE=new Vec3(-1.6,3,-15.4),STOVE=new Vec3(3.5,3,-21.8),SHOULDER=new Vec3(1.4,3,-15.4),
        INSIDE_DOOR=new Vec3(.5,3,-9.0),PORCH=new Vec3(.5,3,-5.5),STEP_SEAT=new Vec3(-.1,3,-2.5),SMALL_AT=new Vec3(.5,8,-9.4),SITTER_AT=new Vec3(3.5,3,-9.5),
        NEIGHBOR_AT=new Vec3(-18.5,1,-17.5),DOG_START=new Vec3(-4.5,0,1.5),DOG_DEN=new Vec3(-2.5,0,-5.5);
    public static final BlockPos STOVE_AT=new BlockPos(3,3,-23),ARMCHAIR_AT=new BlockPos(3,3,-10),UPSTAIRS=new BlockPos(0,8,-8),BIKE_AT=new BlockPos(12,0,-12);
    static final String DOG_TAG="house_read_porch_dog";
    /** Glide speed indoors, a block every twelve ticks or so. */
    static final double PACE=.085;

    /** The yard's added things: the counter at the kitchen window, the stove, the front-room chair, the window upstairs and a light
     *  behind it, the den under the porch, the bicycle, the neighbor's porch and the fence between. */
    public static void build(net.minecraft.server.level.ServerLevel l,BlockPos b){
        HouseReadRooms.fitting(l,b,-3,3,-9,HouseReadPropBlock.Part.TABLE_END,Direction.EAST);HouseReadRooms.fitting(l,b,-2,3,-9,HouseReadPropBlock.Part.TABLE_END,Direction.WEST);
        put(l,b,STOVE_AT,io.github.knaitoe.theoldesthouse.house.TractHouseBlock.of(io.github.knaitoe.theoldesthouse.house.TractHouseBlock.Kind.STOVE,Direction.SOUTH));
        HouseReadRooms.fitting(l,b,ARMCHAIR_AT.getX(),ARMCHAIR_AT.getY(),ARMCHAIR_AT.getZ(),HouseReadPropBlock.Part.ARMCHAIR,Direction.SOUTH);
        for(int y=0;y<2;y++)put(l,b,UPSTAIRS.above(y),HouseReadRegistry.WINDOW.get().defaultBlockState());
        put(l,b,new BlockPos(0,8,-11),Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,9));
        // The den: a blanket laid out on the dirt.
        HouseReadRooms.fitting(l,b,-3,0,-6,HouseReadPropBlock.Part.BATH_MAT,Direction.EAST);HouseReadRooms.fitting(l,b,-2,0,-6,HouseReadPropBlock.Part.BATH_MAT,Direction.EAST);
        put(l,b,BIKE_AT,BorrascaShopBlock.of(BorrascaShopBlock.Kind.BIKE,Direction.EAST,1));
        // Next door: a porch floor, posts, a roof, a light by their door, and a fence between.
        for(int x=-21;x<=-17;x++)for(int z=-20;z<=-15;z++){put(l,b,new BlockPos(x,0,z),HouseReadRegistry.PORCH_BOARDS.get().defaultBlockState());for(int y=1;y<=3;y++)put(l,b,new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());}
        for(int x=-21;x<=-17;x++)for(int y=1;y<=4;y++)put(l,b,new BlockPos(x,y,-21),HouseReadRegistry.CLAPBOARD.get().defaultBlockState());
        for(int y=1;y<=2;y++)put(l,b,new BlockPos(-19,y,-21),Blocks.AIR.defaultBlockState());
        for(int[] post:new int[][]{{-21,-15},{-17,-15}})for(int y=1;y<=3;y++)put(l,b,new BlockPos(post[0],y,post[1]),HouseReadRegistry.POST.get().defaultBlockState());
        for(int x=-21;x<=-17;x++)for(int z=-20;z<=-15;z++)put(l,b,new BlockPos(x,4,z),HouseReadRegistry.SHINGLE_SLAB.get().defaultBlockState());
        HouseReadRooms.fitting(l,b,-18,3,-20,HouseReadPropBlock.Part.PORCH_LIGHT,Direction.SOUTH);
        for(int z=-23;z<=-13;z++)put(l,b,new BlockPos(-16,0,z),HouseReadRegistry.PICKET.get().defaultBlockState());
    }
    private static final int F=net.minecraft.world.level.block.Block.UPDATE_CLIENTS|net.minecraft.world.level.block.Block.UPDATE_KNOWN_SHAPE;
    private static void put(net.minecraft.server.level.ServerLevel l,BlockPos b,BlockPos rel,net.minecraft.world.level.block.state.BlockState s){BuildBlocks.set(l,b.offset(rel),s,F);}

    static Vec3 at(BlockPos b,Vec3 rel){return new Vec3(b.getX()+rel.x,b.getY()+rel.y,b.getZ()+rel.z);}
    private static @Nullable LiteraryActor actor(ServerPlayer p,CompoundTag t,String key){return t.hasUUID(key)&&p.serverLevel().getEntity(t.getUUID(key)) instanceof LiteraryActor a?a:null;}
    private static @Nullable Wolf dog(ServerPlayer p,CompoundTag t){return t.hasUUID("Dog")&&p.serverLevel().getEntity(t.getUUID("Dog")) instanceof Wolf w?w:null;}

    /** The four shapes and the dog, made once each where their spot is loaded. */
    static void ensure(ServerPlayer p,BlockPos b,CompoundTag t){
        record Who(String key,int phase,Vec3 at,float yaw){}
        for(var who:List.of(new Who("Cook",COOK,COUNTER,0),new Who("Small",SMALL,SMALL_AT,0),new Who("Sitter",SITTER,SITTER_AT,0),new Who("Neighbor",NEIGHBOR,NEIGHBOR_AT,-90))){
            if(t.hasUUID(who.key()))continue;var spot=at(b,who.at());if(!p.serverLevel().areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(BlockPos.containing(spot))))continue;
            var a=LiteraryRegistry.ACTOR.get().create(p.serverLevel());if(a==null)continue;
            a.appearance(LiteraryActor.SILHOUETTE,who.phase());a.bind(LabyrinthPlace.READ_PORCH,p.getUUID());a.setNoGravity(true);a.noPhysics=true;a.setNoAi(true);a.setCustomNameVisible(false);
            a.moveTo(spot.x,spot.y,spot.z,who.yaw(),0);a.setYHeadRot(who.yaw());a.yBodyRot=who.yaw();
            a.gesture(who.phase()==COOK?CHOP:who.phase()==SMALL?GLASS:who.phase()==SITTER?STILL:IDLE);
            if(p.serverLevel().addFreshEntity(a))t.putUUID(who.key(),a.getUUID());
        }
        if(!t.hasUUID("Dog")){var spot=at(b,DOG_START);if(p.serverLevel().areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(BlockPos.containing(spot)))){var w=EntityType.WOLF.create(p.serverLevel());
            if(w!=null){w.setNoAi(true);w.setInvulnerable(true);w.setPersistenceRequired();w.noPhysics=true;w.setNoGravity(true);w.addTag(DOG_TAG);w.getPersistentData().putUUID("Reader",p.getUUID());w.moveTo(spot.x,spot.y,spot.z,180,0);if(p.serverLevel().addFreshEntity(w))t.putUUID("Dog",w.getUUID());}}}
    }

    /** Once each: the voice from the kitchen, on the action bar. */
    static boolean say(ServerPlayer p,CompoundTag t,String line){var said=t.getCompound("Said");if(said.getBoolean(line))return false;said.putBoolean(line,true);t.put("Said",said);HouseReadSites.called(p,line);return true;}
    static void heard(ServerPlayer p,SoundEvent s,BlockPos at,float volume,float pitch){LiteraryVignettes.heard(p,s,at,volume,pitch);}

    /** Glides a shape toward a spot inside the house; true once it is there. */
    private static boolean glide(Entity a,Vec3 to,float faceWhenThere){
        var d=to.subtract(a.position());double len=d.length();
        if(len<.05){a.setPos(to);a.setYRot(faceWhenThere);if(a instanceof LiteraryActor l){l.yBodyRot=faceWhenThere;}return true;}
        var step=d.scale(Math.min(1,PACE/len));a.setPos(a.position().add(step));float yaw=(float)(Mth.atan2(-d.x,d.z)*Mth.RAD_TO_DEG);a.setYRot(yaw);
        if(a instanceof net.minecraft.world.entity.LivingEntity l){l.yBodyRot=yaw;l.setYHeadRot(yaw);}return false;
    }
    private static void look(LiteraryActor a,Vec3 at,boolean down){var d=at.subtract(a.getEyePosition());float yaw=(float)(Mth.atan2(-d.x,d.z)*Mth.RAD_TO_DEG);float body=a.yBodyRot;float turn=Mth.clamp(Mth.wrapDegrees(yaw-body),-70,70);a.setYHeadRot(body+turn);a.setXRot(down?35:(float)Mth.clamp(-Mth.atan2(d.y,Math.sqrt(d.x*d.x+d.z*d.z))*Mth.RAD_TO_DEG,-30,30));}

    static boolean onGravel(ServerPlayer p){var below=p.serverLevel().getBlockState(p.blockPosition().below());return below.is(Blocks.GRAVEL);}
    static boolean onSteps(ServerPlayer p,BlockPos b){return p.getY()<b.getY()+3&&Math.abs(p.getX()-b.getX()-.5)<1.6&&p.getZ()>b.getZ()-2.2&&p.getZ()<b.getZ()+1.2;}
    static boolean atScreen(ServerPlayer p,BlockPos b){return p.getY()>=b.getY()+2.8&&Math.abs(p.getX()-b.getX()-.5)<1.8&&p.getZ()>b.getZ()-7.9&&p.getZ()<b.getZ()-5.5;}
    static boolean outside(ServerPlayer p,BlockPos b){return p.getZ()>b.getZ()-8||p.getY()<b.getY()+2.8;}
    static boolean atSwing(ServerPlayer p,BlockPos b){return p.getX()>b.getX()-11.5&&p.getX()<b.getX()-7.5&&Math.abs(p.getZ()-b.getZ()-2.5)<1.6;}
    static boolean atFence(ServerPlayer p,BlockPos b){return p.getX()<b.getX()-13.5&&p.getZ()>b.getZ()-24&&p.getZ()<b.getZ()-12;}

    /**
     * Each tick of the yard for its reader. {@code cue} is the whistle's age (0 before it), {@code first} what the reader answered.
     * Called from HouseReadSites.porch after the read itself, so this never changes HOME; it only sets what is seen and heard, and
     * keeps what the reader did with it.
     */
    static void tick(ServerPlayer p,BlockPos b,CompoundTag t,int age,int cue,String first,boolean wentIn,boolean under,boolean road){
        ensure(p,b,t);var cook=actor(p,t,"Cook");var eye=p.getEyePosition();boolean out=outside(p,b)&&!wentIn;
        // How still the reader is: ten seconds without going half a block makes the shape go still too.
        double lx=t.getDouble("LastX"),lz=t.getDouble("LastZ");if(age==1||Math.abs(p.getX()-lx)+Math.abs(p.getZ()-lz)>.5){t.putDouble("LastX",p.getX());t.putDouble("LastZ",p.getZ());t.putInt("StillSince",age);}
        int still=age-t.getInt("StillSince");
        // The swing still moving when the reader comes into the yard.
        if(age==2)for(int x=-10;x<=-9;x++){var seat=b.offset(x,0,2);var s=p.serverLevel().getBlockState(seat);if(HouseReadSwing.is(s))p.serverLevel().setBlock(seat,s.setValue(HouseReadPropBlock.TURNED,true),3);}
        if(age==60)for(int x=-10;x<=-9;x++){var seat=b.offset(x,0,2);var s=p.serverLevel().getBlockState(seat);if(HouseReadSwing.is(s)&&!p.isPassenger())p.serverLevel().setBlock(seat,HouseReadSwing.still(s),3);}
        if(!t.contains("Arrived"))t.putInt("Arrived",age);
        if(gravelOnce(p,t))t.putBoolean("Stopped",true);
        // The lines, by where the reader is and what they have done.
        if(cue>0&&out){
            if(still>=400&&age>cue+60&&!under)say(p,t,SEE_YOU);
            if(atSwing(p,b))say(p,t,SWING);
            if(road)say(p,t,ROAD);
            if(onSteps(p,b))say(p,t,THERE);
            if(atScreen(p,b)){int at=t.getInt("AtScreen")+1;t.putInt("AtScreen",at);if(at>=40)say(p,t,BITE);}else t.putInt("AtScreen",0);
        }
        if(atFence(p,b)&&!wentIn&&say(p,t,NEXT_DOOR)){var n=actor(p,t,"Neighbor");if(n!=null)n.gesture(RAISED);}
        if(wentIn&&say(p,t,SHUT))t.putInt("InAt",age);
        // The dishes stop once somebody else is home.
        // The truck, if the reader is still outside at ninety seconds.
        if(age>=TRUCK&&!t.contains("Truck")&&out&&!road&&!t.getBoolean("PastMailbox")&&!t.getBoolean("LightsOut")){t.putInt("Truck",age);if(first.isEmpty())t.putBoolean("BeforeAnswerAtTruck",true);}
        if(t.contains("Truck"))truck(p,b,t,age-t.getInt("Truck"));
        // The doorway, if still outside thirty seconds after it.
        if(t.contains("Truck")&&!t.contains("Doorway")&&out&&!road&&!under&&!t.getBoolean("LightsOut")&&age>=t.getInt("Truck")+DOORWAY_AFTER){t.putInt("Doorway",age);if(first.isEmpty())t.putBoolean("BeforeAnswerAtDoorway",true);}
        if(cook!=null)cook(p,b,t,cook,age,cue,first,wentIn,under,road,still,eye);
        var small=actor(p,t,"Small");if(small!=null){small.gesture(GLASS);if(cue>0)look(small,eye,false);}
        var sitter=actor(p,t,"Sitter");if(sitter!=null){sitter.gesture(STILL);sitter.setXRot(40);}
        var n=actor(p,t,"Neighbor");if(n!=null){if(n.gesture()==RAISED){int lift=t.getInt("NeighborWave")+1;t.putInt("NeighborWave",lift);if(lift>60)n.gesture(IDLE);}look(n,eye,false);}
        var w=dog(p,t);if(w!=null)dog(p,b,t,w,cue,under);
        // What the reader did with it, kept beside the read.
        if(wentIn&&t.contains("InAt")&&!t.contains("Door")){var door=p.serverLevel().getBlockState(b.offset(HouseReadRooms.SCREEN));boolean shut=door.getBlock() instanceof DoorBlock&&!door.getValue(DoorBlock.OPEN);
            if(shut&&age>t.getInt("InAt")+4){t.putString("Door","shut");HouseRead.flag(p,HouseReadSites.Room.PORCH,"Shut");}
            else if(age>t.getInt("InAt")+400||p.isPassenger()){t.putString("Door","open");HouseRead.flag(p,HouseReadSites.Room.PORCH,"LeftOpen");}}
        if(!first.isEmpty()&&!t.getBoolean("Lateness")){t.putBoolean("Lateness",true);if(t.getBoolean("BeforeAnswerAtDoorway"))HouseRead.flag(p,HouseReadSites.Room.PORCH,"ComeOutFor");else if(t.getBoolean("BeforeAnswerAtTruck"))HouseRead.flag(p,HouseReadSites.Room.PORCH,"AfterTruck");}
    }
    private static boolean gravelOnce(ServerPlayer p,CompoundTag t){return !t.getBoolean("Stopped")&&onGravel(p);}

    private static void cook(ServerPlayer p,BlockPos b,CompoundTag t,LiteraryActor cook,int age,int cue,String first,boolean wentIn,boolean under,boolean road,int still,Vec3 eye){
        String place=t.getString("CookAt");boolean out=outside(p,b)&&!wentIn;
        // Inside: to the stove, back turned; when the reader sits, to their shoulder a moment, and back.
        if(wentIn){
            if(p.isPassenger()&&!t.contains("SatAt"))t.putInt("SatAt",age);
            boolean shoulder=t.contains("SatAt")&&age>=t.getInt("SatAt")+40&&age<t.getInt("SatAt")+120;
            if(shoulder){cook.gesture(STILL);if(glide(cook,at(b,SHOULDER),90))look(cook,eye,true);}
            else if(place.equals("door")||place.equals("step")||place.equals("porch")){cook.gesture(IDLE);if(glide(cook,at(b,place.equals("door")?INSIDE_DOOR:PORCH),180))t.putString("CookAt",place.equals("door")?"counter":"door");}
            else{cook.gesture(IDLE);if(!t.getBoolean("ViaFront")){if(glide(cook,at(b,new Vec3(3.4,3,-13)),180))t.putBoolean("ViaFront",true);}else if(glide(cook,at(b,STOVE),180)){cook.setYHeadRot(180);cook.setXRot(25);}}
            return;
        }
        // Down the road: a shape in the doorway with one arm up, for as long as the reader looks back, and gone after.
        if(road){
            var house=at(b,new Vec3(.5,5,-12));boolean back=p.getLookAngle().dot(house.subtract(eye).normalize())>.5;
            if(back&&!t.getBoolean("WaveGone")&&p.getZ()<b.getZ()-18){if(!t.getBoolean("Waving")){t.putBoolean("Waving",true);cook.setPos(at(b,INSIDE_DOOR));cook.setYRot(0);cook.yBodyRot=0;openScreen(p,b,true);HouseRead.flag(p,HouseReadSites.Room.PORCH,"SawWave");}cook.gesture(RAISED);look(cook,eye,false);t.putInt("AwayFor",0);}
            else if(t.getBoolean("Waving")){int away=t.getInt("AwayFor")+1;t.putInt("AwayFor",away);if(away>=20){t.putBoolean("Waving",false);t.putBoolean("WaveGone",true);cook.setPos(at(b,COUNTER));cook.gesture(STILL);openScreen(p,b,false);}}
            return;
        }
        // The doorway, then the step, then back in with the door left open.
        if(t.contains("Doorway")){
            int since=age-t.getInt("Doorway");
            if(since==0){openScreen(p,b,true);heard(p,SoundEvents.WOODEN_DOOR_OPEN,b.offset(HouseReadRooms.SCREEN),.6f,.9f);}
            if(since<ON_STEP){cook.gesture(DOORWAY);if(glide(cook,at(b,INSIDE_DOOR),0))look(cook,eye,false);t.putString("CookAt","door");return;}
            if(since<ON_STEP+STEP_WAIT){if(glide(cook,at(b,since<ON_STEP+40?PORCH:STEP_SEAT),0)){cook.gesture(since<ON_STEP+40?IDLE:SIT);t.putString("CookAt",since<ON_STEP+40?"porch":"step");}else cook.gesture(IDLE);if(cook.gesture()==SIT)look(cook,eye,false);return;}
            cook.gesture(IDLE);if(!place.equals("in")){if(glide(cook,at(b,place.equals("step")?PORCH:place.equals("porch")?INSIDE_DOOR:COUNTER),180))t.putString("CookAt",place.equals("step")?"porch":place.equals("porch")?"door":"in");}
            else{cook.gesture(STILL);look(cook,eye,false);}
            return;
        }
        // Under the porch when the whistle's window closes: the humming stops, and the shape is gone from the window.
        if(under&&first.equals("lattice")){cook.gesture(STILL);look(cook,eye,true);return;}
        // At the counter: chopping before the whistle, head down; at the whistle the head turns to the yard and stays turned.
        int since=cue>0?age-cue:-1;
        if(t.contains("Truck")){cook.gesture(STILL);glide(cook,at(b,COUNTER),0);look(cook,eye,false);return;}
        // Once, a while after the whistle: a plate carried to the table, and back.
        if(since>=700&&since<700+260&&out){cook.gesture(CARRY);boolean there=since<700+130?glide(cook,at(b,TABLE_SIDE),180):glide(cook,at(b,COUNTER),0);if(there&&since>=700+130)cook.gesture(STILL);return;}
        glide(cook,at(b,COUNTER),0);
        if(since>=400&&since<440&&out){cook.gesture(GLASS);look(cook,eye,false);return;}
        if(still>=200&&cue>0){cook.gesture(STILL);look(cook,eye,false);return;}
        if(cue>0){cook.gesture(t.getBoolean("Stopped")?STILL:IDLE);look(cook,eye,false);return;}
        cook.gesture(t.getBoolean("Stopped")?STILL:CHOP);look(cook,eye,true);
        if(cook.gesture()==CHOP&&age%14==0)heard(p,SoundEvents.WOOD_HIT,b.offset(-2,4,-9),.3f,1.7f);
    }
    public static void openScreen(ServerPlayer p,BlockPos b,boolean open){for(var pos:List.of(b.offset(HouseReadRooms.SCREEN),b.offset(HouseReadRooms.SCREEN).above())){var door=p.serverLevel().getBlockState(pos);if(door.getBlock() instanceof DoorBlock&&door.getValue(DoorBlock.OPEN)!=open)p.serverLevel().setBlock(pos,door.setValue(DoorBlock.OPEN,open),3);}}

    /** Headlights off the road, across the yard and onto the house; an engine run a moment too long; a door; boots out of sight. */
    private static void truck(ServerPlayer p,BlockPos b,CompoundTag t,int since){
        BlockPos[] sweep={new BlockPos(13,1,-40),new BlockPos(13,1,-34),new BlockPos(13,1,-28),new BlockPos(12,1,-22),new BlockPos(9,1,-17),new BlockPos(6,1,-12),new BlockPos(3,1,-9)};
        if(since==0){heard(p,BikeNightRegistry.ENGINE.get(),b.offset(13,1,-40),1.2f,.7f);}
        int step=since/12;var lit=t.getList("Lights",Tag.TAG_LONG);
        if(since%12==0&&step<=sweep.length){for(var l:lit){var pos=BlockPos.of(((LongTag)l).getAsLong());if(p.serverLevel().getBlockState(pos).is(Blocks.LIGHT))p.serverLevel().setBlock(pos,Blocks.AIR.defaultBlockState(),3);}lit=new ListTag();
            if(step<sweep.length)for(int dx=-1;dx<=1;dx+=2){var pos=b.offset(sweep[step]).offset(dx,0,0);if(p.serverLevel().getBlockState(pos).isAir()){p.serverLevel().setBlock(pos,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,15),3);lit.add(LongTag.valueOf(pos.asLong()));}}
            t.put("Lights",lit);}
        if(since==sweep.length*12+40){var pos=b.offset(sweep[sweep.length-1]);if(p.serverLevel().getBlockState(pos).isAir()){p.serverLevel().setBlock(pos,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,15),3);var held=new ListTag();held.add(LongTag.valueOf(pos.asLong()));t.put("Lights",held);}}
        if(since==sweep.length*12+120){for(var l:t.getList("Lights",Tag.TAG_LONG)){var pos=BlockPos.of(((LongTag)l).getAsLong());if(p.serverLevel().getBlockState(pos).is(Blocks.LIGHT))p.serverLevel().setBlock(pos,Blocks.AIR.defaultBlockState(),3);}t.remove("Lights");heard(p,SoundEvents.IRON_DOOR_CLOSE,b.offset(6,1,-28),.9f,.8f);}
        for(int i=0;i<3;i++)if(since==sweep.length*12+140+i*10)heard(p,SoundEvents.GRAVEL_STEP,b.offset(8-i*2,0,-26),.9f,.9f);
        for(int i=0;i<3;i++)if(since==sweep.length*12+180+i*10)heard(p,HouseReadRegistry.STEP.get(),b.offset(0,3,-25),.8f,.8f);
        if(since==sweep.length*12+220)say(p,t,HOME);
    }

    /** The dog comes to the reader in the yard; at the whistle it goes under the porch and lies down. */
    private static void dog(ServerPlayer p,BlockPos b,CompoundTag t,Wolf w,int cue,boolean under){
        if(cue>0){
            var gap=at(b,new Vec3(-6.5,0,-4.5));boolean in=w.getX()>b.getX()-5.8&&w.getX()<b.getX()+5.8&&w.getZ()<b.getZ()-2.5;
            boolean there=in?glide(w,at(b,DOG_DEN),90):(Math.abs(w.getX()-gap.x)+Math.abs(w.getZ()-gap.z)<.2?glide(w,at(b,new Vec3(-5.2,0,-4.5)),90):glide(w,gap,90));
            if(there&&in&&!w.isInSittingPose())w.setInSittingPose(true);
            if(!t.getBoolean("DogWent")){t.putBoolean("DogWent",true);heard(p,SoundEvents.WOLF_WHINE,w.blockPosition(),.6f,1);}
            return;
        }
        var to=p.position().subtract(w.position()).multiply(1,0,1);double len=to.length();
        if(len>2.2&&len<16&&p.getY()<b.getY()+1.5){w.setInSittingPose(false);glide(w,w.position().add(to.normalize().scale(Math.min(.12,len-2.2))),w.getYRot());}
        else if(len<=2.2){float yaw=(float)(Mth.atan2(-to.x,to.z)*Mth.RAD_TO_DEG);w.setYRot(yaw);w.yBodyRot=yaw;w.setYHeadRot(yaw);}
    }

    // The dog is the yard's, not the reader's: no taming, no leads, no harm.
    private static boolean porchDog(Entity e){return e instanceof Wolf w&&w.getTags().contains(DOG_TAG);}
    @SubscribeEvent public static void pet(PlayerInteractEvent.EntityInteract e){
        if(!porchDog(e.getTarget()))return;e.setCanceled(true);e.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        if(e.getLevel().isClientSide||e.getHand()!=net.minecraft.world.InteractionHand.MAIN_HAND||!(e.getEntity() instanceof ServerPlayer p))return;
        heard(p,SoundEvents.WOLF_PANT,e.getTarget().blockPosition(),.6f,1);HouseReadSites.act(p,HouseReadSites.Room.PORCH,"petted the dog");
    }
    @SubscribeEvent public static void petSpecific(PlayerInteractEvent.EntityInteractSpecific e){if(porchDog(e.getTarget())){e.setCanceled(true);e.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);}}
    @SubscribeEvent public static void harm(AttackEntityEvent e){if(porchDog(e.getTarget()))e.setCanceled(true);}

    /** The mailbox's letter, on the action bar. */
    static void letter(ServerPlayer p){p.displayClientMessage(Component.literal(LETTER).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
}
