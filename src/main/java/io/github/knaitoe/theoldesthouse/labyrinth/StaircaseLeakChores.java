package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import static io.github.knaitoe.theoldesthouse.labyrinth.StaircaseLeakProps.Kind.*;

/** Native interactions and read-only prop menus. No real inventory slots are exposed. */
public final class StaircaseLeakChores {
    private StaircaseLeakChores(){}
    private static void put(ServerPlayer p,BlockPos at,StaircaseLeakProps.Kind kind){var old=p.serverLevel().getBlockState(at);var s=StaircaseLeakProps.state(kind);if(old.hasProperty(BlockStateProperties.HORIZONTAL_FACING))s=s.setValue(BlockStateProperties.HORIZONTAL_FACING,old.getValue(BlockStateProperties.HORIZONTAL_FACING));p.serverLevel().setBlock(at,s,Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}
    private static void remove(ServerPlayer p,BlockPos at){p.serverLevel().setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}
    private static void cue(ServerPlayer p,String text){StaircaseLeaks.caption(p,text);p.playNotifySound(SoundEvents.WOOL_PLACE,SoundSource.BLOCKS,.18F,1);}
    private static void blocked(ServerPlayer p,BlockPos at,String text){ExpeditionRhythm.refuse(p,at,"note_scene_chore",text);p.playNotifySound(SoundEvents.WOOL_PLACE,SoundSource.BLOCKS,.18F,1);}
    public static boolean interact(ServerPlayer p,BlockPos at){
        if(!StaircaseLeaks.active(p)||p.isSpectator()||p.distanceToSqr(at.getCenter())>36)return false;
        var b=StaircaseLeaks.activeBase(p);var state=p.serverLevel().getBlockState(at);var own=StaircaseLeaks.progress(p);int index=StaircaseLeaks.activeIndex(p);
        if(state.getBlock() instanceof DoorBlock&&at.getX()==b.getX()&&at.getZ()==b.getZ()){
            var lower=state.getValue(DoorBlock.HALF)==DoubleBlockHalf.UPPER?at.below():at;
            for(var cell:List.of(lower,lower.above())){var s=p.serverLevel().getBlockState(cell);if(s.getBlock() instanceof DoorBlock)p.serverLevel().setBlock(cell,s.setValue(DoorBlock.OPEN,!state.getValue(DoorBlock.OPEN)),3);}p.playNotifySound(SoundEvents.WOODEN_DOOR_OPEN,SoundSource.BLOCKS,.4F,1);return true;
        }
        if(!state.is(StaircaseLeakRegistry.PROP.get()))return false;var kind=state.getValue(StaircaseLeakProps.KIND);
        switch(index){
            case 0->{
                if(kind==TOWEL){own.putBoolean("Towel",true);cue(p,"The cotton is warm, wet at one end.");}
                else if(kind==CUP_BLUE||kind==CUP_CREAM||kind==CUP_RED){
                    if(!own.getBoolean("Towel")){blocked(p,at,"The rim is still wet. The towel is on the rail.");break;}
                    if(own.contains("HeldCup")){blocked(p,at,"There is a cup in your other hand.");break;}
                    int color=kind==CUP_BLUE?0:kind==CUP_CREAM?1:2;own.putInt("HeldCup",color);remove(p,at);cue(p,"The last drop catches in the towel.");
                }else if(kind==HOOK){
                    if(!own.contains("HeldCup")){blocked(p,at,"An empty hook.");break;}int color=own.getInt("HeldCup");put(p,at,new StaircaseLeakProps.Kind[]{CUP_HUNG_BLUE,CUP_HUNG_CREAM,CUP_HUNG_RED}[color]);own.remove("HeldCup");own.putInt("Cups",own.getInt("Cups")+1);
                    int[] colors=own.getIntArray("HookColors");if(colors.length!=3)colors=new int[]{0,1,2};int hook=at.getX()-b.getX()+2;if(hook>=0&&hook<3)colors[hook]=color;own.putIntArray("HookColors",colors);cue(p,"The cup taps the wood.");
                }else if(kind==PLATE){if(own.getInt("Cups")!=3){blocked(p,at,"The cups need their hooks first.");break;}own.putBoolean("Plate",true);remove(p,at);cue(p,"There is no hook for the cracked plate.");}
                else if(kind==GERANIUM){if(!own.getBoolean("Plate")){blocked(p,at,"Hang the cups, then set the cracked plate beneath the pot.");break;}put(p,at,GERANIUM_PLATE);cue(p,"It fits beneath the pot. The tap keeps dripping.");StaircaseLeaks.progress(p,own);StaircaseLeaks.returnAfterBeat(p,20);return true;}
            }
            case 12->{
                if(kind==LAUNDRY||kind==SOCK_SINGLE){p.openMenu(new SimpleMenuProvider((id,inv,reader)->new SockMenu(id,p,at),Component.literal("Saturday washing")));return true;}
                if(kind==TROUSERS){if(own.getInt("Pairs")<5){blocked(p,at,"Five pairs are still warm from the line.");break;}own.putBoolean("ListHeld",true);cue(p,"From the landing: We forgot the matches.");StaircaseLeaks.progress(p,own);readList(p);return true;}
                if(kind==LIST_SPOT){if(!own.getBoolean("ListHeld")){blocked(p,at,"Pair the socks, then check the trouser pocket for the list.");break;}put(p,at,LIST);cue(p,"The list lies flat. One sock is left over.");StaircaseLeaks.progress(p,own);StaircaseLeaks.returnAfterBeat(p,20);return true;}
            }
            case 6->{if(kind==RADIO){put(p,at,RADIO_OFF);own.putBoolean("RadioOff",true);cue(p,"That was the pear tree. Its roots were lifting the pavement.");p.playNotifySound(StaircaseLeakRegistry.BAG.get(),SoundSource.BLOCKS,.22F,1);StaircaseLeaks.progress(p,own);StaircaseLeaks.returnAfterBeat(p,60);return true;}}
            case 9->{
                if(kind==CANDLE){own.putBoolean("Candle",true);cue(p,"The end of the candle is soft.");}
                if(kind==DRAWER||kind==DRAWER_OPEN){if(!own.getBoolean("Waxed")&&!own.getBoolean("Candle")){blocked(p,at,"The runner sticks. Take the candle from the table.");break;}
                    own.putBoolean("Waxed",true);boolean open=kind==DRAWER;put(p,at,open?DRAWER_OPEN:DRAWER);cue(p,open?"Wax along the runner. A tin of spare buttons.":"The drawer closes without a sound.");}
            }
            case 32->{
                if(kind==SINK_EMPTY){put(p,at,SINK_CLEAN);own.putBoolean("Rinsed",true);cue(p,"A little water takes the last mark away.");}
                if(kind==LIGHT){if(!own.getBoolean("Rinsed")){blocked(p,at,"The sink needs one last rinse.");break;}put(p,at,LIGHT_OFF);cue(p,"Only the light over the cooker remains.");StaircaseLeaks.progress(p,own);StaircaseLeaks.returnAfterBeat(p,25);return true;}
            }
            default->{return false;}
        }
        StaircaseLeaks.progress(p,own);return true;
    }
    private static void readList(ServerPlayer p){
        var page=Component.empty().append(Component.literal("bread\nmilk\ntea\nsoap\n").withStyle(ChatFormatting.STRIKETHROUGH)).append("matches");var book=HouseWriting.book("Shopping","",List.of(page));
        p.openMenu(new SimpleMenuProvider((id,inv,reader)->new ListMenu(id,p,book),Component.literal("Shopping")));
    }
    private static final class ListMenu extends LecternMenu {
        private final ServerPlayer reader;
        ListMenu(int id,ServerPlayer p,ItemStack book){this(id,p,book,new SimpleContainer(1));}
        ListMenu(int id,ServerPlayer p,ItemStack book,SimpleContainer display){super(id,display,new SimpleContainerData(1));reader=p;display.setItem(0,book.copy());}
        @Override public boolean stillValid(Player p){return p==reader&&StaircaseLeaks.active(reader)&&StaircaseLeaks.activeIndex(reader)==12;}
        @Override public boolean clickMenuButton(Player p,int button){return button!=3&&stillValid(p)&&super.clickMenuButton(p,button);}
        @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
    }
    private static final class SockMenu extends ChestMenu {
        private final ServerPlayer reader;private final BlockPos at;private final SimpleContainer display;
        // Five color pairs and an unmatched sock. Reopening keeps completed pairs empty.
        private static final int[] COLORS={0,3,1,4,2,0,4,1,3,2,5};
        SockMenu(int id,ServerPlayer reader,BlockPos at){this(id,reader,at,new SimpleContainer(27));}
        SockMenu(int id,ServerPlayer reader,BlockPos at,SimpleContainer display){super(MenuType.GENERIC_9x3,id,reader.getInventory(),display,3);this.reader=reader;this.at=at;this.display=display;refresh();}
        private void refresh(){var own=StaircaseLeaks.progress(reader);for(int i=0;i<COLORS.length;i++)display.setItem(i,own.getBoolean("Sock"+i)?ItemStack.EMPTY:new ItemStack(StaircaseLeakRegistry.SOCKS.get(COLORS[i]).get()));broadcastChanges();}
        @Override public boolean stillValid(Player p){return p==reader&&StaircaseLeaks.active(reader)&&StaircaseLeaks.activeIndex(reader)==12&&reader.distanceToSqr(at.getCenter())<36;}
        @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
        @Override public void clicked(int slot,int button,ClickType type,Player p){
            if(!stillValid(p)||type!=ClickType.PICKUP||slot<0||slot>=COLORS.length)return;var own=StaircaseLeaks.progress(reader);if(own.getBoolean("Sock"+slot))return;
            if(!own.contains("Selected")){own.putInt("Selected",slot);cue(reader,"The cuff is folded inward.");}
            else{int first=own.getInt("Selected");own.remove("Selected");if(first!=slot&&COLORS[first]==COLORS[slot]&&COLORS[slot]<5){own.putBoolean("Sock"+first,true);own.putBoolean("Sock"+slot,true);own.putInt("Pairs",own.getInt("Pairs")+1);cue(reader,"The heels line up.");}else cue(reader,"These two do not make a pair.");}
            StaircaseLeaks.progress(reader,own);if(own.getInt("Pairs")==5){put(reader,at,SOCK_SINGLE);cue(reader,"Five pairs. One sock, and something in the trouser pocket.");}refresh();
        }
    }
}
