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
        // 0.4.202 (owner: "i really need you to flesh out the personality of each vignette you touch"): the family's own things
        // answer a hand with what they say about the four people who live here.
        if(state.is(LiteraryRegistry.BARRETT_HOME.get())){var line=family(state.getValue(BarrettHomeBlock.KIND));if(line==null)return false;p.displayClientMessage(Component.literal(line),true);return true;}
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
    /** What each of the Barretts' things tells a hand that picks it up or looks close. */
    static String family(BarrettHomeBlock.Kind kind){return switch(kind){
        case TV_STAND->"A cartoon nobody turned off. Merry knows all the words to the song.";
        case COFFEE_TABLE->"A colouring book open to a horse, half done, the crayons lined up the way Merry lines them up. John's mug, gone cold.";
        case PORTRAIT->"A studio portrait, the four of them in their good clothes. Marjorie is giving the camera the smile she gives cameras.";
        case PHOTO_BEACH->"The four of them on a beach, squinting into the sun. Somebody else took this one.";
        case PHOTO_MARJORIE->"Marjorie's school picture. Somebody drew a moustache on the glass in marker and somebody else tried to rub it off.";
        case PHOTO_MERRY->"Merry's school picture: a tooth out, the pigtails at two different heights.";
        case PHOTO_WEDDING->"John and Sarah on their wedding day, younger than people get to look for long.";
        case PHOTO_BABY->"Sarah on the old couch with a baby in a blanket, and Marjorie, small, leaning in to see her sister.";
        case PHOTO_SISTERS->"Marjorie giving Merry a piggyback across the yard. Both of them caught mid-laugh.";
        case CRUCIFIX->"A crucifix over the bed. The nail it hangs on is newer than the paint around it.";
        case MERRY_DRAWING->"Four people holding hands in front of a house, in crayon. Somebody has added a cat they do not have.";
        case PRINT_SOAP->"SOAP OPERA, over bubbles. Somebody bought it as a joke, before there were cameras in the house.";
        case PRINT_WHALE->"WHALE HELLO, and the whale saying it. Merry picked it out.";
        case TUB_SHOWER->"Shampoo, conditioner, a third bottle nobody owns up to. The rubber duck has been turned to face the wall.";
        case VANITY_FAMILY->"Four toothbrushes in one cup. The purple one has been chewed flat.";
        case VANITY_KIDS->"Two toothbrushes, the detangling spray, hair ties wound round the tap.";
        case STEP_STOOL->"Merry's stool, so she can see herself in the mirror.";
        case HAMPER->"Lights, darks, colours. All three bags are full, and most of the darks are Marjorie's.";
        case TOWEL_BAR->"Four towels for four people. One of them has a duck on it.";
        case DIRTY_TOWELS->"A wet towel on the floor with a sock rolled up in it. Nobody will say whose.";
        case BATH_MAT->"The bath mat is still damp.";
        case LINEN_SHELVES->"Towels, sheets, the board games with half their pieces. The closet smells of dryer sheets.";
        case FRUIT_BOWL->"Apples, and a banana going brown. The fruit nobody eats.";
        case CORK_BOARD->"The chore chart, more stars in Merry's column than Marjorie's. A permission slip nobody signed. The parish bulletin.";
        case BACKPACKS->"Two school bags on their hooks. Marjorie's is covered in pen. Merry's has a keyring of a cat.";
        case COVERED->"The family's sofa and their old set, under dust sheets since the crew moved in. A corner held with gaffer tape.";
        case SCHEDULE_BOARD->"The shooting schedule in marker, the days crossed off as they go. Somebody has written a priest's name with a question mark.";
        case STUFFED->"A bear, a lamb and a frog, set in a row facing the door.";
        case CEILING_STARS->"Stars that glow in the dark. Somebody stood on the bed to stick them up.";
        case DRAWINGS->"Three drawings taped in a row: the cardboard house, her sister, and a monster under a bed with enormous feet.";
        case TOYS->"A stuffed rabbit, letter blocks, a plastic pony with its mane brushed flat.";
        case FAIRY_LIGHTS->"Fairy lights pinned along the wall. Half the bulbs still work.";
        case POSTER_C->"A band poster with half the faces scratched out in pen.";
        case PINBOARD->"Photo-booth strips with friends from school, two ticket stubs, a pressed flower going brown.";
        case GUITAR->"A guitar with a string missing. A chord chart is still taped to the neck.";
        case BOOMBOX->"A boombox, and its CDs out of their cases in a stack.";
        case NIGHTSTAND_JOHN->"His side of the bed. A Bible with a rosary laid on it, and the alarm set early.";
        case NIGHTSTAND_SARAH->"Her side. A paperback face down where she stopped, her reading glasses on top of it.";
        case DESK->"The family computer, the job listings in a stack beside it. Two are circled. One is crossed out hard.";
        case FRIDGE,FRIDGE_TOP->"Magnets, a takeout menu, one of Merry's drawings, the school lunch calendar.";
        case CONSOLE->"The mail on the hall table. The bills are on top, still in their envelopes.";
        case CALENDAR->"Appointments in two hands. One day is written in red, all capitals: FILMING STARTS.";
        case WINDOW,WINDOW_HEAD->"The blinds are drawn. The crew keeps the daylight out of the house.";
        case DOUBLE_BED->"Their bed, made in a hurry. One pillow has been slept on and one has not.";
        case DESK_KID->"Merry's homework, a spelling list with a gold star, and a pencil chewed at the end.";
        case POSTER_A->"A band poster, the corners curling where the tape is giving up.";
        case POSTER_B->"The planets, in order. Pluto has been put back in in pen.";
        case CLOTHES->"Clothes where they were dropped.";
        case SOFTBOX,DIRECTORS_CHAIR,VILLAGE_CART,APPLE_BOXES,SANDBAGS,CRAFT_TABLE,FOLDING_TABLE->"The crew's. Nobody in the family has touched it.";
        default->null;
    };}
    private static String meal(int day,boolean plain){return switch(day){case 1->"Stew waits in the bowls while the crew finishes the interviews.";case 3->"Sandwiches, cut before anyone asked whether the girls were hungry.";case 4->"Soup has gone cool during the private conversation.";case 5->"Roast and vegetables, served around another upstairs evaluation.";case 6->"Potatoes and peas. The equipment is already being carried upstairs.";case 7->plain?"Merry's spaghetti has no sauce. Her plate looks ordinary.":"Spaghetti again. The family is eating after the programme has ended.";default->plain?"Plain spaghetti on Merry's plate.":"Spaghetti, eaten under the crew's lights.";};}
}
