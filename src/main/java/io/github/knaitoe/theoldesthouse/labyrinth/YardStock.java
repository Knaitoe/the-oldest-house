package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.345, the Stock Gate (docs/YARD_STOCK_0_4_345.md), the yard's middle puzzle, from the owner's draft: "From the loading platform you can
 * see how the swing gates join the chutes. At ground level the high plank sides hide the junctions. The gate positions and the movement of
 * the animals give the crew evidence."
 *
 * <p>A lane runs from the foot of a loading platform to the far gate, always open. Three side chutes join it, each round a bend to a swing
 * gate. Two of those gates stand open to the cattle pen; the third leads only to an empty crush and a loading race. While anyone is in the
 * yard the herd is driven out of the pen by whatever way the gates leave it, and a chute that reaches the lane brings the cattle into it,
 * away from whoever is there, to the far end, where they mill until the way back behind them is clear. Swinging both pen gates shut keeps
 * the herd in the pen. Loading steps along the lane are somewhere to get out of the way. Nothing hurts or is used up; the cattle push.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class YardStock {
    private YardStock(){}
    public static final String STATE="yard_stock_0345";
    public static final LabyrinthPlace PLACE=LabyrinthPlace.YARD_STOCK;
    /**
     * The yard, south (the platform's foot, z -10) to north (the far gate's cell, z -37), x from -11 to 11. {@code .} chute, {@code #} plank
     * side, {@code P} pen, {@code n} loading step, {@code S} the lane's foot, {@code E} the far gate's cell.
     */
    static final String[] MAP={
        "###########S###########",
        "###########.###########",
        "###########.###########",
        "###########.n##########",
        "###########.###########",
        "###########.###########",
        "#######.....###########",
        "#######.###.###########",
        "#######.###.###.#######",
        "#PPPP##.###.n##.#######",
        "#PPPP...###.###.#######",
        "#PPPP##.###.###.#######",
        "#PPPP##.###.....#######",
        "#PPPP######.###.#######",
        "#PPPP.....#.###.#######",
        "#PPPP##.###.###########",
        "#PPPP##.###.n##########",
        "#PPPP##.###.###########",
        "#PPPP##.....###########",
        "#PPPP######.###########",
        "###########.###########",
        "###########.###########",
        "###########.###########",
        "###########.n##########",
        "###########.###########",
        "###########.###########",
        "###########.###########",
        "###########E###########"};
    public static final int X0=-11,Z0=-10,ROWS=28;
    /** A swing gate: where its arms meet, the two cells its leaf can close, and the post it hangs on. */
    public record Gate(String name,BlockPos junction,BlockPos one,BlockPos two,BlockPos post){
        public BlockPos leaf(int position){return position==1?one:two;}
    }
    /** K and L close the near chutes to the pen in their second position; N leads only to the crush or the loading race either way. */
    public static final List<Gate> GATES=List.of(
        new Gate("K",new BlockPos(-4,0,-20),new BlockPos(-4,0,-21),new BlockPos(-5,0,-20),new BlockPos(-5,0,-21)),
        new Gate("L",new BlockPos(-4,0,-24),new BlockPos(-3,0,-24),new BlockPos(-5,0,-24),new BlockPos(-3,0,-25)),
        new Gate("N",new BlockPos(4,0,-22),new BlockPos(4,0,-21),new BlockPos(4,0,-23),new BlockPos(5,0,-21)));
    /** The pen's two gates, in the order the herd tries them. */
    public static final List<BlockPos> PEN_GATES=List.of(new BlockPos(-6,0,-20),new BlockPos(-6,0,-24));
    public static final int COWS=4,WARN=40,MILL=60,MILL_MAX=400,COOLDOWN=200,RESET=200;
    public static final double SPEED=.16,SPACING=1.3;
    public static final int HOME=0,WARNING=1,OUT=2,MILLING=3,BACK=4,STUCK=5;

    public static boolean is(LabyrinthPlace p){return p==PLACE;}
    public static char cell(int x,int z){int r=Z0-z,c=x-X0;if(r<0||r>=ROWS||c<0||c>=MAP[0].length())return '#';return MAP[r].charAt(c);}
    public static CompoundTag state(ServerLevel l){return LabyrinthData.get(l.getServer()).stateEntry(STATE,PLACE.id());}
    static void save(ServerLevel l,CompoundTag s){LabyrinthData.get(l.getServer()).setStateEntry(STATE,PLACE.id(),s);}
    public static int position(CompoundTag s,Gate g){int v=s.getInt("Gate"+g.name());return v==0?1:v;}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState st){YardRooms.box(l,b,x0,y0,z0,x1,y1,z1,st);}
    static void put(ServerLevel l,BlockPos b,int x,int y,int z,BlockState st){YardRooms.set(l,b,x,y,z,st);}

    // ------------------------------------------------------------------ building
    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();
        box(l,b,-12,-3,-39,12,-2,0,Blocks.DIRT.defaultBlockState());box(l,b,-12,-1,-39,12,-1,0,Blocks.GRASS_BLOCK.defaultBlockState());
        box(l,b,-12,0,-39,12,8,0,YardRooms.leaves());
        // The approach and the loading platform over it: the lane runs on underneath to its foot; the stair climbs at the east side.
        box(l,b,-7,0,-9,9,8,-1,air);
        var deck=Blocks.SPRUCE_PLANKS.defaultBlockState();
        box(l,b,-6,3,-9,8,3,-5,deck);
        for(int x:new int[]{-6,-2,3,8})for(int z:new int[]{-5,-9})box(l,b,x,0,z,x,2,z,Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        // A rail all round the deck but the stair's head: nobody steps from it onto the plank sides.
        for(int x=-5;x<=7;x++)put(l,b,x,4,-9,fence(false,true,false,true));
        for(int x=-5;x<=6;x++)put(l,b,x,4,-5,fence(false,true,false,x>-6));
        put(l,b,6,4,-5,fence(false,false,false,true));
        for(int z=-8;z<=-6;z++){put(l,b,-6,4,z,fence(true,false,true,false));put(l,b,8,4,z,fence(true,false,true,false));}
        put(l,b,-6,4,-9,fence(false,true,true,false));put(l,b,8,4,-9,fence(false,false,true,true));put(l,b,-6,4,-5,fence(true,true,false,false));
        for(int i=0;i<4;i++)for(int x=7;x<=8;x++)put(l,b,x,i,-1-i,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
        put(l,b,0,2,-8,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        // The yard: high plank sides round every chute, open to the sky so the platform looks down on its plan.
        box(l,b,-11,0,-38,11,8,-10,air);
        for(int z=Z0;z>Z0-ROWS;z--)for(int x=X0;x<X0+MAP[0].length();x++){
            char c=cell(x,z);
            switch(c){
                case '#'->{box(l,b,x,0,z,x,2,z,Blocks.SPRUCE_PLANKS.defaultBlockState());}
                case 'P'->put(l,b,x,-1,z,Blocks.MUD.defaultBlockState());
                case 'n'->{put(l,b,x,-1,z,Blocks.COARSE_DIRT.defaultBlockState());put(l,b,x,0,z,Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM));}
                default->put(l,b,x,-1,z,Blocks.COARSE_DIRT.defaultBlockState());
            }
        }
        box(l,b,-11,0,-38,11,2,-38,Blocks.SPRUCE_PLANKS.defaultBlockState());
        // The posts the gates hang on stand a little above the sides, so their plan reads from the platform.
        for(var g:GATES)box(l,b,g.post().getX(),0,g.post().getZ(),g.post().getX(),3,g.post().getZ(),Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        for(var g:PEN_GATES)put(l,b,g.getX(),3,g.getZ(),Blocks.SPRUCE_FENCE.defaultBlockState());
        var s=state(l);
        if(!s.contains("GateN"))s.putInt("GateN",1+Math.floorMod(Long.hashCode(b.asLong()),2));
        s.putInt("GateK",1);s.putInt("GateL",1);
        for(var g:GATES)leaf(l,b,g,position(s,g),true);
        save(l,s);
        LabyrinthBuilder.entrance(l,b,Blocks.SPRUCE_PLANKS.defaultBlockState(),Blocks.GRASS_BLOCK.defaultBlockState(),Blocks.SPRUCE_SLAB.defaultBlockState());
        LabyrinthBuilder.doors(l,b,PLACE);
        herd(l,b,s);save(l,s);
    }
    private static BlockState fence(boolean n,boolean e,boolean s,boolean w){return Blocks.SPRUCE_FENCE.defaultBlockState().setValue(FenceBlock.NORTH,n).setValue(FenceBlock.EAST,e).setValue(FenceBlock.SOUTH,s).setValue(FenceBlock.WEST,w);}
    /** A leaf across one arm, three blocks high, facing out along that arm from where the arms meet. */
    static void leaf(ServerLevel l,BlockPos b,Gate g,int position,boolean on){
        var at=g.leaf(position);var d=at.subtract(g.junction());var facing=Direction.getNearest(d.getX(),0,d.getZ());
        for(int y=0;y<3;y++)put(l,b,at.getX(),y,at.getZ(),on?YardBlock.of(YardBlock.Kind.STOCK_LEAF,facing,y):Blocks.AIR.defaultBlockState());
    }

    // ------------------------------------------------------------------ the gates
    /** A swing gate swung by hand from the chute; never across a body. */
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState block){
        if(!YardBlock.is(block,YardBlock.Kind.STOCK_LEAF))return false;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null||!p.level().dimension().equals(HouseDimensions.OUTSIDE))return false;
        if(LabyrinthPlaces.placeAt(origin,pos)!=PLACE)return false;
        var b=LabyrinthPlaces.base(origin,PLACE);var r=pos.subtract(b);var l=p.serverLevel();var s=state(l);
        if(p.isSpectator()||p.distanceToSqr(pos.getCenter())>36)return true;
        for(var g:GATES){int now=position(s,g);if(g.leaf(now).getX()!=r.getX()||g.leaf(now).getZ()!=r.getZ())continue;
            int next=3-now;
            if(occupied(l,b,g.leaf(next),true)||occupied(l,b,g.junction(),false)){PuzzleRooms.say(p,"The gate won't swing. Something is in its way.");return true;}
            leaf(l,b,g,now,false);leaf(l,b,g,next,true);s.putInt("Gate"+g.name(),next);save(l,s);
            l.playSound(null,pos,SoundEvents.IRON_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.9F,.75F);l.playSound(null,b.offset(g.post()),SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,.6F,.8F);
            return true;
        }
        return true;
    }
    /** A living body in a cell: any at all where the leaf would close, cattle only where the arms meet (a reader swings it from there). */
    static boolean occupied(ServerLevel l,BlockPos b,BlockPos rel,boolean anyone){
        var at=b.offset(rel);return !l.getEntitiesOfClass(LivingEntity.class,new AABB(at.getX(),at.getY(),at.getZ(),at.getX()+1,at.getY()+3,at.getZ()+1),e->e.isAlive()&&!e.isSpectator()&&(anyone||e instanceof Cow)).isEmpty();
    }
    static boolean leafAt(CompoundTag s,int x,int z){for(var g:GATES){var at=g.leaf(position(s,g));if(at.getX()==x&&at.getZ()==z)return true;}return false;}
    static boolean herdCell(CompoundTag s,int x,int z){char c=cell(x,z);return c!='#'&&c!='n'&&c!='P'&&!leafAt(s,x,z);}

    // ------------------------------------------------------------------ the herd
    /** The four cattle, made once with the yard and never replaced. */
    static void herd(ServerLevel l,BlockPos b,CompoundTag s){
        if(s.getBoolean("HerdMade"))return;var ids=new ListTag();
        for(int i=0;i<COWS;i++){var cow=EntityType.COW.create(l);if(cow==null)continue;
            cow.setPersistenceRequired();cow.setNoAi(true);cow.setNoGravity(true);cow.setInvulnerable(true);
            var at=queue(PEN_GATES.get(0),i);cow.moveTo(b.getX()+at.x,b.getY(),b.getZ()+at.z,90,0);
            if(l.addFreshEntity(cow))ids.add(NbtUtils.createUUID(cow.getUUID()));}
        s.put("Herd",ids);s.putBoolean("HerdMade",true);s.putInt("Pen",0);
    }
    /** Cow {@code i}'s place in the pen, in line behind a pen gate, the first nearest it. */
    static Vec3 queue(BlockPos gate,int i){return new Vec3(gate.getX()-1-i+.5,0,gate.getZ()+.5);}
    static List<Cow> cows(ServerLevel l,CompoundTag s){
        var out=new ArrayList<Cow>();var ids=s.getList("Herd",Tag.TAG_INT_ARRAY);
        for(int i=0;i<ids.size();i++)if(l.getEntity(NbtUtils.loadUUID(ids.get(i))) instanceof Cow c&&c.isAlive())out.add(c);else return null;
        return out;
    }
    /**
     * The way out of the pen the gates leave: from the first pen gate with somewhere to go, along every open cell, straight on where it can,
     * and where a chute meets the lane, away from the nearest reader in it (north with none). The four cells in the pen first.
     */
    static List<BlockPos> drive(ServerLevel l,BlockPos b,CompoundTag s,int[] pen){
        for(int g=0;g<PEN_GATES.size();g++){
            var gate=PEN_GATES.get(g);var path=new ArrayList<BlockPos>();
            for(int i=COWS-1;i>=0;i--)path.add(new BlockPos(gate.getX()-1-i,0,gate.getZ()));
            var prev=new BlockPos(gate.getX()-1,0,gate.getZ());var cur=gate;path.add(cur);var seen=new HashSet<>(path);
            while(path.size()<80){
                var options=new ArrayList<BlockPos>();
                for(var d:Direction.Plane.HORIZONTAL){var n=cur.relative(d);if(!n.equals(prev)&&!seen.contains(n)&&herdCell(s,n.getX(),n.getZ()))options.add(n);}
                if(options.isEmpty())break;
                var step=cur.subtract(prev);var straight=cur.offset(step);BlockPos next;
                if(cur.getX()==0&&options.size()>1&&!options.contains(straight)){
                    Double reader=nearestInLane(l,b,cur.getZ());boolean north=reader==null||reader>cur.getZ()+.5;
                    int here=cur.getZ();next=options.stream().filter(o->north?o.getZ()<here:o.getZ()>here).findFirst().orElse(options.get(0));
                }else next=options.contains(straight)?straight:options.get(0);
                prev=cur;cur=next;path.add(cur);seen.add(cur);
            }
            if(path.size()>COWS+1){pen[0]=g;return path;}
        }
        return List.of();
    }
    /** The relative z of the reader in the lane nearest {@code z}, or null. */
    static Double nearestInLane(ServerLevel l,BlockPos b,int z){
        Double best=null;
        for(var p:l.players()){if(p.isSpectator()||!p.isAlive())continue;var r=p.position().subtract(Vec3.atLowerCornerOf(b));
            if(r.x<-.2||r.x>1.2||r.z>Z0+.5||r.z<Z0-ROWS+.5||r.y<-1||r.y>3)continue;
            if(best==null||Math.abs(r.z-z)<Math.abs(best-z))best=r.z;}
        return best;
    }
    static List<BlockPos> path(CompoundTag s){var out=new ArrayList<BlockPos>();for(long v:s.getLongArray("Path"))out.add(BlockPos.of(v));return out;}
    static Vec3 along(List<BlockPos> path,double d){
        d=Math.max(0,Math.min(path.size()-1,d));int i=(int)Math.floor(d);var a=Vec3.atLowerCornerOf(path.get(i)).add(.5,0,.5);
        if(i+1>=path.size())return a;var c=Vec3.atLowerCornerOf(path.get(i+1)).add(.5,0,.5);return a.lerp(c,d-i);
    }
    static Vec3 heading(List<BlockPos> path,double d){int i=(int)Math.max(0,Math.min(path.size()-2,Math.floor(d)));return Vec3.atLowerCornerOf(path.get(i+1).subtract(path.get(i)));}
    /** A reader in the way of cow position {@code at} going {@code dir}, within {@code reach}. */
    static ServerPlayer inWay(ServerLevel l,BlockPos b,Vec3 at,Vec3 dir,double reach){
        var world=Vec3.atLowerCornerOf(b).add(at);
        for(var p:l.players()){if(p.isSpectator()||!p.isAlive())continue;var d=p.position().subtract(world);double ahead=d.x*dir.x+d.z*dir.z,side=Math.abs(d.x*dir.z-d.z*dir.x);
            if(ahead>0&&ahead<reach&&side<.75&&Math.abs(d.y)<1.5)return p;}
        return null;
    }
    static void shove(ServerPlayer p,Vec3 dir){var v=p.getDeltaMovement();p.setDeltaMovement(dir.x*.32,Math.max(v.y,0),dir.z*.32);p.hurtMarked=true;}

    static void tickHerd(ServerLevel l,BlockPos b,CompoundTag s,boolean readersIn){
        var herd=cows(l,s);if(herd==null)return;long now=l.getGameTime();int phase=s.getInt("Phase");
        switch(phase){
            case HOME->{
                if(readersIn&&now>=s.getLong("Ready")){
                    int[] pen={0};var path=drive(l,b,s,pen);var gate=b.offset(PEN_GATES.get(pen[0])).getCenter();
                    // Both pen gates shut: they crowd the gate, and go nowhere.
                    if(path.isEmpty()){s.putLong("Ready",now+COOLDOWN);s.putBoolean("Held",true);l.playSound(null,b.getX()-6.5,b.getY()+1,b.getZ()-22,SoundEvents.WOOD_HIT,SoundSource.BLOCKS,1F,.7F);l.playSound(null,b.getX()-8.5,b.getY()+1,b.getZ()-22,SoundEvents.COW_AMBIENT,SoundSource.NEUTRAL,1F,.8F);settle(herd,b,s);return;}
                    s.putLongArray("Path",path.stream().mapToLong(BlockPos::asLong).toArray());s.putInt("Pen",pen[0]);s.putInt("Phase",WARNING);s.putLong("At",now);s.putBoolean("Held",false);
                    s.putBoolean("Lane",path.stream().anyMatch(c->c.getX()==0));
                    l.playSound(null,gate.x,gate.y,gate.z,SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,1F,.6F);l.playSound(null,gate.x,gate.y,gate.z,SoundEvents.COW_AMBIENT,SoundSource.NEUTRAL,1.4F,.8F);
                }
                settle(herd,b,s);
            }
            case WARNING->{
                long t=now-s.getLong("At");var gate=PEN_GATES.get(s.getInt("Pen"));
                if(t%10==0)l.sendParticles(ParticleTypes.CLOUD,b.getX()+gate.getX()-1.5,b.getY()+.4,b.getZ()+gate.getZ()+.5,4,1.2,.1,1,.01);
                if(t==20)l.playSound(null,b.getX()-8.5,b.getY()+1,b.getZ()-22.5,SoundEvents.COW_AMBIENT,SoundSource.NEUTRAL,1.2F,.9F);
                settle(herd,b,s);
                if(t<WARN)return;
                s.putDouble("Lead",COWS-1);s.putInt("Phase",OUT);s.putInt("Wait",0);
            }
            case OUT->{
                var path=path(s);double lead=s.getDouble("Lead");var dir=heading(path,lead);var front=along(path,lead);
                var blocker=inWay(l,b,front,dir,1.05);int next=(int)Math.floor(lead)+1;
                // A gate swung across the way after they set off: they stop at it, and in time turn back.
                boolean leaf=next<path.size()&&leafAt(s,path.get(next).getX(),path.get(next).getZ());
                if(blocker!=null){shove(blocker,dir);s.putInt("Wait",s.getInt("Wait")+1);}
                else if(leaf)s.putInt("Wait",s.getInt("Wait")+1);
                else lead=Math.min(path.size()-1,lead+SPEED);
                s.putDouble("Lead",lead);
                if(lead>=path.size()-1||s.getInt("Wait")>=MILL){s.putInt("Phase",MILLING);s.putLong("At",now);}
                if(now%7==0)l.playSound(null,b.getX()+front.x,b.getY(),b.getZ()+front.z,SoundEvents.COW_STEP,SoundSource.NEUTRAL,.8F,.9F);
                place(herd,b,path,lead,false,l);
            }
            case MILLING->{
                var path=path(s);double lead=s.getDouble("Lead");long t=now-s.getLong("At");
                double rear=lead-(COWS-1)*SPACING;var back=heading(path,Math.max(0,rear-1)).scale(-1);
                boolean clear=inWay(l,b,along(path,rear),back,3.5)==null;
                if(t%40==0)l.playSound(null,b.getX()+along(path,lead).x,b.getY()+1,b.getZ()+along(path,lead).z,SoundEvents.COW_AMBIENT,SoundSource.NEUTRAL,1F,.85F);
                if((t>=MILL&&clear)||t>=MILL_MAX){s.putInt("Phase",BACK);s.putInt("Wait",0);}
                place(herd,b,path,lead,false,l);
            }
            case BACK,STUCK->{
                var path=path(s);double lead=s.getDouble("Lead");double rear=lead-(COWS-1)*SPACING;
                var dir=heading(path,Math.max(0,rear-1)).scale(-1);var front=along(path,rear);
                // A leaf swung across the way back after they came through: they stand where they are until it opens again.
                int ahead=(int)Math.floor(Math.max(0,rear-.01));boolean shut=false;
                for(int i=ahead;i>=0&&i>ahead-2;i--){var c=path.get(i);if(leafAt(s,c.getX(),c.getZ()))shut=true;}
                if(shut){s.putInt("Phase",STUCK);place(herd,b,path,lead,true,l);return;}
                s.putInt("Phase",BACK);
                var blocker=rear>COWS?inWay(l,b,front,dir,1.05):null;
                if(blocker!=null)shove(blocker,dir);
                else lead=Math.max(COWS-1,lead-SPEED);
                s.putDouble("Lead",lead);
                if(lead<=COWS-1){s.putInt("Phase",HOME);s.putLong("Ready",now+COOLDOWN);s.remove("Path");}
                if(now%7==0)l.playSound(null,b.getX()+front.x,b.getY(),b.getZ()+front.z,SoundEvents.COW_STEP,SoundSource.NEUTRAL,.8F,.9F);
                place(herd,b,path,lead,true,l);
            }
            default->{}
        }
    }
    /** Each cow along the drive, the lead {@code lead} cells on and the rest behind it at their spacing; facing the way they move. */
    static void place(List<Cow> herd,BlockPos b,List<BlockPos> path,double lead,boolean back,ServerLevel l){
        for(int i=0;i<herd.size();i++){var cow=herd.get(i);double d=Math.max(COWS-1-i,lead-i*SPACING);
            var at=along(path,d);var dir=heading(path,d);if(back)dir=dir.scale(-1);float yaw=(float)Math.toDegrees(Math.atan2(-dir.x,dir.z));
            var world=Vec3.atLowerCornerOf(b).add(at);double moved=cow.position().distanceTo(world);
            cow.setPos(world.x,world.y,world.z);cow.setYRot(yaw);cow.setYBodyRot(yaw);cow.setYHeadRot(yaw);cow.walkAnimation.update((float)Math.min(1,moved*4),.4F);
            for(var p:l.players())if(!p.isSpectator()&&p.getBoundingBox().intersects(cow.getBoundingBox().inflate(.05)))shove(p,dir);
        }
    }
    /** At home, in line behind the gate they last went out by, turning their heads a little. */
    static void settle(List<Cow> herd,BlockPos b,CompoundTag s){
        var gate=PEN_GATES.get(Math.max(0,Math.min(PEN_GATES.size()-1,s.getInt("Pen"))));
        for(int i=0;i<herd.size();i++){var cow=herd.get(i);var home=Vec3.atLowerCornerOf(b).add(queue(gate,i));
            if(cow.position().distanceToSqr(home)>.01){var step=home.subtract(cow.position());if(step.length()>SPEED)step=step.normalize().scale(SPEED);cow.setPos(cow.position().add(step));cow.walkAnimation.update(.6F,.4F);}
            float yaw=-90+(float)Math.sin((cow.level().getGameTime()+i*37)*.02)*25;cow.setYHeadRot(yaw);cow.setYRot(-90);cow.setYBodyRot(-90);
        }
    }
    /** Back as it was for the next crew, once nobody is in the yard or near enough to see the herd walked home. */
    static void reset(ServerLevel l,BlockPos b,CompoundTag s){
        var herd=cows(l,s);if(herd==null)return;
        for(var g:GATES)if(!g.name().equals("N")&&position(s,g)!=1){leaf(l,b,g,position(s,g),false);leaf(l,b,g,1,true);s.putInt("Gate"+g.name(),1);}
        s.putInt("Pen",0);for(int i=0;i<herd.size();i++){var home=Vec3.atLowerCornerOf(b).add(queue(PEN_GATES.get(0),i));herd.get(i).moveTo(home.x,home.y,home.z,-90,0);}
        s.putInt("Phase",HOME);s.remove("Path");s.putLong("Ready",0);
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.OUTSIDE);if(origin==null||l==null)return;
        var b=LabyrinthPlaces.base(origin,PLACE);boolean present=false,yard=false,near=false;
        for(var p:l.players()){if(p.isSpectator())continue;if(LabyrinthPlaces.placeAt(origin,p.blockPosition())==PLACE){present=true;var r=p.position().subtract(Vec3.atLowerCornerOf(b));
                if(r.z<-4.5)yard=true;
                if(r.z<-36.5&&r.x>-.2&&r.x<1.2&&r.y<2)PuzzleRooms.solve(l,PLACE,"The far gate. Behind you the cattle shift in their pen.");}
            }
        for(var p:l.players())if(p.position().distanceToSqr(Vec3.atLowerCornerOf(b))<64*64)near=true;
        var s=state(l);if(!s.getBoolean("HerdMade"))return;
        if(!present&&!near){if(s.getInt("Phase")!=HOME||position(s,GATES.get(0))!=1||position(s,GATES.get(1))!=1){int idle=s.getInt("Idle")+1;s.putInt("Idle",idle);if(idle>=RESET&&l.hasChunkAt(b)){reset(l,b,s);s.putInt("Idle",0);}save(l,s);}return;}
        s.putInt("Idle",0);
        if(!l.hasChunkAt(b.offset(-8,0,-22))||!l.hasChunkAt(b.offset(0,0,-36)))return;
        tickHerd(l,b,s,yard);save(l,s);
    }
}
