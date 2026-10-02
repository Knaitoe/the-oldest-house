package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.properties.*;

/** A dirt hut followed by three connected, physically traversable hunt arenas. */
public final class HollowayCamp {
    public static final BlockPos CACHE=new BlockPos(-4,0,-7),JOURNAL=new BlockPos(3,0,-8),LATCH=new BlockPos(-9,1,-66);
    private HollowayCamp(){}
    public static void build(ServerLevel level,BlockPos base){
        var dirt=Blocks.DIRT.defaultBlockState();var stone=Blocks.STONE_BRICKS.defaultBlockState();
        LabyrinthBuilder.room(level,base,-6,6,4,-12,0,dirt,Blocks.COARSE_DIRT.defaultBlockState(),dirt);
        LabyrinthBuilder.room(level,base,-10,10,5,-24,-13,stone,Blocks.STONE.defaultBlockState(),stone);
        LabyrinthBuilder.room(level,base,-8,8,5,-46,-25,Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState(),Blocks.SPRUCE_PLANKS.defaultBlockState(),stone);
        LabyrinthBuilder.room(level,base,-10,10,5,-69,-47,stone,Blocks.TUFF.defaultBlockState(),Blocks.DEEPSLATE_TILES.defaultBlockState());
        for(int z:new int[]{-12,-13,-24,-25,-46,-47})for(int x=-1;x<=1;x++)for(int y=0;y<=3;y++)level.setBlock(base.offset(x,y,z),Blocks.AIR.defaultBlockState(),3);
        for(int z=-3;z>=-61;z-=8)level.setBlock(base.offset(2,0,z),Blocks.TORCH.defaultBlockState(),3);
        // Real sight breaks: the route remains at least two blocks wide around each.
        for(int z:new int[]{-17,-21})for(int x:new int[]{-4,4})for(int y=0;y<4;y++)level.setBlock(base.offset(x,y,z),stone,3);
        for(int z:new int[]{-30,-39})for(int x=-2;x<=2;x++)for(int y=0;y<2;y++)level.setBlock(base.offset(x,y,z),Blocks.BOOKSHELF.defaultBlockState(),3);
        level.setBlock(base.offset(6,0,-34),LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS,Direction.WEST),3);
        level.setBlock(base.offset(7,0,-34),Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,Half.TOP),3);
        for(int z:new int[]{-53,-58})for(int x:new int[]{-5,4})for(int y=0;y<3;y++)level.setBlock(base.offset(x,y,z),Blocks.COBBLED_DEEPSLATE.defaultBlockState(),3);
        level.setBlock(base.offset(0,0,-5),Blocks.CAMPFIRE.defaultBlockState(),3);
        level.setBlock(base.offset(-4,0,-4),Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.PART,BedPart.FOOT).setValue(BedBlock.FACING,Direction.NORTH),3);
        level.setBlock(base.offset(-4,0,-5),Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.PART,BedPart.HEAD).setValue(BedBlock.FACING,Direction.NORTH),3);
        level.setBlock(base.offset(CACHE),Blocks.BARREL.defaultBlockState(),3);
        if(level.getBlockEntity(base.offset(CACHE)) instanceof BarrelBlockEntity cache){
            cache.setItem(0,new ItemStack(Items.TORCH,16));cache.setItem(1,new ItemStack(Items.COOKED_BEEF,4));
            cache.setItem(2,new ItemStack(Items.STONE_PICKAXE));cache.setChanged();
        }
        level.setBlock(base.offset(JOURNAL),Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.HAS_BOOK,true),3);
        if(level.getBlockEntity(base.offset(JOURNAL)) instanceof LecternBlockEntity desk)desk.setBook(journal());
        sign(level,base.offset(-5,1,-10),"SUPPLIES","I counted them.","Leave the torch.","");
        sign(level,base.offset(-8,1,-19),"I went this way.","The wall moved.","It did not move", "when I watched.");
        sign(level,base.offset(-6,1,-43),"YOUR LIGHT", "IS NOT A WAY", "OUT.","");
        level.setBlock(base.offset(LATCH).below(),Blocks.SMOOTH_STONE.defaultBlockState(),3);
        level.setBlock(base.offset(LATCH),Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE,AttachFace.FLOOR),3);
        LabyrinthBuilder.entrance(level,base,dirt,Blocks.COARSE_DIRT.defaultBlockState(),dirt);
        LabyrinthBuilder.doors(level,base,LabyrinthPlace.HOLLOWAY_CAMP);
        HollowayVignette.ensureActor(level,base);
    }
    private static void sign(ServerLevel level,BlockPos at,String a,String b,String c,String d){
        level.setBlock(at.below(),Blocks.SPRUCE_SIGN.defaultBlockState(),3);at=at.below();
        if(level.getBlockEntity(at) instanceof SignBlockEntity sign){var text=sign.getFrontText();String[] lines={a,b,c,d};for(int i=0;i<4;i++)text=text.setMessage(i,net.minecraft.network.chat.Component.literal(lines[i]));sign.setText(text,true);sign.setWaxed(true);sign.setChanged();}
    }
    public static void repairSigns(ServerLevel level,BlockPos base){
        var data=LabyrinthData.get(level.getServer());var all=data.state(HollowayVignette.ID);if(all.getBoolean("GroundedSigns"))return;
        for(BlockPos old:new BlockPos[]{base.offset(-5,1,-10),base.offset(-8,1,-19),base.offset(-6,1,-43)})
            if(level.getBlockEntity(old) instanceof SignBlockEntity source&&level.getBlockState(old.below()).isAir()){
                var front=source.getFrontText();var back=source.getBackText();level.setBlock(old,Blocks.AIR.defaultBlockState(),3);level.setBlock(old.below(),Blocks.SPRUCE_SIGN.defaultBlockState(),3);
                if(level.getBlockEntity(old.below()) instanceof SignBlockEntity target){target.setText(front,true);target.setText(back,false);target.setWaxed(true);target.setChanged();}
            }
        all.putBoolean("GroundedSigns",true);data.setState(HollowayVignette.ID,all);
    }
    public static ItemStack journal(){return HouseWriting.book("Holloway's survey","Holloway",HouseWriting.WritingStyle.WILL,java.util.List.of(
            "No tent left. Dirt holds warmth better than canvas. I have food, a pickaxe and sixteen torches.\n\nLeave them where they are. I will know.\n\nA copy of this survey is still a thing taken.",
            "Three rooms north of the hut. Pillars. A room with somebody's stairs. Broken stone.\n\nA service door stands beyond them. The latch is on the left.",
            "The map ends halfway down the page. The marks that look like rooms are guesses.\n\nI followed another man's torches once. They brought me to his back.",
            "If you take something, you will have to come back.\n\nThen go north. Keep moving. Pull the latch at the service door. A shield may buy enough time.\n\nI counted the torches."));}
}
