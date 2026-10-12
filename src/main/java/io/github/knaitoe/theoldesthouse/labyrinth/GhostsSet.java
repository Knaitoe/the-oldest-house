package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Seven personal filming days. An actual departure and subsequent door arrival alone admit the next day. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class GhostsSet {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.GHOSTS_SET;
    public static final String STATE="ghosts_set_0485";
    public static final int DAYS=7, STAND=0, SIT=1, CROUCH=2, CRAWL=3, WALL=4, BED=5, FLINCH=6, SLUMP=7, VOMIT=8, HAND=9,LEAP=10;
    public static final String[] PEOPLE={"Marjorie","Merry","John","Sarah","Barry","Father Wanderly"};
    public static final int[] ROLES={LiteraryActor.GHOST_MARJORIE,LiteraryActor.GHOST_MERRY,LiteraryActor.GHOST_JOHN,LiteraryActor.GHOST_SARAH,LiteraryActor.GHOST_PRODUCER,LiteraryActor.GHOST_PRIEST};
    public static final String[] TITLES={"The interviews and reconstructions","The family dinner","The upstairs reconstruction","The private conversation","The evaluation","The exorcism","The final pickup"};
    public static final int[] LENGTH={800,1200,1000,1200,1000,780,1200};
    private static final int[][] EXPOSURES={{160,360,560,740},{300,570,900,1100},{250,420,620,900},{170,470,780,1080},{170,480,650,900},{180,560,735,770},{180,550,800,1100}};
    private record Line(int tick,int speaker,String text) {}
    // Original adaptation dialogue. Recollections and recreated incidents are explicitly distinguished from documentary takes.
    private static final List<List<Line>> LINES=List.of(
        List.of(new Line(5,4,"We start with interviews. The bedroom shots are reconstructions of what the girls told us."),
            new Line(100,1,"We used to put our own stories in the books. Now she puts things in them I don't want to read."),
            new Line(220,0,"A story can know what you haven't said yet. Ask Merry about the sisters and the vines."),
            new Line(360,1,"I tied the door shut. I balanced a jug over it. In the morning there was a note on me."),
            new Line(520,3,"You can film the books. Please give them back when you're done."),new Line(660,2,"We agreed to a documentary. Tell people that we are trying to help her.")),
        List.of(new Line(5,4,"Keep rolling through supper. Let them talk to one another."),
            new Line(100,1,"Could I borrow your sparkly hat for school tomorrow?"),
            new Line(280,3,"Marjorie? Are you feeling sick?"),
            new Line(400,0,"You will die, Merry. So will everyone sitting here. My hat isn't for dead things."),
            new Line(500,2,"Take my hand. We can pray together."),new Line(650,3,"Come out, sweetheart. We can go upstairs. I'll stay with you."),
            new Line(760,0,"Your hand, Dad. Down here."),new Line(880,2,"Ah! My hand—"),
            new Line(1000,0,"Look at the old fittings underneath. You reached into them. You hurt yourself."),new Line(1140,0,"Merry isn't the only one who can change her voice.")),
        List.of(new Line(5,4,"An upstairs pickup. The wall incident happened before we arrived. Keep the damaged plaster in the shot."),
            new Line(180,0,"They are inside my head. I can't get away from them."),new Line(340,3,"John, be careful. Help her down."),
            new Line(540,0,"Merry, you keep telling people things. What would you tell them with no tongue?"),
            new Line(680,1,"I don't want to do this part again."),new Line(840,3,"That's enough. Give them a minute.")),
        List.of(new Line(5,0,"Down here. Could you put the camera down? Just for this conversation."),
            new Line(170,0,"I know what they want to see. I can give them that. It doesn't mean everything in my head has stopped."),
            new Line(320,0,"The show pays. Dad hasn't found work. Everyone acts as if I'm the only person who needs help."),
            new Line(470,1,"Then why are you still scaring me?"),new Line(620,4,"We need one more bedroom pickup tonight. Merry has her little camera."),
            new Line(780,1,"Stop! That's my house!"),new Line(1030,0,"Did you get it? All of it?")),
        List.of(new Line(5,5,"We need to establish what we are dealing with."),new Line(170,0,"With cameras in the room? You know your own rules forbid that."),
            new Line(330,2,"How could she know these things?"),new Line(480,3,"She can read, John. She can use the internet. She's a person."),
            new Line(650,0,"One voice asks for help. Another makes good television. Which one are you waiting for?"),
            new Line(840,4,"Hold on the father's reaction. We can use that.")),
        List.of(new Line(5,3,"If you want me to stop this, say so."),new Line(150,0,"I'm cold. Please. Someone help me."),
            new Line(330,5,"Keep your places. Do not answer it."),new Line(510,0,"The drawer! I didn't do that."),
            new Line(660,0,"There's a mechanism in it. Who put this here?"),new Line(705,1,"You said you were pretending!"),
            new Line(715,3,"Marjorie!")),
        List.of(new Line(5,4,"The show is over. We're collecting the equipment. This visit wasn't on the call sheet."),
            new Line(180,3,"We're having supper. You can put the case by the door."),new Line(350,0,"Merry has hers without sauce. She always does."),
            new Line(550,1,"You said you weren't going to eat it."),new Line(790,1,"Marjorie? Mum?"),
            new Line(1000,4,"Leave your account in the booth. Keep the tape and the cut separate.")));
    private static final Map<ServerLevel,BuildBlocks.Plan> REPAIRS=new IdentityHashMap<>();
    private static final Map<ServerLevel,Set<BlockPos>> REPAIR_CELLS=new IdentityHashMap<>();
    private GhostsSet() {}
    public static boolean built(LabyrinthData d,BlockPos b) { return d.state(STATE).getBoolean(Long.toString(b.asLong())); }
    public static void markBuilt(LabyrinthData d,BlockPos b) {var t=d.state(STATE);t.putBoolean(Long.toString(b.asLong()),true);t.remove("Repair_"+b.asLong());d.setState(STATE,t);}
    public static void clearAll() { REPAIRS.clear();REPAIR_CELLS.clear(); }
    private static int day(CompoundTag own) { return Math.max(1,Math.min(DAYS,own.getInt("FilmDay"))); }
    public static void arrive(ServerPlayer p,CompoundTag own) {
        if(own.getBoolean("Completed")||own.getBoolean("Ready")&&!own.getBoolean("Filming0485"))return;
        boolean newDay=false;
        if(!own.getBoolean("Filming0485")) { own.putBoolean("Filming0485",true);own.putInt("FilmDay",1);own.putInt("ShotClock",0);newDay=true; }
        else if(own.getBoolean("DayDone")&&own.getBoolean("FilmDoorExit")&&day(own)<DAYS) {
            own.putInt("FilmDay",day(own)+1);own.putInt("ShotClock",0);own.putBoolean("DayDone",false);own.putBoolean("Rolling",false);own.putBoolean("CastPlaced",false);newDay=true;
        }
        own.putBoolean("FilmDoorExit",false);own.remove("FrameNonce");
        if(newDay){own.remove("JumpFrozen0488");own.remove("JumpEject0488");own.remove("GhostVoice");own.remove("GhostVoiceUntil");}
        LiteraryVignettes.reward(p,PLACE,own,"Camcorder",new ItemStack(LiteraryRegistry.CAMCORDER.get()));
        p.displayClientMessage(Component.literal("Filming day "+day(own)+": "+TITLES[day(own)-1]+". Speak with Barry when you're ready."),false);
    }
    /** Called only by the physical doorway departure hook, never by logout, a death or a scene timer. */
    public static void depart(ServerPlayer p) {
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);
        own.putBoolean("FilmDoorExit",true);own.remove("FrameNonce");LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
    }
    public static boolean recording(ServerPlayer p) {
        if(!LiteraryVignettes.inside(p,PLACE)||!p.isUsingItem()||!(p.getUseItem().getItem() instanceof CamcorderItem))return false;
        var custom=p.getUseItem().get(DataComponents.CUSTOM_DATA); if(custom==null)return false;
        var t=custom.copyTag();return t.hasUUID(LiteraryVignettes.OWNER)&&t.getUUID(LiteraryVignettes.OWNER).equals(p.getUUID());
    }
    /** A centred lens and an actual unobstructed native ray; adjacent table tops do not count as transparent. */
    public static boolean framed(ServerPlayer p,Vec3 target) {
        return framed(p,p.getEyePosition(),p.getLookAngle(),target);
    }
    private static boolean frozen(CompoundTag own){return day(own)==6&&own.getBoolean("JumpFrozen0488")&&!own.getBoolean("DayDone");}
    private static Vec3 lensEye(ServerPlayer p,CompoundTag own){return frozen(own)?new Vec3(own.getDouble("GhostEyeX"),own.getDouble("GhostEyeY"),own.getDouble("GhostEyeZ")):p.getEyePosition();}
    private static Vec3 lensLook(ServerPlayer p,CompoundTag own){return frozen(own)?Vec3.directionFromRotation(own.getFloat("GhostPitch"),own.getFloat("GhostYaw")):p.getLookAngle();}
    private static boolean framed(ServerPlayer p,Vec3 eye,Vec3 look,Vec3 target){
        var ray=target.subtract(eye);if(ray.lengthSqr()>32*32||ray.lengthSqr()<.01||look.dot(ray.normalize())<Math.cos(Math.toRadians(32)))return false;
        var hit=p.serverLevel().clip(new ClipContext(eye,target,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p));
        return hit.getType()==HitResult.Type.MISS||hit.getLocation().distanceToSqr(target)<.0025;
    }
    private static LiteraryActor person(ServerPlayer p,BlockPos b,int i) {
        var d=LabyrinthData.get(p.server);var world=LiteraryVignettes.shared(d,PLACE);String key="Film"+PEOPLE[i]+"_"+p.getUUID();
        // Claim the existing three native family bodies once rather than deleting or duplicating their identities.
        String old=switch(i){case 1->"Child";case 2->"Father";case 3->"Mother";default->"";};
        if(!world.hasUUID(key)&&!old.isEmpty()&&world.hasUUID(old)&&p.serverLevel().getEntity(world.getUUID(old)) instanceof LiteraryActor legacy&&legacy.owner().isEmpty()) {
            legacy.bind(PLACE,p.getUUID());legacy.appearance(ROLES[i],STAND);world.putUUID(key,legacy.getUUID());world.remove(old);LiteraryVignettes.shared(d,PLACE,world);
        }
        var home=i<4?new BlockPos(i==0||i==2?-3:3,0,i<2?-29:-27):new BlockPos(16,0,i==4?-22:-31);
        var at=b.offset(home);if(!p.serverLevel().hasChunkAt(at)||!p.serverLevel().areEntitiesLoaded(ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))return null;
        var a=LiteraryVignettes.actor(p,PLACE,"Film"+PEOPLE[i],ROLES[i],home,true);
        if(a!=null){a.setNoGravity(true);a.setCustomName(Component.literal(PEOPLE[i]));a.setCustomNameVisible(false);}return a;
    }
    private static void pose(LiteraryActor a,int phase) { a.appearance(a.role(),phase); }
    private static void place(LiteraryActor a,BlockPos b,double x,double y,double z,float yaw,int phase) {
        pose(a,phase);a.moveTo(b.getX()+x,b.getY()+y,b.getZ()+z,yaw,0);a.yHeadRot=yaw;a.yBodyRot=yaw;
    }
    /** Scripted movement still goes through native collision; no tick teleports a visible actor through a table or wall. */
    private static void toward(LiteraryActor a,BlockPos b,double x,double y,double z,double speed) {
        var to=new Vec3(b.getX()+x,b.getY()+y,b.getZ()+z).subtract(a.position());double len=to.length();
        if(len>.005){var step=to.scale(Math.min(speed,len)/len);a.move(MoverType.SELF,step);if(step.horizontalDistanceSqr()>.001){float yaw=(float)(Math.atan2(-step.x,step.z)*180/Math.PI);a.setYRot(yaw);a.yHeadRot=yaw;a.yBodyRot=yaw;}}
    }
    private static Vec3 focus(BlockPos b,int day) { return Vec3.atBottomCenterOf(b.offset(switch(day){case 3,6->new BlockPos(-9,5,-30);case 4->GhostsSetRooms.BASEMENT;default->GhostsSetRooms.TABLE;})); }
    private static void stage(BlockPos b,LiteraryActor[] cast,int d) {
        for(int i=0;i<4;i++)place(cast[i],b,i==0||i==2?-2.4:3.4,.625,i<2?-28.5:-26.5,i==0||i==2?-90:90,SIT);
        place(cast[4],b,16.5,0,-21.5,90,STAND);place(cast[5],b,16.5,0,-30.5,90,STAND);
        if(d==1){place(cast[0],b,-7,0,-16.5,90,STAND);place(cast[1],b,-10,0,-16.5,-90,STAND);}
        if(d==3||d==6){place(cast[0],b,d==6?-9.5:-12.5,5,d==6?-32.5:-33,d==6?0:90,d==6?BED:STAND);place(cast[1],b,-6,5,-28,180,STAND);place(cast[2],b,-8,5,-34,-90,STAND);place(cast[3],b,-8,5,-29,180,STAND);place(cast[5],b,-11,5,-31,180,STAND);}
        if(d==4){place(cast[0],b,7,-4,-27,0,STAND);place(cast[1],b,7,-4,-30,180,STAND);}
        if(d==5){for(int i=1;i<4;i++)place(cast[i],b,-12+i*2,5,-33,0,STAND);place(cast[0],b,-9.5,5.75,-32.5,0,SIT);place(cast[5],b,-8,5,-29,180,STAND);}
    }
    public static boolean start(ServerPlayer p,CompoundTag own) {
        if(!LiteraryVignettes.inside(p,PLACE)||own.getBoolean("DayDone")||!own.getBoolean("Read_Source"))return false;
        own.putBoolean("Rolling",true);return true;
    }
    private static final double[][] PICKUP_ROUTE={{8.5,-4,-25.5},{8.5,-4,-24.5},{8.5,-3,-23.5},{8.5,-2,-22.5},{8.5,-1,-21.5},{8.5,0,-20.5},{8.5,0,-19.5},{-17.5,0,-14.5},{-17.5,1,-15.5},{-17.5,2,-16.5},{-17.5,3,-17.5},{-17.5,4,-18.5},{-17.5,5,-19.5},{-17.5,5,-20.5},{-16,5,-21.5},{8.5,5,-21.5},{8.5,5,-25},{8.5,5,-29.5}};
    private static void pickup(BlockPos b,LiteraryActor[] cast,CompoundTag own){
        for(int i=0;i<2;i++){String key="PickupStep"+i;int step=own.getInt(key);if(step>=PICKUP_ROUTE.length)continue;var at=PICKUP_ROUTE[step];pose(cast[i],STAND);cast[i].setOnGround(true);toward(cast[i],b,at[0]+i*.05,at[1],at[2],.35);if(cast[i].position().distanceToSqr(new Vec3(b.getX()+at[0]+i*.05,b.getY()+at[1],b.getZ()+at[2]))<.16)own.putInt(key,step+1);}
    }
    public static boolean cameraCrawl(ServerPlayer p,BlockPos b){double x=p.getX()-b.getX(),z=p.getZ()-b.getZ();return Math.abs(p.getY()-b.getY())<1&&(p.isShiftKeyDown()&&x>=-4&&x<=5&&z>=-31&&z<=-26||x>=-1.4&&x<=2.4&&z>=-29.5&&z<=-27&&!p.serverLevel().noCollision(p,new AABB(p.getX()-.3,p.getY(),p.getZ()-.3,p.getX()+.3,p.getY()+1.8,p.getZ()+.3)));}
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own) {
        if(!own.getBoolean("Filming0485"))return;
        if(a.role()==LiteraryActor.GHOST_PRODUCER){
            var icons=new HashMap<Integer,ItemStack>();
            icons.put(0,LiteraryChoiceMenu.icon(Items.REDSTONE,own.getBoolean("DayDone")?"Review the edited episode":"Ready for today's filming"));
            icons.put(4,LiteraryChoiceMenu.icon(Items.PAPER,"Review the unedited camera log"));
            icons.put(8,LiteraryChoiceMenu.icon(Items.OAK_DOOR,own.getBoolean("DayDone")?"Leave until the following day":"Leave this day unfinished"));
            LiteraryChoiceMenu.open(p,"Filming day "+day(own),icons,()->LiteraryVignettes.inside(p,PLACE)&&p.distanceToSqr(a)<36,slot->{
                var data=LabyrinthData.get(p.server);var now=LiteraryVignettes.personal(data,p.getUUID(),PLACE);
                if(slot==0&&now.getBoolean("DayDone"))LiteraryVignettes.open(p,PLACE,"Episode"+day(now),edit(p,now,day(now),false),a.blockPosition());
                else if(slot==0){if(!start(p,now))p.displayClientMessage(Component.literal("Read the call sheet first."),true);}
                else if(slot==4&&now.getBoolean("DayDone"))LiteraryVignettes.open(p,PLACE,"Rushes"+day(now),edit(p,now,day(now),true),a.blockPosition());
                else if(slot==4)p.displayClientMessage(Component.literal("Barry: We'll review the rushes after the day's take."),true);
                LiteraryVignettes.save(data,p.getUUID(),PLACE,now);return slot==0||slot==4||slot==8;
            });return;
        }
        if(a.phase()==SLUMP&&a.role()<=LiteraryActor.GHOST_SARAH){p.displayClientMessage(Component.literal(PEOPLE[a.role()-LiteraryActor.GHOST_MARJORIE]+" does not answer."),false);return;}
        int i=Arrays.stream(ROLES).boxed().toList().indexOf(a.role());if(i<0)return;
        String line=switch(i){
            case 0->day(own)==4?"Being watched doesn't make the voices stop. Sometimes I can decide what you see.":"You keep looking through that thing. Do you ever look at me?";
            case 1->day(own)>3?"They cut out the bits where she talks normally. Will they do that to me too?":"Could you leave the pictures here? They're ours.";
            case 2->"We need people to understand what this family is going through.";
            case 3->"She has a doctor. Remember that when you put this together.";
            default->"An image is evidence of what it shows. Be careful with what you say it proves.";
        };p.displayClientMessage(Component.literal(PEOPLE[i]+": "+line),false);
        if(day(own)==4&&i==0&&!recording(p))own.putBoolean("RespectedPrivacy",true);
        if(day(own)>1){var prev=own.getCompound("FilmLog"+(day(own)-1));if(prev.getInt("Frames_Merry")>=20&&i==1)p.displayClientMessage(Component.literal("Merry: They kept my face in the episode. People at school saw it."),false);if(prev.getInt("Frames_Sarah")>=20&&i==3)p.displayClientMessage(Component.literal("Sarah: They used my crying. The part where I asked them to stop is gone."),false);}
    }
    private static void capture(ServerPlayer p,CompoundTag own,String subject,Vec3 at) {
        if(!recording(p)||!framed(p,lensEye(p,own),lensLook(p,own),at))return;
        var log=own.getCompound("FilmLog"+day(own));String k="Frames_"+subject;log.putInt(k,Math.min(72000,log.getInt(k)+5));own.put("FilmLog"+day(own),log);
    }
    private static LiteraryActor prop(ServerPlayer p,BlockPos b,String key,int role,BlockPos relative){
        var at=b.offset(relative);if(!p.serverLevel().hasChunkAt(at)||!p.serverLevel().areEntitiesLoaded(ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))return null;
        var a=LiteraryVignettes.actor(p,PLACE,key,role,relative,true);if(a!=null){a.setNoGravity(true);a.setCustomNameVisible(false);}return a;
    }
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own) {
        if(!own.getBoolean("Filming0485")||own.getBoolean("Completed")||!built(LabyrinthData.get(p.server),b))return;
        var cast=new LiteraryActor[6];for(int i=0;i<6;i++){cast[i]=person(p,b,i);if(cast[i]==null)return;}
        var playhouse=prop(p,b,"FilmPlayhouse",LiteraryActor.GHOST_PLAYHOUSE,new BlockPos(9,5,-32));
        var drawer=prop(p,b,"FilmDrawer",LiteraryActor.GHOST_DRAWER,new BlockPos(-12,5,-33));
        if(playhouse==null||drawer==null)return;
        int d=day(own);if(!own.getBoolean("CastPlaced")){stage(b,cast,d);pose(playhouse,0);pose(drawer,0);own.putBoolean("CastPlaced",true);}
        if(d==4&&own.getBoolean("BedroomPickup")&&(own.getInt("PickupStep0")<PICKUP_ROUTE.length||own.getInt("PickupStep1")<PICKUP_ROUTE.length)){pickup(b,cast,own);return;}
        if(own.getBoolean("DayDone")) { display(p,own,b); return; }
        var here=d==4&&own.getBoolean("BedroomPickup")?Vec3.atBottomCenterOf(b.offset(GhostsSetRooms.MERRY_ROOM)):focus(b,d);
        if(!own.getBoolean("Rolling")||p.distanceToSqr(here)>24*24){display(p,own,b);return;}
        // The evaluation takes place upstairs too; the utility conversation moves to the bedroom later in its day.
        int previous=own.getInt("ShotClock"),t=previous+5;own.putInt("ShotClock",t);
        for(var line:LINES.get(d-1))if(line.tick()>previous&&line.tick()<=t&&p.distanceToSqr(cast[line.speaker()])<24*24){String words=PEOPLE[line.speaker()]+": "+line.text();if(line.speaker()==0&&d==2){own.putString("GhostVoice",words);own.putInt("GhostVoiceId",d*10000+line.tick());own.putLong("GhostVoiceUntil",p.level().getGameTime()+140);}else p.displayClientMessage(Component.literal(words),false);if(d==2&&line.speaker()==0)cue(p,line.tick()<1100?LiteraryRegistry.GHOST_LOW:LiteraryRegistry.GHOST_THIN,cast[0].getEyePosition());}
        if(d==2) {
            if(previous<520&&t>=520)cue(p,LiteraryRegistry.GHOST_SCRAPE,cast[0].position());
            if(t>=250&&t<520){pose(cast[0],VOMIT);if(t%20==0)p.serverLevel().sendParticles(p,net.minecraft.core.particles.ParticleTypes.SPORE_BLOSSOM_AIR,false,b.getX()-.5,b.getY()+1.03,b.getZ()-28.5,2,.06,.02,.06,0);}
            if(t>=520&&t<950){pose(cast[0],CRAWL);toward(cast[0],b,-.4,0,-28.5,.24);}
            if(t>=880)pose(cast[2],HAND);
            if(t>=950){pose(cast[0],CRAWL);toward(cast[0],b,-.4,0,-38.5,.24);}
            if(t>=100&&t<500)capture(p,own,"HatExchange",cast[0].getEyePosition());
            if(t>=520&&t<1000){capture(p,own,"UnderTable",cast[0].getEyePosition());capture(p,own,"TableFittings",Vec3.atLowerCornerOf(b).add(.5,.69,-28.435));}
            if(t>=880)capture(p,own,"InjuredHand",cast[2].position().add(0,.9,0));
        }
        if(d==3) {if(t<430){pose(cast[0],WALL);toward(cast[0],b,-12.5,7,-33,.16);capture(p,own,"Handholds",Vec3.atLowerCornerOf(b).add(-13,7.4,-33));}else{pose(cast[0],STAND);toward(cast[0],b,-7,5,-28,.24);if(t<750)capture(p,own,"Threat",cast[0].getEyePosition());}}
        if(d==4){
            if(previous<780&&t>=780)cue(p,LiteraryRegistry.GHOST_CARD,playhouse.position());
            if(t<570){capture(p,own,"PrivateConfession",cast[0].getEyePosition());if(!recording(p))own.putBoolean("RespectedPrivacy",true);}
            if(t>=570&&!own.getBoolean("BedroomPickup")){
                // Both sisters climb the real stair on their next pickup; the player follows them.
                own.putBoolean("BedroomPickup",true);own.putBoolean("Rolling",false);
                p.displayClientMessage(Component.literal("Barry: Upstairs for the last pickup. Speak with me when you're ready."),false);
            }
            if(t>=575){pose(cast[0],CRAWL);toward(cast[0],b,9.3,5,-31.5,.2);pose(playhouse,t>=900?2:t>=780?1:0);capture(p,own,"Playhouse",playhouse.position().add(0,t>=900?.1:.5,0));}
        }
        if(d==5){capture(p,own,"Evaluation",cast[0].getEyePosition());capture(p,own,"MotherObjection",cast[3].getEyePosition());}
        if(d==6){
            if(t<720)pose(cast[0],BED);
            if(t>=510){pose(drawer,t>=660?2:1);if(t<820)capture(p,own,"DrawerMechanism",drawer.position().add(0,.55,-.4));}
            if(t>=720&&t<780){
                if(!own.getBoolean("JumpFrozen0488")){var eye=p.getEyePosition();pose(cast[0],LEAP);own.putBoolean("JumpFrozen0488",true);own.putDouble("GhostEyeX",eye.x);own.putDouble("GhostEyeY",eye.y);own.putDouble("GhostEyeZ",eye.z);cast[0].moveTo(cast[0].getX(),cast[0].getY()+.8,cast[0].getZ());var aim=cast[0].getEyePosition().subtract(eye);own.putFloat("GhostYaw",(float)(Math.toDegrees(Math.atan2(aim.z,aim.x))-90));own.putFloat("GhostPitch",(float)-Math.toDegrees(Math.atan2(aim.y,Math.sqrt(aim.x*aim.x+aim.z*aim.z))));own.putFloat("JumpBodyYaw",cast[0].getYRot());}
                pose(cast[0],LEAP);cast[0].setDeltaMovement(Vec3.ZERO);cast[0].setNoGravity(true);float body=own.getFloat("JumpBodyYaw");var toCamera=new Vec3(own.getDouble("GhostEyeX"),own.getDouble("GhostEyeY"),own.getDouble("GhostEyeZ")).subtract(cast[0].getEyePosition());float head=(float)(Math.toDegrees(Math.atan2(toCamera.z,toCamera.x))-90);float turn=net.minecraft.util.Mth.wrapDegrees(head-body);cast[0].setYRot(body);cast[0].setYBodyRot(body);cast[0].setYHeadRot(body+turn*Math.min(1,(t-720)/45F));own.putInt("GhostFreezeTarget",cast[0].getId());capture(p,own,"FrozenLeap",cast[0].getEyePosition());
            }
            if(t>=780)own.putBoolean("JumpEject0488",true);
        }
        if(d==7){if(t>=700)for(int i:new int[]{0,2,3})pose(cast[i],SLUMP);capture(p,own,"FinalMeal",cast[0].getEyePosition());capture(p,own,"MerryPlate",Vec3.atLowerCornerOf(b).add(1.5,1.06,-28.5));}
        for(int i=0;i<4;i++)capture(p,own,PEOPLE[i],cast[i].getEyePosition());
        if(recording(p)){
            var eye=lensEye(p,own);var look=lensLook(p,own);int lead=-1;double alignment=-1;for(int i=0;i<4;i++)if(framed(p,eye,look,cast[i].getEyePosition())){double a=look.dot(cast[i].getEyePosition().subtract(eye).normalize());if(a>alignment){alignment=a;lead=i;}}
            if(lead>=0){var log=own.getCompound("FilmLog"+d);var key="LeadTicks_"+PEOPLE[lead];log.putInt(key,Math.min(72000,log.getInt(key)+5));own.put("FilmLog"+d,log);}
            for(int exposure:EXPOSURES[d-1])if(previous<exposure&&t>=exposure)requestFrame(p,own);
        }
        if(t>=LENGTH[d-1]) {
            own.putBoolean("DayDone",true);own.putBoolean("Rolling",false);own.putInt("FilmedDays",own.getInt("FilmedDays")|(1<<(d-1)));own.putInt("Beat",own.getInt("Visit"));
            p.displayClientMessage(Component.literal(d==DAYS?"The equipment is packed. Leave your account in the booth.":"Barry: That's the day. Review the cut if you want, then leave through the door. We'll pick this up another day."),false);
        }
        if(finished(own))LiteraryVignettes.ready(p,PLACE,own,"completed_the_filming_days_and_read_own_cut");
        display(p,own,b);
    }
    private static void cue(ServerPlayer p,net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> sound,Vec3 at){p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(sound,net.minecraft.sounds.SoundSource.NEUTRAL,at.x,at.y,at.z,.6F,1,p.getRandom().nextLong()));}
    public static ItemStack edit(ServerPlayer p,CompoundTag own,int d,boolean raw) { return edit(p.getGameProfile().getName(),own,d,raw); }
    public static ItemStack edit(String name,CompoundTag own,int d,boolean raw) {
        var log=own.getCompound("FilmLog"+d);var text=new StringBuilder((raw?"CAMERA LOG":"THE POSSESSION — EDITED EPISODE")+"\n\nDay "+d+": "+TITLES[d-1]+"\n\n");
        var seen=new ArrayList<String>();for(var key:log.getAllKeys())if(key.startsWith("Frames_")&&log.getInt(key)>=20)seen.add(key.substring(7));Collections.sort(seen);
        if(raw){text.append(seen.isEmpty()?"No sustained usable shot was recorded.\n":"Sustained shots: "+String.join(", ",seen)+".\n");text.append("The camera log records framing, not a diagnosis. No shot proves what happened outside it.");}
        else {
            String lead="";int held=0;for(int i=0;i<4;i++)if(log.getInt("LeadTicks_"+PEOPLE[i])>held){lead=PEOPLE[i];held=log.getInt("LeadTicks_"+lead);}
            if(held>=20)text.append(switch(lead){case "Marjorie"->"Your longest centred subject is Marjorie. The production builds a possession story around her body and voice.\n\n";case "John"->"Your longest centred subject is John. The production makes this a father's struggle to save his family.\n\n";case "Sarah"->"Your longest centred subject is Sarah. Her medical concerns become the episode's story of a family losing trust.\n\n";default->"Your longest centred subject is Merry. The episode makes a child's frightened perspective its point of view.\n\n";});
            if(seen.contains("Marjorie"))text.append("The cut holds on Marjorie. Barry presents the pauses and changes of voice as a continuous display.\n\n");
            if(seen.contains("John"))text.append("John's reaction becomes the centre of the episode: a father confronting something he cannot understand.\n\n");
            if(seen.contains("Sarah"))text.append("Sarah's distress is used as a reaction shot. Her objections lose their surrounding conversation.\n\n");
            if(seen.contains("Merry"))text.append("Merry's frightened face is held after the words that frightened her have ended.\n\n");
            if(seen.contains("UnderTable"))text.append("The lens followed Marjorie below the table. The family remains audible beyond the frame.\n\n");
            if(seen.contains("TableFittings"))text.append("The rusty fittings are visible in the rushes. Barry omits that shot from the broadcast. You still have its place in the camera log.\n\n");
            if(seen.contains("DrawerMechanism"))text.append("You kept the mechanism in the frame. The broadcast cuts away before it can be examined.\n\n");
            if(seen.isEmpty())text.append("Your camera supplies no usable image. The episode fills the gap with narration and a reconstructed shot.\n\n");
            if(seen.contains("Handholds"))text.append("The damaged plaster stays visible beside the wall reconstruction.\n\n");
            if(seen.contains("Playhouse"))text.append("The cardboard house collapses in your frame. The cut holds on the wreckage after the sisters have stopped speaking.\n\n");
            if(seen.contains("PrivateConfession"))text.append("The confession is filmed. Barry uses it as an explanation without the appeal to put the camera down.\n\n");
            if(seen.contains("FinalMeal"))text.append("The final meal remains on the tape after the programme has ended.\n\n");
            if(d==4&&own.getBoolean("RespectedPrivacy"))text.append("You put the camera away for part of the private conversation. The production calls the missing image a technical problem.\n\n");
        }
        return HouseWriting.book(raw?"Unedited camera log — day "+d:"Edited episode — day "+d,name,HouseWriting.WritingStyle.WILL,pages(text.toString()));
    }
    /** Conservative pages for the real 114-pixel lectern, retaining every word and explicit line break. */
    public static List<String> pages(String text){var result=new ArrayList<String>();for(int start=0;start<text.length();){int end=Math.min(text.length(),start+120),breaks=0;for(int i=start;i<end;i++)if(text.charAt(i)=='\n'&&++breaks>4){end=i;break;}if(end<text.length()){int word=text.lastIndexOf(' ',end-1);if(word>start+40)end=word+1;}result.add(text.substring(start,end));start=end;}return result.isEmpty()?List.of(""):result;}
    public static String account(String name,CompoundTag own) {
        var text=new StringBuilder("THE TAPE AND THE CUT\n\nI returned on seven filming days. The lens could keep one face while another person spoke. The production could make a silence last longer than it had.\n\n");
        for(int d=1;d<=DAYS;d++){var book=edit(name,own,d,true).get(DataComponents.WRITTEN_BOOK_CONTENT);for(var page:book.pages())text.append(page.raw().getString());text.append("\n\n");}
        text.append("MY ACCOUNT\n\n").append(own.getString("Confession")).append("\n\nThe record does not tell me whether Marjorie was possessed. It tells me what I kept looking at.");return text.toString();
    }
    private static void display(ServerPlayer p,CompoundTag own,BlockPos b) {
        if(finished(own))LiteraryVignettes.ready(p,PLACE,own,"completed_the_filming_days_and_read_own_cut");
        var model=new CompoundTag();var frames=own.getList("FramesDay"+day(own),Tag.TAG_COMPOUND);
        if(!frames.isEmpty()){int n=(p.server.getTickCount()/60)%frames.size();model.putIntArray("Screen",frames.getCompound(n).getIntArray("Pixels"));}
        model.putUUID("Reader",p.getUUID());model.putInt("GhostDay",day(own));model.putLong("GhostBase",b.asLong());boolean frozen=day(own)==6&&own.getBoolean("JumpFrozen0488")&&!own.getBoolean("DayDone");model.putBoolean("GhostFreeze",frozen);model.putInt("GhostFreezeTarget",frozen?own.getInt("GhostFreezeTarget"):-1);for(String key:List.of("GhostEyeX","GhostEyeY","GhostEyeZ","GhostYaw","GhostPitch"))if(own.contains(key))model.put(key,own.get(key).copy());int voice=(int)Math.max(0,Math.min(140,own.getLong("GhostVoiceUntil")-p.level().getGameTime()));model.putInt("GhostVoiceRemaining",voice);if(voice>0){model.putString("GhostVoice",own.getString("GhostVoice"));model.putInt("GhostVoiceId",own.getInt("GhostVoiceId"));}
        model.putBoolean("ScreenDark",frames.isEmpty());HousePackets.send(p,new LiteraryViewPayload(-1,20,0,model));
    }
    @SubscribeEvent public static void finishJump(ServerTickEvent.Post event){for(var p:event.getServer().getPlayerList().getPlayers()){if(!LiteraryVignettes.inside(p,PLACE)||LabyrinthDoors.isBusy(p))continue;var data=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(data,p.getUUID(),PLACE);if(!own.getBoolean("JumpEject0488")||!own.getBoolean("DayDone"))continue;if(LabyrinthDoors.finishTakeAndReturn(p,PLACE)){own=LiteraryVignettes.personal(data,p.getUUID(),PLACE);own.remove("JumpEject0488");LiteraryVignettes.save(data,p.getUUID(),PLACE,own);}}}
    private static boolean finished(CompoundTag own){return day(own)==DAYS&&own.getBoolean("DayDone")&&own.getInt("FilmedDays")==((1<<DAYS)-1)&&own.contains("Confession");}
    private static void requestFrame(ServerPlayer p,CompoundTag own) {
        var frames=own.getList("FramesDay"+day(own),Tag.TAG_COMPOUND);if(frames.size()>=4||own.hasUUID("FrameNonce")&&p.server.getTickCount()-own.getInt("FrameTick")<60)return;
        UUID nonce=UUID.randomUUID();own.putUUID("FrameNonce",nonce);own.putInt("FrameTick",p.server.getTickCount());own.putInt("FrameDay",day(own));own.putDouble("FrameX",p.getX());own.putDouble("FrameY",p.getY());own.putDouble("FrameZ",p.getZ());own.putFloat("FrameYaw",p.getYRot());own.putFloat("FramePitch",p.getXRot());HousePackets.send(p,new CamcorderExposurePayload(nonce));
    }
    public static boolean receiveFrame(ServerPlayer p,UUID nonce,int[] pixels) {
        if(pixels.length!=384||!recording(p))return false;var data=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(data,p.getUUID(),PLACE);
        if(!own.hasUUID("FrameNonce")||!own.getUUID("FrameNonce").equals(nonce)||p.server.getTickCount()-own.getInt("FrameTick")>60||day(own)!=own.getInt("FrameDay"))return false;
        if(p.position().distanceToSqr(new Vec3(own.getDouble("FrameX"),own.getDouble("FrameY"),own.getDouble("FrameZ")))>.25||Math.abs(net.minecraft.util.Mth.wrapDegrees(p.getYRot()-own.getFloat("FrameYaw")))>8||Math.abs(p.getXRot()-own.getFloat("FramePitch"))>8)return false;
        var frames=own.getList("FramesDay"+day(own),Tag.TAG_COMPOUND);if(frames.size()>=4)return false;var frame=new CompoundTag();var bounded=pixels.clone();for(int i=0;i<bounded.length;i++)bounded[i]&=0xffffff;frame.putIntArray("Pixels",bounded);frames.add(frame);own.put("FramesDay"+day(own),frames);own.remove("FrameNonce");LiteraryVignettes.save(data,p.getUUID(),PLACE,own);return true;
    }
    private static AABB area(BlockPos b) { return new AABB(Vec3.atLowerCornerOf(b.offset(-21,-5,-47)),Vec3.atLowerCornerOf(b.offset(22,13,1))); }
    private static boolean vacant(ServerLevel l,BlockPos b) {
        var area=area(b);for(int x=(int)area.minX>>4;x<=(int)area.maxX>>4;x++)for(int z=(int)area.minZ>>4;z<=(int)area.maxZ>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
        if(l.players().stream().anyMatch(p->area.inflate(24).intersects(p.getCamera().getBoundingBox())))return false;
        return l.getEntitiesOfClass(Entity.class,area,e->e.isAlive()&&!(e instanceof LiteraryActor)).isEmpty();
    }
    public static boolean rebuild(ServerLevel l,BlockPos b) {
        var data=LabyrinthData.get(l.getServer());if(built(data,b))return true;if(!vacant(l,b))return false;
        var plan=REPAIRS.get(l);if(plan==null){
            // Retain every actual block entity, including the source and depleted/customized containers, in its original cell.
            var originals=new HashMap<BlockPos,net.minecraft.world.level.block.entity.BlockEntity>();
            for(var at:BlockPos.betweenClosed(b.offset(-21,-1,-47),b.offset(21,12,0)))if(l.getBlockEntity(at)!=null)originals.put(at.immutable(),l.getBlockEntity(at));
            var legacy=BuildBlocks.record(l,()->{BuildBlocks.box(l,b.offset(-21,-1,-47),b.offset(21,12,0),Blocks.AIR.defaultBlockState(),Block.UPDATE_SUPPRESS_DROPS);LiteraryRooms.legacyGhostSet(l,b);});
            var keep=new HashSet<BlockPos>(originals.keySet());
            // A state unlike the old authored set is retained, including a removed original (air), custom construction,
            // and older decoration outside that template. This conservative upgrade cannot reconstruct a player's edits.
            var checkpoint=data.state(STATE);String repairKey="Repair_"+b.asLong();
            if(checkpoint.contains(repairKey,Tag.TAG_LONG_ARRAY)){for(long at:checkpoint.getLongArray(repairKey))keep.add(BlockPos.of(at));}
            else for(var at:BlockPos.betweenClosed(b.offset(-21,-1,-47),b.offset(21,12,0))){var expected=legacy.plannedState(at);if(expected!=null&&!l.getBlockState(at).equals(expected))keep.add(at.immutable());}
            checkpoint.putLongArray(repairKey,keep.stream().mapToLong(BlockPos::asLong).toArray());data.setState(STATE,checkpoint);REPAIR_CELLS.put(l,keep);
            plan=BuildBlocks.record(l,()->{
                for(var at:BlockPos.betweenClosed(b.offset(-19,0,-45),b.offset(19,10,-1)))if(!keep.contains(at))BuildBlocks.protectedSet(l,at,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS,()->vacant(l,b),keep);
                GhostsSetRooms.repair(l,b,keep,()->vacant(l,b));
            });REPAIRS.put(l,plan);
        }
        if(plan.tick()){REPAIRS.remove(l);REPAIR_CELLS.remove(l);markBuilt(data,b);return true;}return false;
    }
    private static void protectEdit(ServerLevel l,BlockPos at){var keep=REPAIR_CELLS.get(l);if(keep==null)return;keep.add(at.immutable());var d=LabyrinthData.get(l.getServer());var state=d.state(STATE);for(var key:state.getAllKeys())if(key.startsWith("Repair_"))state.putLongArray(key,keep.stream().mapToLong(BlockPos::asLong).toArray());d.setState(STATE,state);}
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void placed(net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent e){if(!e.isCanceled()&&e.getLevel() instanceof ServerLevel l&&e.getEntity() instanceof ServerPlayer p&&LiteraryVignettes.inside(p,PLACE))protectEdit(l,e.getPos());}
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void broken(net.neoforged.neoforge.event.level.BlockEvent.BreakEvent e){if(!e.isCanceled()&&e.getLevel() instanceof ServerLevel l&&e.getPlayer() instanceof ServerPlayer p&&LiteraryVignettes.inside(p,PLACE))protectEdit(l,e.getPos());}
    @SubscribeEvent public static void repairs(ServerTickEvent.Post e) {
        var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%20!=7||LabyrinthBuilder.isCarving())return;
        var origin=HouseSavedData.get(s).houseOrigin();if(origin==null)return;var data=LabyrinthData.get(s);if(!LabyrinthBuilder.isPlaceReady(data,PLACE))return;
        var b=LabyrinthPlaces.base(origin,PLACE);var l=s.getLevel(NovelRooms.dimension(PLACE));if(l!=null&&!built(data,b))rebuild(l,b);
    }
}
