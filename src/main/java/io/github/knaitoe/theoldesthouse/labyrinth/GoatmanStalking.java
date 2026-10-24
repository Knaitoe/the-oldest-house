package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.*;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.entity.Entity;

/** One saved cousin body circles the house. Only its presentation changes, between unseen sightings. */
public final class GoatmanStalking {
    private static final Vec3[] CIRCUIT={
        new Vec3(.5,.5,-52.6),new Vec3(.5,0,-51.5),new Vec3(-5.5,0,-51.5),
        new Vec3(-5.5,0,-53.5),new Vec3(-9.25,0,-54.5),new Vec3(-8.5,1,-58.5),
        new Vec3(-9.25,0,-66.5),new Vec3(-8.5,1,-72.5),new Vec3(-9.25,0,-78.5),
        new Vec3(.5,0,-78.5),new Vec3(10.25,0,-78.5),new Vec3(10.25,0,-74.5),
        new Vec3(10.25,0,-66.5),new Vec3(9.5,1,-58.5),new Vec3(10.25,0,-53.5),
        new Vec3(10.25,0,-48.5),new Vec3(.5,0,-48.5),new Vec3(.5,0,-51.5),new Vec3(.5,.5,-52.6),new Vec3(.5,1,-53.8)
    };
    private static final Vec3[] TO_WINDOW={new Vec3(.5,.5,-52.6),new Vec3(.5,0,-51.5),new Vec3(.5,0,-48.5),new Vec3(10.25,0,-48.5),new Vec3(10.25,0,-53.5),new Vec3(10.25,0,-66.5),new Vec3(10.25,0,-74.5)};
    public static boolean windowApproachDone(CompoundTag run){return run.getInt("StalkWindowLeg0465")>=TO_WINDOW.length;}
    public static final int[] DEMAND_TICKS={0,160,330,500,665,830,1000};
    public static final int ASSAULT_END=1080;
    public static final int CIRCUITS=3;
    public static boolean circuitDone(CompoundTag r){return r.getInt("StalkLeg0465")>=CIRCUIT.length
            &&(!r.contains("CircuitGoal0494")||r.getInt("Circuits0494")>=r.getInt("CircuitGoal0494"))
            &&(!r.contains("WindowSerial0497")||r.getBoolean("DoorPressureDone0497"));}
    private GoatmanStalking(){}
    private static Vec3 abs(BlockPos b,Vec3 p){return p.add(b.getX(),b.getY(),b.getZ());}
    /** The deck is consumed only when an appearance actually changes; exhaustion never recycles a child. */
    public static int nextChild(CompoundTag r){
        int used=r.getInt("StalkUsed0465"),cursor=r.getInt("StalkCursor0465");int[] deck=r.getIntArray("StalkDeck0465");
        while(cursor<deck.length){int skin=deck[cursor++];if(skin>=0&&skin<9&&(used&(1<<skin))==0){r.putInt("StalkCursor0465",cursor);r.putInt("StalkUsed0465",used|(1<<skin));return skin;}}
        r.putInt("StalkCursor0465",cursor);return -2;
    }
    public static boolean obscured(ServerLevel l,GoatmanChild c){
        for(var p:l.players()){
            Entity camera=p.getCamera();var eye=camera.getEyePosition();
            // Include the taller skull/horns before changing either presentation, and the actual spectator camera.
            for(double y:new double[]{.15,.8,1.3,2.0,2.65})for(double x:new double[]{-.3,0,.3}){
                var point=c.position().add(x,y,0);var to=point.subtract(eye);double d=to.length();
                if(d<72&&d>.01&&camera.getLookAngle().dot(to.scale(1/d))>-.15&&visible(l,camera,eye,point))return false;
            }
        }
        return true;
    }
    /** Native outlines occlude; the trailer's clear glazing does not hide a visible texture change. */
    public static boolean visible(net.minecraft.world.level.Level l,Entity camera,Vec3 from,Vec3 to){
        var direction=to.subtract(from).normalize();var start=from;
        for(int i=0;i<12;i++){
            var hit=l.clip(new ClipContext(start,to,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,camera));if(hit.getType()==HitResult.Type.MISS)return true;
            var pos=hit.getBlockPos();var state=l.getBlockState(pos);
            if(!state.is(Blocks.GLASS)&&!state.is(Blocks.GLASS_PANE)&&!state.is(GoatmanRegistry.WINDOW.get())&&!(state.getBlock() instanceof StainedGlassBlock)&&!(state.getBlock() instanceof StainedGlassPaneBlock))return false;
            var at=hit.getLocation();double exit=Double.POSITIVE_INFINITY;
            if(Math.abs(direction.x)>1e-7)exit=Math.min(exit,((direction.x>0?pos.getX()+1:pos.getX())-at.x)/direction.x);
            if(Math.abs(direction.y)>1e-7)exit=Math.min(exit,((direction.y>0?pos.getY()+1:pos.getY())-at.y)/direction.y);
            if(Math.abs(direction.z)>1e-7)exit=Math.min(exit,((direction.z>0?pos.getZ()+1:pos.getZ())-at.z)/direction.z);
            start=at.add(direction.scale(Math.max(0,exit)+.001));if(start.subtract(from).lengthSqr()>=to.subtract(from).lengthSqr())return true;
        }
        return false;
    }
    /** The trailer's side windows (west, then east), by their lower pane. */
    private static final BlockPos[] SIDE_WINDOWS={new BlockPos(-8,2,-59),new BlockPos(-8,2,-67),new BlockPos(-8,2,-73),new BlockPos(8,2,-59),new BlockPos(8,2,-67)};
    /** The four corners of a lap, just off the trailer's walls: front west, back west, back east, front east. */
    private static final Vec3[] CORNERS={new Vec3(-9.5,0,-51.5),new Vec3(-9.5,0,-78.5),new Vec3(9.5,0,-78.5),new Vec3(9.5,0,-51.5)};
    public static final int LAPS=3,LAP_GAP=300;
    public static final double LAP_SPEED=.8;
    /** Every window struck on the way round, and the back wall. */
    public static final int ALL_STRUCK=(1<<SIDE_WINDOWS.length+1)-1;
    /**
     * 0.4.84: while the impostor waits unseen behind the closed door, it is also seen as it is, tearing round the trailer:
     * for each reader inside, a private, unsaved figure runs a whole lap just off the walls, past every side window, striking
     * each one and the back wall as it goes, loud enough to hear from anywhere inside. It starts and ends at a corner the
     * reader cannot see, at most three times a night. Door pressure deliberately continues during these laps.
     */
    public static void dash(ServerLevel l,BlockPos b,CompoundTag r,ServerPlayer p,CompoundTag own){
        long now=l.getGameTime();
        if(p.isSpectator()||own.getInt("Laps0484")>=LAPS||now<own.getLong("LapAt0484"))return;
        boolean waiting=r.getInt("Phase")==GoatmanVignette.VIGIL?circuitDone(r)&&r.getInt("StalkWindowLeg0465")==0&&(!r.contains("AssaultAt0465")||r.getBoolean("AssaultDone0465")):r.getBoolean("Silence");
        if(!waiting||!GoatmanWoods.indoors(p.position().subtract(b.getX(),b.getY(),b.getZ())))return;
        if(!l.getEntitiesOfClass(GoatmanFigure.class,IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN),f->f.purpose.equals("lap")&&f.viewer().filter(p.getUUID()::equals).isPresent()).isEmpty())return;
        Vec3 eye=p.getEyePosition();int first=p.getRandom().nextInt(4),start=-1;
        for(int k=0;k<4;k++){int c=(first+k)%4;if(!seenAt(l,p,eye,abs(b,CORNERS[c]))){start=c;break;}}
        if(start<0){own.putLong("LapAt0484",now+40);return;}
        var f=GoatmanRegistry.FIGURE.get().create(l);if(f==null)return;
        int dir=p.getRandom().nextBoolean()?1:-1;f.viewer(p.getUUID());f.purpose="lap";f.lapBase=b;f.lapCorner=start;f.lapDir=dir;f.until=now+1200;
        for(int k=1;k<=4;k++)f.lap.add(abs(b,CORNERS[Math.floorMod(start+k*dir,4)]));
        Vec3 from=abs(b,CORNERS[start]),to=f.lap.peekFirst();float yaw=(float)(Math.atan2(to.z-from.z,to.x-from.x)*180/Math.PI)-90;
        f.moveTo(from.x,from.y,from.z,yaw,0);f.face(yaw);GoatmanHaunt.appear(l,f);
        own.putInt("Laps0484",own.getInt("Laps0484")+1);own.putLong("LapAt0484",now+LAP_GAP);
    }
    /**
     * 0.4.97 visibility: the lap ran on the ground, and the figure's head (1.7-2.1 above its feet) passed below the side
     * windows' sills at y 2, a block a tick. At a window its reader can actually see from where they stand, it now jumps up
     * onto the tire beneath it, so its head and shoulders fill the glass; it slams the pane, waits for a look, stares back
     * laughing without a sound while it is watched, and drops back to its lane past the window. Windows the reader cannot
     * see are still slapped at a run.
     */
    public static final double VAULT=.42,FACE=1.9;
    public static final int PANE_SLAM=10,PANE_WAIT=46,PANE_WATCHED=24,PANE_MAX=72;
    /** Where the figure stands on a side window's tire, just off the glass. */
    public static Vec3 paneSpot(BlockPos b,int window){var w=SIDE_WINDOWS[window];return abs(b,new Vec3(w.getX()<0?-8.5:9.5,1,w.getZ()+.5));}
    private static Vec3 paneSpot(ServerLevel l,BlockPos b,int window){var w=SIDE_WINDOWS[window];return abs(b,GoatmanWindowCircuit.landing(l,b,new Vec3(w.getX()<0?-8.5:9.5,1,w.getZ()+.5)));}
    public static int sideWindows(){return SIDE_WINDOWS.length;}
    /** One tick of a lap: on round at a run, striking the windows it passes; gone at its corner once it is out of sight there. */
    static void lapStep(ServerLevel l,GoatmanFigure f,ServerPlayer p,long now){
        Vec3 to=f.lap.peekFirst();if(to==null||f.lapBase==null){f.discard();return;}
        if(f.pane>=0){atPane(l,f,p,now,to);return;}
        int ahead=paneAhead(f,to);
        if(ahead>=0&&canSee(l,p,paneSpot(l,f.lapBase,ahead).add(0,FACE,0))){f.pane=ahead;f.paneAt=-1;f.paneSeen=0;f.paneExit=null;atPane(l,f,p,now,to);return;}
        Vec3 d=to.subtract(f.position());double len=d.length();float yaw=len>.01?(float)(Math.atan2(d.z,d.x)*180/Math.PI)-90:f.getYRot();
        if(len<=LAP_SPEED){
            f.step(to,yaw);f.lap.pollFirst();f.lapCorner=Math.floorMod(f.lapCorner+f.lapDir,4);
            if(f.lap.isEmpty()){
                // Round to where it began: it goes there only if this reader cannot see it go; otherwise on to the next corner.
                if(!seenAt(l,p,p.getEyePosition(),f.position())||f.extra>=4){f.discard();return;}
                f.extra++;f.lap.add(abs(f.lapBase,CORNERS[Math.floorMod(f.lapCorner+f.lapDir,4)]));
            }
        }else f.step(f.position().add(d.scale(LAP_SPEED/len)),yaw);
        if(now%3==0)hear(p,net.minecraft.sounds.SoundEvents.GRASS_STEP,f.position(),1.1F,.65F);
        strike(f,p);
    }
    /** A slap or a scrape on each window as it passes, and a blow on the back wall, all heard by its reader only. */
    private static void strike(GoatmanFigure f,ServerPlayer p){
        BlockPos b=f.lapBase;Vec3 rel=f.position().subtract(b.getX(),b.getY(),b.getZ());
        for(int i=0;i<SIDE_WINDOWS.length;i++){var w=SIDE_WINDOWS[i];if((f.struck&1<<i)!=0)continue;
            double lane=w.getX()<0?-9.5:9.5;
            if(Math.abs(rel.x-lane)<.8&&Math.abs(rel.z-(w.getZ()+.5))<.5){f.struck|=1<<i;f.swing(InteractionHand.MAIN_HAND);
                Vec3 pane=Vec3.atCenterOf(b.offset(w)).add(w.getX()<0?-.45:.45,0,0);
                hear(p,i%2==0?GoatmanRegistry.CLAW.get():GoatmanRegistry.KNOCK.get(),pane,3F,i%2==0?1.2F:1.05F);}
        }
        int back=1<<SIDE_WINDOWS.length;
        if((f.struck&back)==0&&Math.abs(rel.z+78.5)<.8&&Math.abs(rel.x-.5)<.6){f.struck|=back;f.swing(InteractionHand.MAIN_HAND);
            hear(p,GoatmanRegistry.HAMMER.get(),Vec3.atCenterOf(b.offset(0,2,-77)).add(0,0,-.45),3.4F,.85F);}
    }
    /** The next unstruck side window just ahead of it on its own side's lane, or -1. */
    private static int paneAhead(GoatmanFigure f,Vec3 to){
        BlockPos b=f.lapBase;Vec3 rel=f.position().subtract(b.getX(),b.getY(),b.getZ());double heading=to.z-f.getZ();
        for(int i=0;i<SIDE_WINDOWS.length;i++){if((f.struck&1<<i)!=0)continue;
            var w=SIDE_WINDOWS[i];double lane=w.getX()<0?-9.5:9.5,ahead=w.getZ()+.5-rel.z;
            if(Math.abs(rel.x-lane)<.8&&Math.abs(ahead)<1.7&&ahead*heading>=0)return i;}
        return -1;
    }
    /** Up on a side window's tire: vault, slam, wait for a look, stare while watched, then drop back down past it. */
    private static void atPane(ServerLevel l,GoatmanFigure f,ServerPlayer p,long now,Vec3 to){
        BlockPos b=f.lapBase;var w=SIDE_WINDOWS[f.pane];boolean west=w.getX()<0;float inward=west?-90:90;
        if(f.paneExit!=null){
            Vec3 d=f.paneExit.subtract(f.position());double len=d.length();float yaw=len>.01?(float)(Math.atan2(d.z,d.x)*180/Math.PI)-90:f.getYRot();
            if(len<=VAULT){f.step(f.paneExit,yaw);f.pane=-1;f.paneAt=-1;f.paneExit=null;}else f.step(f.position().add(d.scale(VAULT/len)),yaw);
            return;
        }
        Vec3 spot=paneSpot(l,b,f.pane),glass=Vec3.atCenterOf(b.offset(w)).add(west?-.45:.45,0,0);
        if(f.paneAt<0){
            Vec3 d=spot.subtract(f.position());double len=d.length();
            if(len>VAULT){f.step(f.position().add(d.scale(VAULT/len)),(float)(Math.atan2(d.z,d.x)*180/Math.PI)-90);return;}
            f.step(spot,inward);f.face(inward);f.paneAt=now;f.struck|=1<<f.pane;f.swing(InteractionHand.MAIN_HAND);
            hear(p,f.pane%2==0?GoatmanRegistry.CLAW.get():GoatmanRegistry.KNOCK.get(),glass,3.2F,f.pane%2==0?1.1F:.95F);
            return;
        }
        long held=now-f.paneAt;boolean seen=watching(l,p,spot.add(0,FACE,0));
        // Caught looking: it hits the glass again, right in front of them.
        if(seen&&++f.paneSeen==8){f.swing(InteractionHand.MAIN_HAND);hear(p,GoatmanRegistry.KNOCK.get(),glass,3.4F,.88F);}
        f.face(inward);f.heave(seen);
        if(held<PANE_SLAM||held<PANE_MAX&&(f.paneSeen==0?held<PANE_WAIT:f.paneSeen<PANE_WATCHED))return;
        f.heave(false);double away=Math.signum(to.z-spot.z);if(away==0)away=west?-1:1;
        f.paneExit=abs(b,new Vec3(west?-9.5:9.5,0,w.getZ()+.5+away*1.4));
    }
    /** Its reader's actual camera has a clear native glazing ray to the point, wherever they are looking. */
    private static boolean canSee(ServerLevel l,ServerPlayer p,Vec3 point){
        Entity camera=p.getCamera();var eye=camera.getEyePosition();return eye.distanceToSqr(point)<72*72&&visible(l,camera,eye,point);
    }
    /** Its reader is looking at the point now, through clear glazing. */
    private static boolean watching(ServerLevel l,ServerPlayer p,Vec3 point){
        Entity camera=p.getCamera();var eye=camera.getEyePosition();var to=point.subtract(eye);
        return to.lengthSqr()>.0001&&camera.getLookAngle().dot(to.normalize())>.65&&canSee(l,p,point);
    }
    private static void hear(ServerPlayer p,net.minecraft.sounds.SoundEvent sound,Vec3 at,float volume,float pitch){
        p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),SoundSource.HOSTILE,at.x,at.y,at.z,volume,pitch,p.getRandom().nextLong()));
    }
    private static boolean seenAt(ServerLevel l,ServerPlayer p,Vec3 eye,Vec3 feet){
        for(double y:new double[]{.3,1.2,2.1}){var point=feet.add(0,y,0);var to=point.subtract(eye);double d=to.length();if(d>.01&&p.getLookAngle().dot(to.scale(1/d))>-.15&&visible(l,p,eye,point))return true;}
        return false;
    }
    private static void change(ServerLevel l,CompoundTag r,GoatmanChild c){
        if(!r.getBoolean("StalkChange0465")||!obscured(l,c))return;
        int turn=r.getInt("StalkTurn0465"),skin=turn%2==0?-2:nextChild(r);
        c.disguise(skin);r.putInt("StalkTurn0465",turn+1);r.putBoolean("StalkChange0465",false);
    }
    /** The admission keeps a cousin's face; a visible monster waits outside until it can change unseen. */
    public static boolean admissionDisguise(ServerLevel l,CompoundTag r,GoatmanChild c){
        if(c.monsterAppearance()){
            if(!obscured(l,c)){c.pose(false);return false;}
            int skin=nextChild(r);c.disguise(skin>=0?skin:c.skin());
        }
        r.putInt("AdmissionSkin04114",c.visibleSkin());r.putBoolean("StalkChange0465",false);return true;
    }
    public static boolean eveningActive(CompoundTag run){var lap=run.getCompound("EveningCircuit0497");return !lap.isEmpty()&&lap.getInt("StalkLeg0465")<CIRCUIT.length;}
    /** The fleeing child keeps its face through the doorway; only the unseen body outside may change between panes. */
    public static boolean evening(ServerLevel l,BlockPos b,CompoundTag run,GoatmanChild c,int clock,boolean ready){
        if(run.getInt("WindowRun0464")==3)return false;
        var lap=run.getCompound("EveningCircuit0497");
        if(lap.isEmpty()){
            if(!ready)return false;
            lap.putBoolean("Evening0497",true);lap.putBoolean("StalkStarted0465",true);lap.putInt("CircuitGoal0494",1);
            lap.putIntArray("StalkDeck0465",run.getIntArray("StalkDeck0465"));lap.putInt("StalkUsed0465",run.getInt("StalkUsed0465")|1<<c.skin());
            lap.putInt("StalkCursor0465",run.getInt("StalkCursor0465"));lap.putInt("StalkTurn0465",run.getInt("StalkTurn0465"));lap.putBoolean("StalkChange0465",true);
            lap.putFloat("StalkSpeed0497",.42F);c.getPersistentData().remove("Route");c.pose(false);c.heave(false);
            run.putInt("WindowRun0464",1);GoatmanFear.departed(run);
        }
        int previous=lap.getInt("ImpactSerial0472");step(l,b,lap,c,clock);
        for(String key:List.of("StalkUsed0465","StalkCursor0465","StalkTurn0465"))run.putInt(key,lap.getInt(key));
        if(lap.getInt("ImpactSerial0472")!=previous){
            run.putLong("LastImpact0465",lap.getLong("LastImpact0465"));run.putLong("LastImpactPos0465",lap.getLong("LastImpactPos0465"));
            run.putInt("ImpactSerial0472",run.getInt("ImpactSerial0472")+1);run.putInt("ImpactClock0472",clock);
        }
        run.put("EveningCircuit0497",lap);
        if(lap.getInt("StalkLeg0465")>=CIRCUIT.length){
            // The body really finished on the front step; no stale by-fire route may cut back through the walls.
            c.getPersistentData().putString("At","step");c.getPersistentData().putString("Goal","step");c.getPersistentData().remove("Route");
            run.putInt("WindowRun0464",3);if(run.getBoolean("Silence"))run.putInt("ExtraState",GoatmanVignette.X_DOOR);
        }
        return true;
    }
    public static void step(ServerLevel l,BlockPos b,CompoundTag r,GoatmanChild c,int clock){
        boolean evening=r.getBoolean("Evening0497");
        if(!r.getBoolean("StalkStarted0465")){
            r.putBoolean("StalkStarted0465",true);r.putInt("StalkUsed0465",r.getInt("StalkUsed0465")|1<<c.skin());r.putBoolean("StalkChange0465",true);
            // An already-started saved circuit keeps its progress. New nights circle repeatedly
            // with this same saved actor, rather than waiting invisibly through the whole assault.
            if(r.getInt("StalkLeg0465")==0)r.putInt("CircuitGoal0494",CIRCUITS);
            if(!c.stalkingAppearance())c.disguise(c.skin());c.getPersistentData().remove("Route");c.pose(false);c.cower(false);c.heave(false);
        }
        boolean supported=GoatmanWindowCircuit.stepsReady(l,b);
        boolean window=!evening&&circuitDone(r)&&r.getBoolean("AssaultDone0465")&&clock>=1900;
        if(!window&&!GoatmanWindowCircuit.atPane(b,c))change(l,r,c);
        // Longer glimpses cannot move the voice clock or make a distant wall strike stress the door.
        // Finish on this original body's real front step, then permit its bathroom-window approach.
        if(!evening&&r.contains("WindowSerial0497")&&r.getInt("StalkLeg0465")>=CIRCUIT.length
                &&r.getInt("Circuits0494")>=r.getInt("CircuitGoal0494")&&!r.getBoolean("DoorPressureDone0497")){
            c.pose(false);face(c,Vec3.atCenterOf(b.offset(GoatmanWoods.DOOR)));
            GoatmanWindowCircuit.frontDoor(l,b,r,c,clock);return;
        }
        // Voice timing cannot skip a leg or send this same body to the window prematurely.
        if(window&&!admissionDisguise(l,r,c))return;
        if(!window&&circuitDone(r)){
            c.pose(false);face(c,abs(b,new Vec3(.5,2,-55)));
            return;
        }
        int leg=r.getInt(window?"StalkWindowLeg0465":"StalkLeg0465");Vec3[] route=window?TO_WINDOW:CIRCUIT;
        if(evening&&leg>=route.length){c.pose(false);return;}
        if(!window&&leg>=route.length&&!circuitDone(r)){
            GoatmanWindowCircuit.nextLap(r,c,clock);c.pose(false);return;
        }
        if(leg>=route.length){c.pose(false);face(c,Vec3.atCenterOf(b.offset(GoatmanWoods.WINDOW)));return;}
        if(GoatmanWindowCircuit.hold(l,r,c,clock)||clock<r.getInt("StalkPause0465")){c.pose(false);return;}
        Vec3 local=route[leg];if(!window&&supported){if(leg==6)local=new Vec3(-8.5,1,-66.5);if(leg==12)local=new Vec3(9.5,1,-66.5);}
        if(!window&&GoatmanWindowCircuit.sideWindow(leg)&&local.y>=1)local=GoatmanWindowCircuit.landing(l,b,local);
        local=GoatmanWindowCircuit.porchLanding(l,b,local);
        Vec3 via=!window&&supported?GoatmanWindowCircuit.via(leg):null;boolean approaching=via!=null&&r.getInt("StalkVia0497")!=leg;
        Vec3 target=abs(b,approaching?via:local),delta=target.subtract(c.position());double length=delta.length();
        double speed=(r.contains("StalkSpeed0497")?r.getFloat("StalkSpeed0497"):.42)*(r.getInt("DemandStage0465")>=4?.52/.42:1);
        // Leave the tire sideways before descending. A diagonal downward move is
        // clipped by the wheel under the feet and pins the body against the sill.
        if(c.getY()>target.y+.15&&Math.abs(c.getX()-b.getX())>8){double lane=c.getX()<b.getX()?-9.5:10.25;double exit=b.getX()+lane-c.getX();
            delta=Math.abs(exit)>.04?new Vec3(exit,0,0):new Vec3(0,target.y-c.getY(),0);length=delta.length();}
        Vec3 movement=length<=speed?delta:delta.scale(speed/length);var before=c.position();
        c.setOnGround(true);c.move(net.minecraft.world.entity.MoverType.SELF,movement);c.setDeltaMovement(Vec3.ZERO);
        // A wheel approach rises physically before the next horizontal step; collision never teleports the body.
        if(c.position().distanceToSqr(before)<.000004&&delta.y>.03)c.move(net.minecraft.world.entity.MoverType.SELF,new Vec3(0,Math.min(.08,delta.y),0));
        float yaw=length>.01?(float)(Math.atan2(delta.z,delta.x)*180/Math.PI)-90:c.getYRot();
        c.setYRot(yaw);c.setYHeadRot(yaw);c.yBodyRot=yaw;c.animate(c.position().distanceTo(before),false,false);
        if(c.position().distanceToSqr(target)>.012)return;
        if(approaching){r.putInt("StalkVia0497",leg);return;}
        r.putInt(window?"StalkWindowLeg0465":"StalkLeg0465",leg+1);
        if(!window&&leg==route.length-1&&r.contains("CircuitGoal0494"))r.putInt("Circuits0494",r.getInt("Circuits0494")+1);
        if(window&&leg==route.length-1){impact(l,b,r,c,GoatmanWoods.WINDOW,2);return;}
        if(!window){
            BlockPos hit=switch(leg){case 0->r.getInt("Circuits0494")==0?GoatmanWoods.DOOR:null;case 5->new BlockPos(-8,2,-59);case 6->new BlockPos(-8,2,-67);case 7->new BlockPos(-8,2,-73);case 9->new BlockPos(0,2,-77);case 11->GoatmanWoods.WINDOW;case 12->new BlockPos(8,2,-67);case 13->new BlockPos(8,2,-59);default->null;};
            if(hit!=null){face(c,Vec3.atCenterOf(b.offset(hit)));impact(l,b,r,c,hit,leg%3==0?2:1);r.putInt("StalkPause0465",clock+18+c.getRandom().nextInt(29));
                if(GoatmanWindowCircuit.sideWindow(leg)){GoatmanWindowCircuit.start(l,r,c,clock);r.putBoolean("StalkChange0465",true);}
                else if(!r.contains("CircuitGoal0494"))r.putBoolean("StalkChange0465",true);}
        }
    }
    private static void face(GoatmanChild c,Vec3 p){var d=p.subtract(c.position());float yaw=(float)(Math.atan2(d.z,d.x)*180/Math.PI)-90;c.setYRot(yaw);c.setYHeadRot(yaw);c.yBodyRot=yaw;}
    public static void assault(ServerLevel l,BlockPos b,CompoundTag r,List<ServerPlayer> players,int clock){
        // These native world sounds remain audible when the body is blocked or temporarily untracked.
        // It starts at the front door, then races round: one side, the back, the other side.
        BlockPos[] sides={GoatmanWoods.DOOR,new BlockPos(-8,2,-67),new BlockPos(0,2,-77),new BlockPos(8,2,-67)};
        int[] at={120,190,250,310};
        int cues=r.getInt("PressureCues0472");
        for(int i=0;i<sides.length;i++)if(clock>=at[i]&&(cues&(1<<i))==0){
            impact(l,b,r,null,sides[i],i==2?2:1);r.putInt("PressureCues0472",cues|(1<<i));break;
        }
        if(!r.contains("AssaultAt0465")){if(clock<450)return;r.putInt("AssaultAt0465",clock);}
        int elapsed=clock-r.getInt("AssaultAt0465");
        if(elapsed>=ASSAULT_END){r.putBoolean("AssaultDone0465",true);int beat=(elapsed-ASSAULT_END)%80;
            if(beat==0||beat==7||beat==15){impact(l,b,r,null,GoatmanWoods.DOOR,beat==0?2:1);r.putInt("DoorPulseSerial04112",r.getInt("DoorPulseSerial04112")+1);}return;}
        int stage=0;while(stage+1<DEMAND_TICKS.length&&elapsed>=DEMAND_TICKS[stage+1])stage++;
        int beat=elapsed-DEMAND_TICKS[stage];
        if(beat==0){GoatmanFear.begin(r);r.putInt("Demand",101+stage);r.putInt("DemandAt",clock);r.putInt("DemandStage0465",stage+1);}
        boolean hit=switch(stage){case 0->beat==0||beat==14;case 1->beat==0||beat==22||beat==44;case 2->beat==12;case 3->beat==0||beat==10||beat==20;case 4->beat==30;case 5->beat==12;default->beat<80&&beat%6==0;};
        if(!hit)return;
        var it=l.getEntitiesOfClass(GoatmanChild.class,IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN),c->c.getTags().contains(GoatmanVignette.ACTOR)&&c.getPersistentData().getInt(GoatmanVignette.INDEX)==r.getInt("Wrong")&&c.getPersistentData().hasUUID(GoatmanVignette.ROUND)&&c.getPersistentData().getUUID(GoatmanVignette.ROUND).equals(r.getUUID("Id"))).stream().findFirst().orElse(null);
        // The door answers while the same original body is seen circling outside.
        // This deliberate overlap is the requested impossibility, not a second actor.
        impact(l,b,r,it!=null&&it.distanceToSqr(Vec3.atCenterOf(b.offset(GoatmanWoods.DOOR)))<9?it:null,GoatmanWoods.DOOR,stage==0?1:stage<3?2:3);
        if(stage==6)r.putInt("FinalBlows0465",r.getInt("FinalBlows0465")+1);
    }
    private static BlockPos nearestSurface(BlockPos b,Vec3 body){
        BlockPos best=GoatmanWoods.DOOR;double distance=body.distanceToSqr(Vec3.atCenterOf(b.offset(best)));
        for(var pane:SIDE_WINDOWS){double d=body.distanceToSqr(Vec3.atCenterOf(b.offset(pane)));if(d<distance){best=pane;distance=d;}}
        for(var wall:List.of(new BlockPos(0,2,-77),GoatmanWoods.WINDOW)){double d=body.distanceToSqr(Vec3.atCenterOf(b.offset(wall)));if(d<distance){best=wall;distance=d;}}
        return best;
    }
    public static void impact(ServerLevel l,BlockPos b,CompoundTag r,GoatmanChild c,BlockPos relative,int force){
        var at=b.offset(relative);var state=l.getBlockState(at);var center=Vec3.atCenterOf(at);boolean door=relative.equals(GoatmanWoods.DOOR);
        GoatmanFear.begin(r);if(door)r.putBoolean(GoatmanFear.DOOR_KNOCKED,true);
        var toward=c==null?new Vec3(relative.getX()< -4?-1:relative.getX()>4?1:0,0,relative.getZ()<=-77?-1:1):c.position().subtract(center);double x=center.x,z=center.z;
        if(Math.abs(toward.x)>Math.abs(toward.z))x+=Math.signum(toward.x)*.49;else z+=Math.signum(toward.z)*.49;
        double y=center.y+(door?.55:0);
        l.playSound(null,x,y,z,force==1?GoatmanRegistry.KNOCK.get():force==2?GoatmanRegistry.CLAW.get():GoatmanRegistry.HAMMER.get(),SoundSource.BLOCKS,3F+force*.2F,force==1?1.08F:.9F);
        if(force>=2)l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,state),x,y,z,force==3?12:5,.22,.28,.035,.012);
        if(force==3){l.sendParticles(ParticleTypes.POOF,x,y,z,3,.18,.25,.05,.008);l.playSound(null,x,y,z,SoundEvents.IRON_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.24F,.65F);}
        if(door&&l.getBlockEntity(at) instanceof TrailerDoorBlockEntity frame)frame.impact(force);
        if(c!=null)c.swing(InteractionHand.MAIN_HAND);r.putInt("ImpactClock0472",r.getInt("Clock"));r.putInt("ImpactSerial0472",r.getInt("ImpactSerial0472")+1);r.putLong("LastImpact0465",l.getGameTime());r.putLong("LastImpactPos0465",at.asLong());
    }
}
