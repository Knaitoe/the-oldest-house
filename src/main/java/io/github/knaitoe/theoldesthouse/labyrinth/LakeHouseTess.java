package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;

/**
 * A private partner for a genuinely solo visit: Tess, or the young man in her place (LakeHousePartner). Departure is physical when
 * the reader keeps watching.
 * <p>0.4.560 (owner: "also ensure the npc behaves correctly in that vignette, please - tess"): she moves a tick at a time; she
 * climbs into the reader's canoe from beside it, going to it first, and never from across the lake; on the helmet visit she stays
 * on the raft with the compressor; she walks where there is ground and swims where there is water, never standing on it; out of
 * the canoe she steps onto what is beside it or slips over the side; she ducks under the culvert's roof; and when she is not going
 * anywhere she turns to her reader, more quickly while she is speaking. The standard cast checks (gametest.CastConduct) hold her
 * to all of it.
 */
public final class LakeHouseTess {
    private LakeHouseTess(){}
    /**
     * Where she waits for the reader, except on the helmet visit: 0.4.560, on the dock's planks by the canoes, where before she stood
     * on the water two blocks off its edge; and the boathouse, where she comes from when the reader is looking at the dock.
     */
    static final BlockPos DOCK=new BlockPos(0,0,-20),BOATHOUSE=new BlockPos(0,0,-6);
    /** 0.4.560: how near the canoe she has to be to climb in. */
    static final double BOARD=2.6;
    /** How far under the surface she swims there: a swimmer's body lies in the six-tenths of a block above her feet. */
    static final double SWIM_DEPTH=.4;
    /** The places beside the reader she may take, in blocks across and along. */
    private static final double[][] BESIDE={{2,1},{-2,1},{1,2},{-1,2},{2,-1},{-2,-1},{0,2},{1,-2},{-1,-2},{2,0},{-2,0}};
    public static @Nullable LiteraryActor actor(ServerPlayer p,CompoundTag own){return own.hasUUID("Tess")&&p.serverLevel().getEntity(own.getUUID("Tess")) instanceof LiteraryActor a&&!a.isRemoved()?a:null;}
    public static int readers(ServerPlayer p,BlockPos b){var bounds=IndianLakeRooms.bounds(b,LakeHouse.PLACE);return (int)p.serverLevel().players().stream().filter(q->q.isAlive()&&!q.isSpectator()&&bounds.contains(q.position())).count();}
    private static @Nullable LiteraryActor ensure(ServerPlayer p,BlockPos b,CompoundTag own){
        var a=actor(p,own);if(a!=null)return a;
        if(own.hasUUID("Tess"))return null; // Its saved native body may be in an unloaded entity section.
        var staging=own.getInt("Visit")==2?b.offset(LakeHouse.COMPRESSOR):b.offset(DOCK);if(!p.serverLevel().hasChunkAt(staging)||!p.serverLevel().areEntitiesLoaded(new net.minecraft.world.level.ChunkPos(staging).toLong()))return null;
        // Recover a loaded original before creating a body; a reconnect never adds a second Tess.
        for(var original:p.serverLevel().getEntitiesOfClass(LiteraryActor.class,IndianLakeRooms.bounds(b,LakeHouse.PLACE),q->LiteraryActor.lakePartner(q.role())&&q.owner().filter(p.getUUID()::equals).isPresent())){own.putUUID("Tess",original.getUUID());return original;}
        // 0.4.560: she never comes into being where the reader can see. She is there already when they reach the dock (or the raft),
        // or comes out of the boathouse behind them; while every way in is in their sight, she waits to come.
        Vec3 spot=null;
        for(var c:own.getInt("Visit")==2?java.util.List.of(start(b,own)):java.util.List.of(start(b,own),Vec3.atBottomCenterOf(b.offset(BOATHOUSE))))
            if(!HouseWatchers.sees(p,c.add(0,1,0))&&!HouseWatchers.sees(p,c.add(0,.2,0))&&p.position().distanceToSqr(c)>4){spot=c;break;}
        if(spot==null)return null;
        a=LiteraryRegistry.ACTOR.get().create(p.serverLevel());if(a==null)return null;
        a.appearance(LakeHousePartner.of(own).role,0);a.bind(LakeHouse.PLACE,p.getUUID());a.setNoGravity(true);a.setCustomNameVisible(false);
        a.moveTo(spot.x,spot.y,spot.z,180,0);
        if(!p.serverLevel().addFreshEntity(a))return null;own.putUUID("Tess",a.getUUID());own.putBoolean(PLACED,true);return a;
    }
    /** 0.4.560: whether this visit has had her put where it starts her, if her body was elsewhere. */
    static final String PLACED="TessPlaced0560";
    /** Where a visit starts her: by the compressor on the raft on the helmet visit, else on the dock. */
    static Vec3 start(BlockPos b,CompoundTag own){return Vec3.atBottomCenterOf(own.getInt("Visit")==2?b.offset(LakeHouse.COMPRESSOR.offset(1,0,0)):b.offset(DOCK));}
    private static boolean visible(ServerPlayer p,LiteraryActor a){
        var camera=p.getCamera();var eye=camera.getEyePosition();var d=a.getEyePosition().subtract(eye);double distance=d.length();
        if(distance>256)return false;if(distance<2)return true;if(camera.getLookAngle().dot(d.scale(1/distance))<.18)return false;
        var hit=p.serverLevel().clip(new net.minecraft.world.level.ClipContext(eye,a.getEyePosition(),net.minecraft.world.level.ClipContext.Block.VISUAL,net.minecraft.world.level.ClipContext.Fluid.NONE,camera));return hit.getType()==net.minecraft.world.phys.HitResult.Type.MISS||hit.getBlockPos().distManhattan(a.blockPosition())<=1;
    }
    public static boolean minds(ServerPlayer p){
        var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),LakeHouse.PLACE);var a=actor(p,own);var b=LakeHouse.readerBase(p);
        return b!=null&&!own.getBoolean("SoloGone")&&a!=null&&a.phase()==4&&a.distanceToSqr(b.offset(LakeHouse.COMPRESSOR).getCenter())<16;
    }
    public static void line(ServerPlayer p,@Nullable LiteraryActor a,String text){
        // 0.4.156: with a reader in Tess's place, her line is theirs to say.
        var mate=LakeHousePairs.partnerHere(p);if(mate!=null){LakeHousePairs.prompt(mate,text);return;}
        var partner=LakeHousePartner.of(LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),LakeHouse.PLACE));
        if(p.isEyeInFluid(net.minecraft.tags.FluidTags.WATER)){
            p.displayClientMessage(Component.literal(LakeHousePartner.mouths(partner)).withStyle(ChatFormatting.ITALIC),true);if(a!=null)a.appearance(a.role(),3);return;
        }
        p.displayClientMessage(Component.literal(partner.name+": “"+text+"”"),false);
        if(a!=null){a.say(text);a.getPersistentData().putLong("LakeSayUntil",p.level().getGameTime()+100);}
    }
    private static void move(LiteraryActor a,Vec3 to,double step){
        LakeHouseSwimNavigation.move(a,to,step);
    }
    /** Ground to stand on near a point: a floor with room for her over it and no water in it; the top of the floor, or none. */
    static @Nullable Vec3 ground(Level l,double x,double y,double z){
        for(int dy=1;dy>=-2;dy--){
            var feet=BlockPos.containing(x,y+dy,z);var below=feet.below();var floor=l.getBlockState(below).getCollisionShape(l,below);
            if(floor.isEmpty())continue;
            if(!l.getFluidState(feet).isEmpty()||!l.getFluidState(feet.above()).isEmpty())continue;
            if(!l.getBlockState(feet).getCollisionShape(l,feet).isEmpty()||!l.getBlockState(feet.above()).getCollisionShape(l,feet.above()).isEmpty())continue;
            return new Vec3(feet.getX()+.5,below.getY()+floor.max(net.minecraft.core.Direction.Axis.Y),feet.getZ()+.5);
        }
        return null;
    }
    /** The top of the water in a column near a height, or none. */
    static @Nullable Double surface(Level l,double x,double y,double z){
        for(int dy=1;dy>=-3;dy--){
            var at=BlockPos.containing(x,y+dy,z);var water=l.getFluidState(at);
            if(water.is(FluidTags.WATER)&&!l.getFluidState(at.above()).is(FluidTags.WATER))return at.getY()+(double)water.getHeight(l,at);
        }
        return null;
    }
    /** Whether she is in the water with nothing under her to stand on: then she swims. */
    static boolean swimming(LiteraryActor a){return a.isInWater()&&ground(a.level(),a.getX(),a.getY(),a.getZ())==null;}
    /** A place beside someone: on ground near them if there is any (the nearest such place to her), else in the water at its surface. */
    private static @Nullable Vec3 beside(LiteraryActor a,Vec3 at){
        Vec3 best=null;double nearest=Double.MAX_VALUE;
        for(var o:BESIDE){
            var g=ground(a.level(),at.x+o[0],at.y+.1,at.z+o[1]);if(g==null||Math.abs(g.y-at.y)>1.1)continue;
            double d=g.distanceToSqr(a.position());if(d<nearest){nearest=d;best=g;}
        }
        if(best!=null)return best;
        var swim=EntityDimensions.scalable(.6F,.6F);
        for(var o:BESIDE){
            double x=at.x+o[0]*.8,z=at.z+o[1]*.8;var top=surface(a.level(),x,at.y,z);if(top==null)continue;
            var spot=new Vec3(x,top-SWIM_DEPTH,z);if(!a.level().noCollision(a,swim.makeBoundingBox(spot)))continue;
            double d=spot.distanceToSqr(a.position());if(d<nearest){nearest=d;best=spot;}
        }
        return best;
    }
    /** Whether a point is in the water rather than on ground. */
    private static boolean wet(Level l,Vec3 at){return surface(l,at.x,at.y,at.z)!=null&&ground(l,at.x,at.y,at.z)==null;}
    /** From where she stands into the water beside her, a step down; true if she went in. */
    private static boolean slipIn(LiteraryActor a){
        var swim=EntityDimensions.scalable(.6F,.6F);
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){
            if(dx==0&&dz==0)continue;
            double x=a.getX()+dx*1.1,z=a.getZ()+dz*1.1;var top=surface(a.level(),x,a.getY(),z);
            if(top==null||top>a.getY()+.3||top<a.getY()-1.5)continue;
            var spot=new Vec3(x,top-SWIM_DEPTH,z);if(!a.level().noCollision(a,swim.makeBoundingBox(spot)))continue;
            a.appearance(a.role(),2);a.moveTo(spot.x,spot.y,spot.z,a.getYRot(),0);return true;
        }
        return false;
    }
    /** She walks on ground and swims in water; going where the water is, she steps down into it from the edge she reaches. */
    private static void stride(LiteraryActor a,Vec3 toward){
        if(!a.isInWater()&&wet(a.level(),toward)&&slipIn(a))return;
        a.appearance(a.role(),swimming(a)?2:0);
    }
    /**
     * 0.4.560: on foot in the boathouse or on the dock, with somewhere to be out on the water, she goes out through the boathouse's
     * lake door and down the middle of the boards to the place on them nearest it, and steps down into the water only from there.
     */
    private static Vec3 landward(LiteraryActor a,BlockPos b,Vec3 to){
        if(a.isInWater())return to;
        var r=a.position().subtract(Vec3.atLowerCornerOf(b));var t=to.subtract(Vec3.atLowerCornerOf(b));
        boolean boathouse=r.z>-9.3&&r.z<-.5&&Math.abs(r.x-.5)<5.5,dock=r.z<=-9.3&&r.z>LakeHouse.DOCK_END.getZ()-.2&&Math.abs(r.x-.5)<1.6;
        if(!(boathouse||dock)||Math.abs(r.y)>.6)return to;
        // In the boathouse with somewhere to be outside it: to its middle first, in line with the lake door.
        if(boathouse&&t.z<-9.3&&Math.abs(r.x-.5)>.9)return new Vec3(b.getX()+.5,a.getY(),a.getZ());
        if(!wet(a.level(),to))return to;
        var end=new Vec3(b.getX()+Mth.clamp(t.x,-.5,1.5),a.getY(),b.getZ()+Mth.clamp(t.z,LakeHouse.DOCK_END.getZ()+.5,-9.8));
        return a.position().distanceToSqr(end)<.09?to:end;
    }
    /** 0.4.560: out of the canoe onto what is beside it to stand on, or over its side into the water. */
    private static void leave(LiteraryActor a,LakeHouseCanoe canoe){
        a.stopRiding();CanoeRiderPose.duck(a,false);LakeHouseSwimNavigation.forget(a);
        var spot=beside(a,canoe.position());
        if(spot!=null&&spot.distanceToSqr(canoe.position())<9){if(wet(a.level(),spot))a.appearance(a.role(),2);a.moveTo(spot.x,spot.y,spot.z,a.getYRot(),0);}
        stride(a,a.position());
    }
    /**
     * Every tick, on the server (0.4.560): she moves a tick at a time; and when she is not going anywhere she turns her head to her
     * reader, and herself as well if they are behind her and she is standing, quickly while she is speaking.
     */
    public static void tick(LiteraryActor a){
        LakeHouseSwimNavigation.tick(a);
        if(a.isPassenger()||LakeHouseSwimNavigation.moving(a)||!(a.level() instanceof ServerLevel l))return;
        var reader=a.owner().map(l.getServer().getPlayerList()::getPlayer).orElse(null);
        if(reader==null||reader.level()!=l||a.distanceToSqr(reader)>100)return;
        var d=reader.getEyePosition().subtract(a.getEyePosition());float yaw=(float)Math.toDegrees(Math.atan2(-d.x,d.z));
        float turn=a.saying()?14:5;
        if(a.phase()==0&&Math.abs(Mth.wrapDegrees(yaw-a.yBodyRot))>60){a.yBodyRot=Mth.approachDegrees(a.yBodyRot,yaw,turn*.6F);a.setYRot(a.yBodyRot);}
        float head=Mth.approachDegrees(a.getYHeadRot(),yaw,turn);
        a.setYHeadRot(a.yBodyRot+Mth.clamp(Mth.wrapDegrees(head-a.yBodyRot),-70,70));
    }
    /** Tess slipped away because a reader came down in her place this visit (0.4.156). */
    private static final String STOOD_IN="PartnerStoodIn0156";
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        var a=actor(p,own);int count=readers(p,b);boolean ended=own.getBoolean("Ready");
        // A partner who leaves partway through a visit leaves the diver alone: Tess comes back for the rest of it, out of sight.
        if(own.getBoolean(STOOD_IN)&&count==1&&a==null&&own.getInt("TessLeaving")==0&&!own.getBoolean("GoodbyeDone")){
            var staging=own.getInt("Visit")==2?b.offset(LakeHouse.COMPRESSOR):b.offset(DOCK);
            if(!HouseWatchers.sees(p,staging.getCenter())){own.putBoolean("SoloGone",false);own.putBoolean(STOOD_IN,false);}
        }
        boolean goodbye=ended&&own.getInt("Visit")>own.getInt("EndVisit")&&!own.getBoolean("GoodbyeDone");
        if(count>1&&a!=null&&!own.getBoolean("SoloGone")){
            // A reader in Tess's place (0.4.156) means Tess slips away without a word.
            boolean paired=LakeHousePairs.partnerHere(p)!=null;
            own.putBoolean("SoloGone",true);own.putBoolean(STOOD_IN,paired);own.putInt("TessLeaving",5);if(!paired)line(p,a,"I'll watch the compressor.");a.stopRiding();
        }
        // A partner already there when the visit starts means Tess never comes at all.
        if(a==null&&!own.getBoolean("SoloGone")&&LakeHousePairs.partnerHere(p)!=null){own.putBoolean("SoloGone",true);own.putBoolean(STOOD_IN,true);}
        if(own.getInt("TessLeaving")>0&&a!=null){
            int t=own.getInt("TessLeaving")+5;own.putInt("TessLeaving",t);
            if(!visible(p,a)){LakeHouseSwimNavigation.forget(a);a.discard();own.remove("Tess");own.putInt("TessLeaving",0);return;}
            if(t>=60){
                // 0.4.560: she swims away in the water, under it out through the house's door, and walks off along the dock or
                // off the raft into the lake; she never swims through the air.
                a.stopRiding();CanoeRiderPose.duck(a,false);
                var relative=a.position().subtract(Vec3.atLowerCornerOf(b));double surface=b.getY()-.11-SWIM_DEPTH;
                boolean dock=relative.z>LakeHouse.DOCK_END.getZ()-1&&relative.z<-2&&!a.isInWater();
                boolean wet=a.isInWater()||!dock&&slipIn(a);a.appearance(a.role(),wet?6:0);
                Vec3 to;
                if(LakeHouseDive.inHouse(relative))to=new Vec3(b.getX()+.5,b.getY()-24,b.getZ()-159.5);
                else if(dock)to=new Vec3(b.getX()+.5,b.getY(),b.getZ()-2.5);
                else if(relative.y<-1.5)to=new Vec3(a.getX(),surface,a.getZ());
                else to=relative.z>LakeHouse.CHANNEL_SOUTH?new Vec3(b.getX()+20,surface,b.getZ()-60):new Vec3(b.getX()+28,surface,b.getZ()-160);
                move(a,to,.7);
            }
            return;
        }
        if(count!=1||own.getBoolean("SoloGone")||own.getBoolean("GoodbyeDone")||ended&&!goodbye)return;
        a=ensure(p,b,own);if(a==null)return;
        // 0.4.560: a body kept from an earlier visit is where that visit left it. This visit starts her on the dock, or by the
        // compressor on the helmet visit; she is put there the first moment neither she nor the place is in the reader's sight.
        if(!own.getBoolean(PLACED)){
            var spot=start(b,own);
            if(a.position().distanceToSqr(spot)<4)own.putBoolean(PLACED,true);
            else if(!visible(p,a)&&!HouseWatchers.sees(p,spot.add(0,1,0))){a.stopRiding();CanoeRiderPose.duck(a,false);LakeHouseSwimNavigation.forget(a);a.appearance(a.role(),0);a.moveTo(spot.x,spot.y,spot.z,180,0);own.putBoolean(PLACED,true);}
        }
        if(!a.saying()){a.setCustomNameVisible(false);a.setCustomName(null);}
        if(goodbye){
            a.stopRiding();stride(a,Vec3.atBottomCenterOf(b.offset(DOCK)));move(a,Vec3.atBottomCenterOf(b.offset(DOCK)),.5);
            if(p.distanceToSqr(a)<36&&!p.isEyeInFluid(net.minecraft.tags.FluidTags.WATER)&&HouseWatchers.sees(p,a.getEyePosition())){
                if(!own.getBoolean("GoodbyeSaid")){own.putBoolean("GoodbyeSaid",true);line(p,a,switch(own.getString("Outcome")){case LakeHouse.LEFT_THE_LIGHTS_ON->"It's still lit, isn't it. I'm not going back down. I don't think you should either.";case LakeHouse.STAYED->"You didn't come up. I sat on that raft until it got light. I can't do that again.";default->"I came out here twice on my own. Just sat in the canoe and looked down. There's nothing there. I have to stop coming.";});}
                own.putInt("GoodbyeWait",own.getInt("GoodbyeWait")+5);
                if(own.getInt("GoodbyeWait")>=120){own.putBoolean("GoodbyeDone",true);own.putBoolean("SoloGone",true);own.putInt("TessLeaving",60);}
            }return;
        }
        if(!own.getBoolean("TessGreeting")&&!p.isEyeInFluid(net.minecraft.tags.FluidTags.WATER)&&p.distanceToSqr(a)<100){
            own.putBoolean("TessGreeting",true);own.putBoolean(LakeHousePartner.MET,true);
            line(p,a,own.getInt("Visit")>=4?"Something was in my room last night. The floor was wet, and the water was warm, like that room by the pool. We have to go back and say hello.":own.getInt("Visit")==1?LakeHousePartner.firstGreeting(LakeHousePartner.of(own)):own.getInt("Visit")==2?"I'll keep the compressor going. Pull twice if you need me.":"The tanks are filled. Let's see what's upstairs. And there were bubbles coming out round the wardrobe in the study.");
        }
        // 0.4.560: she climbs into the reader's canoe from beside it, going to it first, on foot along the dock or swimming; never
        // from across the lake. On the helmet visit she keeps the compressor going, and stays on the raft.
        if(p.getVehicle() instanceof LakeHouseCanoe canoe&&own.getInt("Visit")!=2){
            if(a.getVehicle()==canoe){a.appearance(a.role(),1);return;}
            if(a.getVehicle() instanceof LakeHouseCanoe other)leave(a,other);
            if(a.distanceToSqr(canoe)<BOARD*BOARD&&canoe.getPassengers().size()<2){LakeHouseSwimNavigation.forget(a);if(a.startRiding(canoe)){a.appearance(a.role(),1);return;}}
            var way=landward(a,b,canoe.position());stride(a,way);move(a,way,a.distanceToSqr(canoe)>144?1.2:.7);return;
        }
        if(a.getVehicle() instanceof LakeHouseCanoe canoe)leave(a,canoe);
        a.stopRiding();
        if(own.getInt("Visit")==2){
            var post=Vec3.atBottomCenterOf(b.offset(LakeHouse.COMPRESSOR.offset(1,0,0)));
            if(a.position().distanceToSqr(post)<1.5)a.appearance(a.role(),4);else stride(a,post);
            move(a,post,.65);return;
        }
        var r=p.position().subtract(Vec3.atLowerCornerOf(b));Vec3 to;
        if(p.isEyeInFluid(net.minecraft.tags.FluidTags.WATER)){
            var ar=a.position().subtract(Vec3.atLowerCornerOf(b));
            // Swim via the real foyer and central stair, rather than crossing a partition to follow a reader.
            var mouth=new Vec3(.5,-24,-159.5);
            if(LakeHouseDive.inHouse(r)&&!LakeHouseDive.inHouse(ar))to=ar.z>-160.1&&(Math.abs(ar.x-.5)>.5||Math.abs(ar.y+24)>1)?mouth:new Vec3(.5,-24,-163.5);
            else to=r.add(-1.7,.1,1.4);
            if(own.getInt("Visit")>=4&&own.getBoolean("Lit"))to=new Vec3(.5,-18.8,-164.5);
            // 0.4.560: from the raft or the dock she goes in at the edge, rather than swimming through the air to the water.
            var target=to.add(b.getX(),b.getY(),b.getZ());if(!a.isInWater()&&!slipIn(a))a.appearance(a.role(),0);else a.appearance(a.role(),2);
            move(a,target,a.isInWater()?.55:.55);return;
        }
        // 0.4.560: beside the reader on whatever they stand on; where there is nothing near them to stand on, in the water beside
        // them, swimming. She never stands on the water, and she keeps up.
        var spot=beside(a,p.position());if(spot==null)return;spot=landward(a,b,spot);
        stride(a,spot);move(a,spot,a.distanceToSqr(p)>144?1.1:.55);
    }
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        if(!LiteraryActor.lakePartner(a.role()))return;
        int n=own.getInt("TessTalk");own.putInt("TessTalk",n+1);
        String[] lines=own.getBoolean("RuleBroken")?new String[]{"Couldn't it just be ours?","You heard it too. Someone walking, upstairs.","We should have left it where it was."}:own.getBoolean("Lit")?new String[]{"All of them. Every room.","There's somebody upstairs. I won't go in without you.","We came to say hello."}:own.getInt("Visit")==2?new String[]{"There's a staircase at the back. Red carpet, all the way up.","I saw bedrooms up there. Dresses too.","The hose catches on that rail. Swim back and lift it over."}
                :own.getInt("Visit")==3?new String[]{"The bubbles come out round the wardrobe in the study. Something's behind it.","I saw bedrooms up there. Dresses too.","Come up when you need to. It will still be there."}
                :own.getInt("Visit")>=4?new String[]{"The water on my floor was warm. Like that room by the pool.","We came to say hello.","Don't ask how. I mean it."}:new String[]{"Everything's still where somebody put it.","Don't ask how. I mean it.","Come up when you need to. It will still be there."};
        line(p,a,lines[n%lines.length]);
    }
}
