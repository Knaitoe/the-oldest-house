package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;

/** Fittings can be handled, lamps switched and the girls' books read; looking alone never grants filmed evidence. */
public final class GhostsSetInteractions {
    private GhostsSetInteractions(){}
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var state=p.serverLevel().getBlockState(b.offset(rel));
        if(rel.equals(GhostsSetRooms.MONITOR_AT.above())&&state.is(LiteraryRegistry.PROP.get())&&state.getValue(LiteraryPropBlock.KIND)==LiteraryPropBlock.Kind.TELEVISION){
            if(own.getBoolean("DayDone")){int day=Math.max(1,Math.min(7,own.getInt("FilmDay")));boolean raw=p.isShiftKeyDown();LiteraryVignettes.open(p,GhostsSet.PLACE,own,(raw?"Rushes":"Episode")+day,GhostsSet.edit(p,own,day,raw),b.offset(rel));}
            else p.displayClientMessage(Component.literal("The monitor holds your recorded frames. The day's cut is reviewed after the take."),true);
            return true;
        }
        if(!state.is(LiteraryRegistry.GHOST_SET.get()))return false;
        var kind=state.getValue(GhostsSetBlock.KIND);
        if(kind==GhostsSetBlock.Kind.LAMP||kind==GhostsSetBlock.Kind.LAMP_OFF){
            p.serverLevel().setBlock(b.offset(rel),GhostsSetBlock.of(kind==GhostsSetBlock.Kind.LAMP?GhostsSetBlock.Kind.LAMP_OFF:GhostsSetBlock.Kind.LAMP,state.getValue(GhostsSetBlock.FACING)),3);
            p.serverLevel().playSound(null,b.offset(rel),SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.3F,kind==GhostsSetBlock.Kind.LAMP?.7F:.95F);
            own.putInt("LampSwitches0488",Math.min(999,own.getInt("LampSwitches0488")+1));return true;
        }
        if(kind==GhostsSetBlock.Kind.BOOKS||kind==GhostsSetBlock.Kind.DRAWING){
            String key="GirlsPaper0488_"+rel.asLong();String title=kind==GhostsSetBlock.Kind.DRAWING?"The sisters' drawing":"The sisters' storybook";
            var pages=kind==GhostsSetBlock.Kind.DRAWING?List.of("Two girls in a house. Green lines crowd the borders. One little figure is drawn over an older mark.","On the back: Merry started it. Marjorie filled in the parts Merry left white."):List.of("Merry wrote the beginning in a small, careful hand. Marjorie's larger words gather in the margins. Neither name is crossed out.","Two sisters went into a house they had drawn. They could leave by rubbing out the door. Each time they tried, the other drew it back.","The final page has no ending. There is room under the picture for another line.");
            LiteraryVignettes.open(p,GhostsSet.PLACE,own,key,HouseWriting.book(title,"Merry and Marjorie",HouseWriting.WritingStyle.WILL,pages),b.offset(rel));return true;
        }
        String text=switch(kind){
            case PLATE,PLAIN_PLATE,MEAL_STEW,MEAL_SANDWICH,MEAL_SOUP,MEAL_ROAST,MEAL_POTATOES->meal(own.getInt("FilmDay"),kind==GhostsSetBlock.Kind.PLAIN_PLATE);
            case HAT->"The sequins catch the light. Nothing about the hat is different when you lift its brim.";
            case BOLTS->"You feel the rough metal beneath the table edge. The rusty fitting is ordinary, and sharp.";
            case JUG->"The jug is heavy enough to hurt someone if it falls. You set it back where it was.";
            case CAMERA->"The crew's tripod is locked. Your own handheld lens can keep what their angle leaves out.";
            case CASE->"An old camera case. The latch is stiff.";
            case SINK->"The tap runs briefly into the stained basin.";
            case COOKER->"The cooker is warm. A family meal waits for the cameras to be ready.";
            case DRAWER,DRAWER_RIG->"The drawer's metalwork is visible. Touching it cannot tell you who put it there.";
            case PLAYHOUSE,TORN_CARD->"The cardboard still carries the sisters' pictures. Its creases are softened by handling.";
            case BED_HEAD,BED_FOOT->"The bedding smells of a room that has been kept shut.";
            case TABLE,CHAIR,COUNTER->"The furniture has ordinary scratches, worked around by the crew's careful framing.";
            case RUG->"The worn rug muffles your footsteps between the armchairs.";
            case PLASTER->"The chipped plaster gives your fingers a shallow hold.";
            default->null;
        };
        if(text==null)return false;
        if(kind==GhostsSetBlock.Kind.SINK&&p.serverLevel().getGameTime()>=own.getLong("SinkNext0488")){own.putLong("SinkNext0488",p.serverLevel().getGameTime()+40);p.serverLevel().sendParticles(p,net.minecraft.core.particles.ParticleTypes.SPLASH,false,b.getX()+rel.getX()+.5,b.getY()+rel.getY()+1.02,b.getZ()+rel.getZ()+.5,8,.1,.02,.1,.02);p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.WATER_AMBIENT),SoundSource.BLOCKS,b.getX()+rel.getX()+.5,b.getY()+rel.getY()+1,b.getZ()+rel.getZ()+.5,.25F,1,p.getRandom().nextLong()));}
        own.putBoolean("Handled0488_"+kind.getSerializedName(),true);p.displayClientMessage(Component.literal(text),true);return true;
    }
    private static String meal(int day,boolean plain){return switch(day){case 1->"Stew waits in the bowls while the crew finishes the interviews.";case 3->"Sandwiches, cut before anyone asked whether the girls were hungry.";case 4->"Soup has gone cool during the private conversation.";case 5->"Roast and vegetables, served around another upstairs evaluation.";case 6->"Potatoes and peas. The equipment is already being carried upstairs.";case 7->plain?"Merry's spaghetti has no sauce. Her plate looks ordinary.":"Spaghetti again. The family is eating after the programme has ended.";default->plain?"Plain spaghetti on Merry's plate.":"Spaghetti, eaten under the crew's lights.";};}
}
