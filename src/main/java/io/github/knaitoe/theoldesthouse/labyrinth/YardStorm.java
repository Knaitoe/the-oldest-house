package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.347, the storm field (docs/YARD_STORM_0_4_347.md), from the owner's draft: "A storm field can combine those ideas without committing
 * to a full vignette. From a rise, the reader sees a farmhouse, grain elevator, and power line. At eye level in the corn, they lose all
 * three. The ditch is easy to follow until it fills. The exposed field edge gives a bearing, and the wind there drowns out footsteps.
 * Cutting across the rows is quick and leaves a track of bent stalks. A dry equipment shed midway offers a pause. From inside, you can
 * hear someone following the ditch you just abandoned. In co-op, the reader on the rise can signal until the rain takes the view away."
 *
 * <p>Through the House's back door onto a verge. To the west, a grassy rise with old steps up its flank, a windsock and a yard light on a
 * pole at the top: from there the farmhouse at the far end, the grain elevator beyond the corn, and the power line along the east edge.
 * The corn stands three high in rows running across the way north; pushing through a row is slow, and bends the stalks into a track. Down
 * the west side a drainage ditch runs the length of the field; the east edge is open grass along the wire under the power line. A machine
 * shed stands in a clearing midway. The farmhouse's back door is the way on.
 *
 * <p>While anyone is in the field the storm builds on one clock: overcast, then rain, then the storm. In the storm the ditch fills and
 * carries whoever is in it back toward the rise; lightning finds the elevator's rod. On the open edge the wind is loud. A reader in the
 * shed, once the rain has come, hears someone splashing up the ditch, out of sight. The reader on the rise can swing the yard light out
 * over the corn to where they are looking, and a light lands there above the stalks, until the storm takes the view. Nothing hurts.
 * Empty and unwatched, the field stands as it was: the corn upright, the ditch low, the sky only overcast.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class YardStorm {
    private YardStorm(){}
    public static final String STATE="yard_storm_0347";
    public static final LabyrinthPlace PLACE=LabyrinthPlace.YARD_STORM_FIELD;
    /** When the rain comes and when the storm, on the field's own clock; how long a spot of light stays; idle ticks before the reset. */
    public static final int RAIN=400,STORM=900,BOLT=300,SPOT=80,RESET=200;
    /** The yard light's pole on the rise, its switch, its lamp, and the windsock that shows the storm to anyone who looks (the client reads it too). */
    public static final BlockPos POLE=new BlockPos(-11,3,-7),SWITCH=new BlockPos(-11,4,-6),LAMP=new BlockPos(-11,9,-7),WINDSOCK=new BlockPos(-10,8,-7);
    /** The elevator's lightning rod. */
    public static final BlockPos ROD=new BlockPos(15,16,-64);
    /** The corn: x across, rows at even z across the way north. */
    public static final int CORN_X0=-15,CORN_X1=15,CORN_Z0=-14,CORN_Z1=-52;
    /** The ditch down the west side, its slab bed a block and a half below the field. */
    public static final int DITCH_X0=-19,DITCH_X1=-17,DITCH_Z0=-12,DITCH_Z1=-58;
    /** The open edge between the corn and the wire, and the wire. */
    public static final int EDGE_X=16,FENCE_X=20;
    /** The shed's dry floor. */
    public static final int SHED_X0=5,SHED_X1=9,SHED_Z0=-33,SHED_Z1=-37;
    /** The rise's top, where a reader stands to see the field. */
    public static final int TOP_X0=-13,TOP_X1=-8,TOP_Z0=-5,TOP_Z1=-9,TOP_Y=3;

    public static boolean is(LabyrinthPlace p){return p==PLACE;}
    public static CompoundTag state(ServerLevel l){return LabyrinthData.get(l.getServer()).stateEntry(STATE,PLACE.id());}
    static void save(ServerLevel l,CompoundTag s){LabyrinthData.get(l.getServer()).setStateEntry(STATE,PLACE.id(),s);}
    /** 0 overcast, 1 the rain, 2 the storm. */
    public static int stage(long clock){return clock<RAIN?0:clock<STORM?1:2;}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState st){YardRooms.box(l,b,x0,y0,z0,x1,y1,z1,st);}
    static void put(ServerLevel l,BlockPos b,int x,int y,int z,BlockState st){YardRooms.set(l,b,x,y,z,st);}
    static BlockState stalk(int stage,Direction f){return YardBlock.of(YardBlock.Kind.STALK,f,stage);}
    public static boolean cornRow(int x,int z){return x>=CORN_X0&&x<=CORN_X1&&z<=CORN_Z0&&z>=CORN_Z1&&z%2==0&&!clearing(x,z);}
    /** The shed's clearing in the corn. */
    static boolean clearing(int x,int z){return x>=2&&x<=12&&z<=-29&&z>=-39;}

    // ------------------------------------------------------------------ building
    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var grass=Blocks.GRASS_BLOCK.defaultBlockState();var dirt=Blocks.DIRT.defaultBlockState();
        box(l,b,-22,-4,-72,22,-2,0,dirt);box(l,b,-22,-1,-72,22,-1,0,grass);
        box(l,b,-22,0,-72,22,8,0,YardRooms.leaves());
        box(l,b,-20,0,-70,20,18,-2,air);box(l,b,-1,0,-2,1,4,-1,air);
        var rnd=new Random(b.asLong());
        // The verge and the headlands: rough grass, a trodden way to the rise, the ditch's head and the open edge.
        for(int x=-20;x<=20;x++)for(int z=-13;z<=-2;z++)if(rnd.nextInt(3)==0)put(l,b,x,0,z,Blocks.SHORT_GRASS.defaultBlockState());
        for(int x=-4;x<=EDGE_X+1;x++){put(l,b,x,-1,-3,Blocks.COARSE_DIRT.defaultBlockState());put(l,b,x,0,-3,air);}
        for(int z=-3;z>=-12;z--){put(l,b,EDGE_X+1,-1,z,Blocks.COARSE_DIRT.defaultBlockState());put(l,b,EDGE_X+1,0,z,air);}
        for(int x=-16;x<=-4;x++){put(l,b,x,-1,-12,Blocks.COARSE_DIRT.defaultBlockState());put(l,b,x,0,-12,air);}
        rise(l,b);ditch(l,b);corn(l,b);edge(l,b);shed(l,b);farmyard(l,b);
        LabyrinthBuilder.entrance(l,b,PuzzleRegistry.yardMaterial("clapboard").defaultBlockState(),grass,Blocks.DARK_OAK_SLAB.defaultBlockState());
        LabyrinthBuilder.doors(l,b,PLACE);
        var s=state(l);s.putLong("Clock",0);s.putInt("Drawn",-1);s.put("Spots",new ListTag());s.put("Follow",new CompoundTag());s.put("Heard",new ListTag());save(l,s);
    }
    /** Three grassy shelves to a top a block above the corn's tassels, old steps up its east flank, and the yard light's pole on it. */
    static void rise(ServerLevel l,BlockPos b){
        var grass=Blocks.GRASS_BLOCK.defaultBlockState();var dirt=Blocks.DIRT.defaultBlockState();
        box(l,b,-15,0,-11,-5,0,-3,dirt);box(l,b,-14,1,-10,-6,1,-4,dirt);box(l,b,TOP_X0,2,TOP_Z1,TOP_X1,2,TOP_Z0,dirt);
        for(int x=-15;x<=-5;x++)for(int z=-11;z<=-3;z++){int top=l.getBlockState(b.offset(x,2,z)).is(Blocks.DIRT)?2:l.getBlockState(b.offset(x,1,z)).is(Blocks.DIRT)?1:0;put(l,b,x,top,z,grass);}
        // The steps: a half-step at a time, so a reader walks up out of the verge onto the top.
        var slab=Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM);
        for(int z=-6;z>=-7;z--){put(l,b,-4,0,z,slab);put(l,b,-6,1,z,slab);put(l,b,-8,2,z,slab);}
        var log=Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
        box(l,b,POLE.getX(),POLE.getY(),POLE.getZ(),POLE.getX(),LAMP.getY()-1,POLE.getZ(),log);
        put(l,b,LAMP.getX(),LAMP.getY(),LAMP.getZ(),YardBlock.of(YardBlock.Kind.BULB,Direction.SOUTH,0));
        put(l,b,WINDSOCK.getX(),WINDSOCK.getY(),WINDSOCK.getZ(),YardBlock.of(YardBlock.Kind.WINDSOCK,Direction.EAST,0));
        put(l,b,SWITCH.getX(),SWITCH.getY(),SWITCH.getZ(),YardBlock.of(YardBlock.Kind.GATE_LATCH,Direction.SOUTH,0));
    }
    /** Down the west side, a block and a half deep: boot-deep water over slabs, a mud step to climb out every nine blocks. */
    static void ditch(ServerLevel l,BlockPos b){
        var bed=Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM).setValue(SlabBlock.WATERLOGGED,true);
        for(int z=DITCH_Z0;z>=DITCH_Z1;z--)for(int x=DITCH_X0;x<=DITCH_X1;x++){
            put(l,b,x,-3,z,Blocks.MUD.defaultBlockState());put(l,b,x,-1,z,Blocks.AIR.defaultBlockState());put(l,b,x,0,z,Blocks.AIR.defaultBlockState());
            put(l,b,x,-2,z,x==DITCH_X1&&Math.floorMod(z,9)==0?Blocks.PACKED_MUD.defaultBlockState():bed);
        }
        for(int z=DITCH_Z0;z>=DITCH_Z1;z--)for(int x:new int[]{DITCH_X0-1,DITCH_X1+1})if(Math.floorMod(z*3+x,5)==0)put(l,b,x,0,z,Blocks.TALL_GRASS.defaultBlockState().setValue(DoublePlantBlock.HALF,DoubleBlockHalf.LOWER));
        for(int z=DITCH_Z0;z>=DITCH_Z1;z--)for(int x:new int[]{DITCH_X0-1,DITCH_X1+1})if(l.getBlockState(b.offset(x,0,z)).is(Blocks.TALL_GRASS))put(l,b,x,1,z,Blocks.TALL_GRASS.defaultBlockState().setValue(DoublePlantBlock.HALF,DoubleBlockHalf.UPPER));
    }
    static void corn(ServerLevel l,BlockPos b){
        for(int x=CORN_X0;x<=CORN_X1;x++)for(int z=CORN_Z0;z>=CORN_Z1;z--){
            if(clearing(x,z)){put(l,b,x,-1,z,Blocks.COARSE_DIRT.defaultBlockState());continue;}
            put(l,b,x,-1,z,Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE,7));
            if(z%2==0)plant(l,b,x,z);
        }
    }
    static void plant(ServerLevel l,BlockPos b,int x,int z){var f=Direction.from2DDataValue(Math.floorMod(x*7+z*13,4));for(int y=0;y<3;y++)put(l,b,x,y,z,stalk(y,f));}
    /** Open grass along the wire, the power line on its poles above it, running north to the elevator. */
    static void edge(ServerLevel l,BlockPos b){
        for(int z=-12;z>=-58;z--){put(l,b,FENCE_X,0,z,Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH,z>-58).setValue(FenceBlock.SOUTH,z<-12));}
        var log=Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();var wire=Blocks.CHAIN.defaultBlockState().setValue(ChainBlock.AXIS,Direction.Axis.Z);
        for(int z=-12;z>=-56;z-=11){box(l,b,18,0,z,18,7,z,log);put(l,b,17,6,z,Blocks.SPRUCE_FENCE.defaultBlockState());put(l,b,19,6,z,Blocks.SPRUCE_FENCE.defaultBlockState());}
        for(int z=-12;z>=-59;z--)for(int x:new int[]{17,19})if(l.getBlockState(b.offset(x,6,z)).isAir())put(l,b,x,6,z,wire);
    }
    /** An open-fronted machine shed in a clearing: plank sides, a roof of barn sheets, a lamp, hay and the gear under cover. */
    static void shed(ServerLevel l,BlockPos b){
        var plank=Blocks.SPRUCE_PLANKS.defaultBlockState();var air=Blocks.AIR.defaultBlockState();
        box(l,b,SHED_X0-1,0,SHED_Z0+1,SHED_X1+1,3,SHED_Z1-1,plank);box(l,b,SHED_X0,0,SHED_Z0+1,SHED_X1,3,SHED_Z1,air);
        for(int z:new int[]{-34,-36})put(l,b,SHED_X0-1,2,z,air);
        for(int x=SHED_X0-2;x<=SHED_X1+2;x++)for(int z=SHED_Z0+2;z>=SHED_Z1-2;z--)put(l,b,x,4,z,YardBlock.of(YardBlock.Kind.ROOF_SHEET,Direction.EAST,0));
        box(l,b,SHED_X0,-1,SHED_Z0+1,SHED_X1,-1,SHED_Z1,Blocks.PACKED_MUD.defaultBlockState());
        put(l,b,SHED_X1,0,SHED_Z1,Blocks.HAY_BLOCK.defaultBlockState());put(l,b,SHED_X1,1,SHED_Z1,Blocks.HAY_BLOCK.defaultBlockState());put(l,b,SHED_X1-1,0,SHED_Z1,Blocks.HAY_BLOCK.defaultBlockState());
        put(l,b,SHED_X0,0,SHED_Z1,Blocks.BARREL.defaultBlockState());put(l,b,SHED_X0,0,SHED_Z1+1,Blocks.CAULDRON.defaultBlockState());
        put(l,b,SHED_X0,0,SHED_Z0,Blocks.GRINDSTONE.defaultBlockState().setValue(GrindstoneBlock.FACE,AttachFace.FLOOR).setValue(GrindstoneBlock.FACING,Direction.EAST));
        put(l,b,SHED_X1,0,SHED_Z0-1,Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING,Direction.NORTH));
        put(l,b,7,3,-35,YardBlock.of(YardBlock.Kind.BULB,Direction.SOUTH,0));
    }
    /** The farmyard: gravel before the farmhouse's back porch, the house two storeys under a pitched roof, the elevator to the east. */
    static void farmyard(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var clap=PuzzleRegistry.yardMaterial("clapboard").defaultBlockState();
        for(int x=-20;x<=20;x++)for(int z=-53;z>=-61;z--)if(l.getBlockState(b.offset(x,-1,z)).is(Blocks.GRASS_BLOCK)&&(x>=-6&&x<=6||Math.floorMod(x*5+z*3,4)!=0))put(l,b,x,-1,z,Blocks.GRAVEL.defaultBlockState());
        // The house.
        box(l,b,-8,-1,-62,8,7,-70,clap);box(l,b,-7,0,-63,7,6,-69,air);box(l,b,-7,-1,-63,7,-1,-69,Blocks.SPRUCE_PLANKS.defaultBlockState());
        box(l,b,-7,3,-63,7,3,-69,Blocks.SPRUCE_PLANKS.defaultBlockState());
        for(int x:new int[]{-6,-5,4,5})for(int y:new int[]{1,5})put(l,b,x,y,-62,Blocks.GLASS_PANE.defaultBlockState().setValue(IronBarsBlock.EAST,true).setValue(IronBarsBlock.WEST,true));
        put(l,b,-5,1,-64,Blocks.LANTERN.defaultBlockState());put(l,b,5,5,-64,Blocks.LANTERN.defaultBlockState());
        for(int i=0;i<=4;i++){
            var south=Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH);var north=Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH);
            for(int x=-9;x<=9;x++){put(l,b,x,8+i,-61-i,south);put(l,b,x,8+i,-71+i,north);}
            if(i>0)for(int z=-61-i;z>=-71+i;z--)for(int x:new int[]{-8,8})put(l,b,x,7+i,z,clap);
        }
        box(l,b,-9,13,-66,9,13,-66,Blocks.DARK_OAK_SLAB.defaultBlockState());
        box(l,b,4,8,-67,5,14,-67,Blocks.BRICKS.defaultBlockState());
        // The back porch under its own roof; its door is the way on.
        box(l,b,-3,-1,-61,3,-1,-60,Blocks.SPRUCE_PLANKS.defaultBlockState());
        for(int x:new int[]{-3,3})box(l,b,x,0,-60,x,2,-60,Blocks.SPRUCE_FENCE.defaultBlockState());
        box(l,b,-3,3,-61,3,3,-60,Blocks.SPRUCE_SLAB.defaultBlockState());
        put(l,b,-2,2,-61,YardBlock.of(YardBlock.Kind.BULB,Direction.SOUTH,0));
        // The elevator: a tall bin with its headhouse and a rod on top, the power line's end.
        var concrete=Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
        for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)if(dx*dx+dz*dz<=10)box(l,b,15+dx,0,-64+dz,15+dx,12,-64+dz,(dx+dz)%4==0?Blocks.SMOOTH_STONE.defaultBlockState():concrete);
        box(l,b,13,13,-66,17,15,-62,Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState());
        put(l,b,ROD.getX(),ROD.getY(),ROD.getZ(),Blocks.LIGHTNING_ROD.defaultBlockState());
    }

    // ------------------------------------------------------------------ the corn under a reader
    /** Pushed through by a reader: the stalk goes over the way they are going, and the two above it come down. */
    public static void bend(ServerLevel l,BlockPos at,BlockState s,ServerPlayer p){
        int stage=s.getValue(YardBlock.STAGE);if(stage>2)return;
        var base=at.below(stage);var v=p.getDeltaMovement();
        var f=v.horizontalDistanceSqr()>1e-4?Direction.getNearest(v.x,0,v.z):p.getDirection();
        l.setBlock(base,stalk(3,f),Block.UPDATE_CLIENTS);
        for(int y=1;y<=2;y++){var up=base.above(y);if(YardBlock.is(l.getBlockState(up),YardBlock.Kind.STALK))l.setBlock(up,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);}
        l.playSound(null,base,SoundEvents.AZALEA_LEAVES_BREAK,SoundSource.BLOCKS,.5F,.7F);
    }

    // ------------------------------------------------------------------ the storm
    /** The water in the ditch: boot-deep over its slabs, or up to the brim. */
    static void ditchWater(ServerLevel l,BlockPos b,boolean full){
        for(int z=DITCH_Z0;z>=DITCH_Z1;z--)for(int x=DITCH_X0;x<=DITCH_X1;x++){
            var at=b.offset(x,-1,z);var old=l.getBlockState(at);
            if(full&&(old.isAir()||old.is(Blocks.WATER)&&!old.getFluidState().isSource()))l.setBlock(at,Blocks.WATER.defaultBlockState(),Block.UPDATE_ALL);
            else if(!full&&old.is(Blocks.WATER))l.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);
        }
    }
    static void windsock(ServerLevel l,BlockPos b,int stage){var at=b.offset(WINDSOCK);var st=l.getBlockState(at);if(YardBlock.is(st,YardBlock.Kind.WINDSOCK)&&st.getValue(YardBlock.STAGE)!=stage)l.setBlock(at,st.setValue(YardBlock.STAGE,stage),Block.UPDATE_CLIENTS);}
    public static boolean onTop(BlockPos b,Vec3 pos){var r=pos.subtract(Vec3.atLowerCornerOf(b));return r.y>TOP_Y-.1&&r.y<TOP_Y+1&&r.x>TOP_X0&&r.x<TOP_X1+1&&r.z<TOP_Z0+1&&r.z>TOP_Z1;}
    public static boolean inShed(BlockPos b,Vec3 pos){var r=pos.subtract(Vec3.atLowerCornerOf(b));return r.x>SHED_X0&&r.x<SHED_X1+1&&r.z<SHED_Z0+1&&r.z>SHED_Z1&&r.y>-.5&&r.y<3;}
    public static boolean inDitch(BlockPos b,Vec3 pos){var r=pos.subtract(Vec3.atLowerCornerOf(b));return r.x>DITCH_X0&&r.x<DITCH_X1+1&&r.z<DITCH_Z0+1&&r.z>DITCH_Z1&&r.y<.5;}
    public static boolean onEdge(BlockPos b,Vec3 pos){var r=pos.subtract(Vec3.atLowerCornerOf(b));return r.x>EDGE_X&&r.x<FENCE_X&&r.z<-11&&r.z>-59;}
    static void hear(ServerPlayer p,SoundEvent sound,Vec3 at,float volume,float pitch){p.connection.send(new ClientboundSoundPacket(Holder.direct(sound),SoundSource.AMBIENT,at.x,at.y,at.z,volume,pitch,p.getRandom().nextLong()));}

    static void tickStorm(ServerLevel l,BlockPos b,CompoundTag s,List<ServerPlayer> here){
        long clock=s.getLong("Clock")+1;s.putLong("Clock",clock);int stage=stage(clock);
        if(stage!=s.getInt("Drawn")){
            windsock(l,b,stage);ditchWater(l,b,stage==2);
            if(stage>0)l.playSound(null,b.getX(),b.getY()+14,b.getZ()-50,SoundEvents.LIGHTNING_BOLT_THUNDER,SoundSource.WEATHER,stage==1?.5F:1.2F,.8F);
            s.putInt("Drawn",stage);
        }
        if(clock==RAIN/2)l.playSound(null,b.getX(),b.getY()+12,b.getZ()-90,SoundEvents.LIGHTNING_BOLT_THUNDER,SoundSource.WEATHER,.3F,.7F);
        // In the storm, lightning on the elevator's rod.
        if(stage==2&&(clock-STORM)%BOLT==BOLT/2){var bolt=EntityType.LIGHTNING_BOLT.create(l);if(bolt!=null){bolt.moveTo(Vec3.atBottomCenterOf(b.offset(ROD).above()));bolt.setVisualOnly(true);l.addFreshEntity(bolt);}}
        if(stage==2){
            // The ditch full and running back toward the rise.
            var ditch=new AABB(b.getX()+DITCH_X0,b.getY()-2,b.getZ()+DITCH_Z1,b.getX()+DITCH_X1+1,b.getY()+1,b.getZ()+DITCH_Z0+1);
            for(var e:l.getEntitiesOfClass(LivingEntity.class,ditch,e->e.isAlive()&&!e.isSpectator())){var v=e.getDeltaMovement();e.setDeltaMovement(Math.max(-.15,Math.min(.15,v.x)),v.y,Math.min(.28,Math.max(v.z,0)+.05));e.hurtMarked=true;e.setAirSupply(e.getMaxAirSupply());}
            if(clock%6==0)l.sendParticles(ParticleTypes.SPLASH,b.getX()+DITCH_X0+1.5,b.getY()-.1,b.getZ()+(DITCH_Z0+DITCH_Z1)/2.0,10,.8,.05,20,.05);
        }
        var heard=s.getList("Heard",Tag.TAG_STRING);var follow=s.getCompound("Follow");
        for(var p:here){
            var pos=p.position();
            // On the open edge, the wind: loud enough to cover a footstep.
            if(stage>0&&onEdge(b,pos)&&clock%30==0){hear(p,PuzzleRegistry.YARD_WIND.get(),pos.add(-6,2,0),stage==1?1.1F:1.6F,.9F);l.sendParticles(p,new BlockParticleOption(ParticleTypes.BLOCK,Blocks.SHORT_GRASS.defaultBlockState()),true,pos.x-4,pos.y+.6,pos.z,8,1.5,.4,1.5,.08);}
            // In the shed, once the rain has come: someone splashing up the ditch, out of sight in the corn.
            var id=p.getUUID().toString();
            if(stage>0&&inShed(b,pos)&&!heard.contains(net.minecraft.nbt.StringTag.valueOf(id))){heard.add(net.minecraft.nbt.StringTag.valueOf(id));follow.putLong(id,clock);PuzzleRooms.say(p,"Under the roof it is dry. The rain is loud on the sheets.");}
        }
        for(var id:List.copyOf(follow.getAllKeys())){
            long t=clock-follow.getLong(id);var p=l.getServer().getPlayerList().getPlayer(UUID.fromString(id));
            if(t>FOLLOW_STEPS*FOLLOW_PACE||p==null||p.level()!=l){follow.remove(id);continue;}
            if(t%FOLLOW_PACE!=0)continue;int step=(int)(t/FOLLOW_PACE);var at=Vec3.atLowerCornerOf(b).add(DITCH_X0+1.5,-1,FOLLOW_FROM-step+.5);
            hear(p,step%2==0?PuzzleRegistry.YARD_FOLLOW.get():SoundEvents.PLAYER_SPLASH,at,step%2==0?2.2F:1.4F,step%2==0?.9F:1.3F);
            l.sendParticles(p,new BlockParticleOption(ParticleTypes.BLOCK,Blocks.SHORT_GRASS.defaultBlockState()),true,at.x+2.5,at.y+2.5,at.z,4,.3,.6,.3,.05);
        }
        s.put("Heard",heard);s.put("Follow",follow);
        // Spots of light from the yard light go out after a while.
        var spots=s.getList("Spots",Tag.TAG_COMPOUND);var keep=new ListTag();
        for(int i=0;i<spots.size();i++){var spot=spots.getCompound(i);if(spot.getLong("Until")>clock){keep.add(spot);continue;}var at=b.offset(spot.getInt("X"),spot.getInt("Y"),spot.getInt("Z"));if(l.getBlockState(at).is(Blocks.LIGHT))l.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);}
        s.put("Spots",keep);
    }
    /** Where the footsteps in the ditch start (a little south of the shed's latitude), how many there are and how far apart in ticks. */
    public static final int FOLLOW_FROM=-22,FOLLOW_STEPS=26,FOLLOW_PACE=8;

    // ------------------------------------------------------------------ the yard light
    /** From the rise, the switch swings the yard light out over the corn to where the reader is looking, while the rain still lets it. */
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState st){
        var o=HouseSavedData.get(p.server).houseOrigin();if(o==null||LabyrinthPlaces.placeAt(o,pos)!=PLACE)return false;
        var l=p.serverLevel();var b=LabyrinthPlaces.base(o,PLACE);var rel=pos.subtract(b);
        if(YardBlock.is(st,YardBlock.Kind.WINDSOCK)){PuzzleRooms.say(p,switch(st.getValue(YardBlock.STAGE)){case 0->"The windsock hangs slack on its hoop.";case 1->"The windsock lifts and fills, pointing east.";default->"The windsock stands out stiff, snapping in the rain.";});return true;}
        if(YardBlock.is(st,YardBlock.Kind.STALK)){PuzzleRooms.say(p,st.getValue(YardBlock.STAGE)==3?"Stalks pushed flat, all leaning one way.":"Corn, well over your head. The leaves are sharp at the edges.");return true;}
        if(rel.equals(LAMP)){PuzzleRooms.say(p,"The yard light, up on its pole. Its switch is lower down, on the pole.");return true;}
        if(rel.equals(new BlockPos(7,3,-35))){PuzzleRooms.say(p,"A bulb on a cord under the roof sheets.");return true;}
        if(!rel.equals(SWITCH))return false;
        signal(l,b,p);return true;
    }
    /** Swings the yard light; returns where it landed (local), or null. */
    public static BlockPos signal(ServerLevel l,BlockPos b,ServerPlayer p){
        var s=state(l);int stage=stage(s.getLong("Clock"));
        l.playSound(null,b.offset(SWITCH),SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.6F,.8F);
        if(stage==2){PuzzleRooms.say(p,"The rain has taken the field. The light reaches the first rows and no further.");return null;}
        int range=stage==0?56:24;var eye=p.getEyePosition();var hit=l.clip(new ClipContext(eye,eye.add(p.getLookAngle().scale(range)),ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,p));
        if(hit.getType()!=HitResult.Type.BLOCK){PuzzleRooms.say(p,stage==0?"The light runs out over the field and finds nothing to land on.":"The light runs out into the rain and is lost.");return null;}
        var cell=hit.getBlockPos().subtract(b);var spot=new BlockPos(cell.getX(),Math.max(cell.getY()+1,TOP_Y),cell.getZ());
        if(!(Math.abs(spot.getX())<=20&&spot.getZ()<=-2&&spot.getZ()>=-70)){PuzzleRooms.say(p,"The light runs out over the field and finds nothing to land on.");return null;}
        var at=b.offset(spot);if(l.getBlockState(at).isAir()||l.getBlockState(at).is(Blocks.LIGHT))l.setBlock(at,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,13),Block.UPDATE_CLIENTS);
        var spots=s.getList("Spots",Tag.TAG_COMPOUND);var tag=new CompoundTag();tag.putInt("X",spot.getX());tag.putInt("Y",spot.getY());tag.putInt("Z",spot.getZ());tag.putLong("Until",s.getLong("Clock")+SPOT);spots.add(tag);s.put("Spots",spots);save(l,s);
        // The beam, seen by everyone in the field who can still see that far.
        var from=Vec3.atCenterOf(b.offset(LAMP));var to=Vec3.atCenterOf(at);int steps=(int)(from.distanceTo(to)/1.5);
        for(var q:l.players()){if(!present(b,q.position()))continue;
            for(int i=1;i<=steps;i++){var c=from.lerp(to,i/(double)steps);l.sendParticles(q,ParticleTypes.END_ROD,true,c.x,c.y,c.z,1,0,0,0,0);}
            l.sendParticles(q,ParticleTypes.END_ROD,true,to.x,to.y,to.z,14,.6,.4,.6,.02);}
        PuzzleRooms.say(p,"You swing the yard light out over the corn. It lands where you are looking.");
        return spot;
    }
    static boolean present(BlockPos b,Vec3 pos){var r=pos.subtract(Vec3.atLowerCornerOf(b));return r.x>-21&&r.x<21&&r.z<1&&r.z>-71&&r.y>-4&&r.y<19;}

    // ------------------------------------------------------------------ the field as it was
    public static void reset(ServerLevel l,BlockPos b,CompoundTag s){
        for(int x=CORN_X0;x<=CORN_X1;x++)for(int z=CORN_Z0;z>=CORN_Z1;z--)if(cornRow(x,z)){
            boolean whole=true;for(int y=0;y<3;y++){var st=l.getBlockState(b.offset(x,y,z));if(!YardBlock.is(st,YardBlock.Kind.STALK)||st.getValue(YardBlock.STAGE)!=y)whole=false;}
            if(!whole)plant(l,b,x,z);
        }
        ditchWater(l,b,false);windsock(l,b,0);
        var spots=s.getList("Spots",Tag.TAG_COMPOUND);for(int i=0;i<spots.size();i++){var spot=spots.getCompound(i);var at=b.offset(spot.getInt("X"),spot.getInt("Y"),spot.getInt("Z"));if(l.getBlockState(at).is(Blocks.LIGHT))l.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);}
        s.putLong("Clock",0);s.putInt("Drawn",0);s.putInt("Idle",0);s.put("Spots",new ListTag());s.put("Follow",new CompoundTag());s.put("Heard",new ListTag());save(l,s);
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.OUTSIDE);if(origin==null||l==null)return;
        var b=LabyrinthPlaces.base(origin,PLACE);boolean near=false;var here=new ArrayList<ServerPlayer>();
        for(var p:l.players()){if(!p.isSpectator()&&LabyrinthPlaces.placeAt(origin,p.blockPosition())==PLACE)here.add(p);if(p.position().distanceToSqr(Vec3.atLowerCornerOf(b).add(0,0,-36))<80*80)near=true;}
        var s=state(l);if(!s.contains("Clock"))return;
        boolean loaded=l.hasChunkAt(b.offset(-18,0,-12))&&l.hasChunkAt(b.offset(18,0,-58))&&l.hasChunkAt(b.offset(-18,0,-58))&&l.hasChunkAt(b.offset(18,0,-12));
        if(here.isEmpty()){if(!near&&s.getLong("Clock")>0){int idle=s.getInt("Idle")+1;s.putInt("Idle",idle);if(idle>=RESET&&loaded)reset(l,b,s);else save(l,s);}return;}
        s.putInt("Idle",0);if(!loaded)return;tickStorm(l,b,s,here);save(l,s);
    }
}
