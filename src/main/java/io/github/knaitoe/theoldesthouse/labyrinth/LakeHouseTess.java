package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;

/** A private partner for a genuinely solo visit: Tess, or the young man in her place (LakeHousePartner). Departure is physical when the reader keeps watching. */
public final class LakeHouseTess {
    private LakeHouseTess(){}
    public static @Nullable LiteraryActor actor(ServerPlayer p,CompoundTag own){return own.hasUUID("Tess")&&p.serverLevel().getEntity(own.getUUID("Tess")) instanceof LiteraryActor a&&!a.isRemoved()?a:null;}
    public static int readers(ServerPlayer p,BlockPos b){var bounds=IndianLakeRooms.bounds(b,LakeHouse.PLACE);return (int)p.serverLevel().players().stream().filter(q->q.isAlive()&&!q.isSpectator()&&bounds.contains(q.position())).count();}
    private static @Nullable LiteraryActor ensure(ServerPlayer p,BlockPos b,CompoundTag own){
        var a=actor(p,own);if(a!=null)return a;
        if(own.hasUUID("Tess"))return null; // Its saved native body may be in an unloaded entity section.
        var staging=own.getInt("Visit")==2?b.offset(LakeHouse.COMPRESSOR):b.offset(3,0,-16);if(!p.serverLevel().hasChunkAt(staging)||!p.serverLevel().areEntitiesLoaded(new net.minecraft.world.level.ChunkPos(staging).toLong()))return null;
        // Recover a loaded original before creating a body; a reconnect never adds a second Tess.
        for(var original:p.serverLevel().getEntitiesOfClass(LiteraryActor.class,IndianLakeRooms.bounds(b,LakeHouse.PLACE),q->LiteraryActor.lakePartner(q.role())&&q.owner().filter(p.getUUID()::equals).isPresent())){own.putUUID("Tess",original.getUUID());return original;}
        a=LiteraryRegistry.ACTOR.get().create(p.serverLevel());if(a==null)return null;
        a.appearance(LakeHousePartner.of(own).role,0);a.bind(LakeHouse.PLACE,p.getUUID());a.setNoGravity(true);a.setCustomNameVisible(false);
        var spot=own.getInt("Visit")==2?b.offset(LakeHouse.COMPRESSOR.offset(1,0,0)):b.offset(3,0,-16);a.moveTo(spot.getX()+.5,spot.getY(),spot.getZ()+.5,180,0);
        if(!p.serverLevel().addFreshEntity(a))return null;own.putUUID("Tess",a.getUUID());return a;
    }
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
    /** Tess slipped away because a reader came down in her place this visit (0.4.156). */
    private static final String STOOD_IN="PartnerStoodIn0156";
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        var a=actor(p,own);int count=readers(p,b);boolean ended=own.getBoolean("Ready");
        // A partner who leaves partway through a visit leaves the diver alone: Tess comes back for the rest of it, out of sight.
        if(own.getBoolean(STOOD_IN)&&count==1&&a==null&&own.getInt("TessLeaving")==0&&!own.getBoolean("GoodbyeDone")){
            var staging=own.getInt("Visit")==2?b.offset(LakeHouse.COMPRESSOR):b.offset(3,0,-16);
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
            if(t>=60){a.appearance(a.role(),6);var relative=a.position().subtract(Vec3.atLowerCornerOf(b));var to=LakeHouseDive.inHouse(relative)?new Vec3(b.getX()+.5,b.getY()-24,b.getZ()-159.5):a.getY()<b.getY()+.1?new Vec3(a.getX(),b.getY()+.2,a.getZ()):new Vec3(b.getX()+48,b.getY()+1,b.getZ()-148);move(a,to,.7);}
            return;
        }
        if(count!=1||own.getBoolean("SoloGone")||own.getBoolean("GoodbyeDone")||ended&&!goodbye)return;
        a=ensure(p,b,own);if(a==null)return;
        if(a.getPersistentData().getLong("LakeSayUntil")<p.level().getGameTime()){a.setCustomNameVisible(false);a.setCustomName(null);}
        if(goodbye){
            a.stopRiding();a.appearance(a.role(),0);move(a,new Vec3(b.getX()+3.5,b.getY(),b.getZ()-16.5),.5);
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
        if(p.getVehicle() instanceof LakeHouseCanoe canoe){
            if(a.getVehicle()!=canoe){a.stopRiding();a.startRiding(canoe);}a.appearance(a.role(),1);return;
        }
        a.stopRiding();
        if(own.getInt("Visit")==2){a.appearance(a.role(),4);move(a,Vec3.atBottomCenterOf(b.offset(LakeHouse.COMPRESSOR.offset(1,0,0))),.65);return;}
        var r=p.position().subtract(Vec3.atLowerCornerOf(b));Vec3 to;
        if(p.isEyeInFluid(net.minecraft.tags.FluidTags.WATER)){
            a.appearance(a.role(),2);
            var ar=a.position().subtract(Vec3.atLowerCornerOf(b));
            // Swim via the real foyer and central stair, rather than crossing a partition to follow a reader.
            var mouth=new Vec3(.5,-24,-159.5);
            if(LakeHouseDive.inHouse(r)&&!LakeHouseDive.inHouse(ar))to=ar.z>-160.1&&(Math.abs(ar.x-.5)>.5||Math.abs(ar.y+24)>1)?mouth:new Vec3(.5,-24,-163.5);
            else to=r.add(-1.7,.1,1.4);
            if(own.getInt("Visit")>=4&&own.getBoolean("Lit"))to=new Vec3(.5,-18.8,-164.5);
        }else{a.appearance(a.role(),0);to=r.add(2,0,1);}
        move(a,to.add(b.getX(),b.getY(),b.getZ()),.55);
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
