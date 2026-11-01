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
        if(rel.equals(GhostsSetRooms.MONITOR_AT.above())&&(BarrettHomeBlock.is(state,BarrettHomeBlock.Kind.FIELD_MONITOR)||state.is(LiteraryRegistry.PROP.get())&&state.getValue(LiteraryPropBlock.KIND)==LiteraryPropBlock.Kind.TELEVISION)){
            if(own.getBoolean("DayDone")){int day=Math.max(1,Math.min(7,own.getInt("FilmDay")));boolean raw=p.isShiftKeyDown();LiteraryVignettes.open(p,GhostsSet.PLACE,own,(raw?"Rushes":"Episode")+day,GhostsSet.edit(p,own,day,raw),b.offset(rel));}
            else p.displayClientMessage(Component.literal("The feed from the crew's camera. The day's cut is reviewed here after the take."),true);
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
        case TV_STAND->"A cartoon, the sound turned low. Merry knows all the words to the song.";
        case COFFEE_TABLE->"A colouring book open to a half-done horse, the crayons lined up in a row. John's mug, gone cold.";
        case PORTRAIT->"A studio portrait of the four of them in their good clothes.";
        case PHOTO_BEACH->"The four of them on a beach, squinting into the sun.";
        case PHOTO_MARJORIE->"Marjorie's school picture. A marker moustache on the glass, half rubbed off.";
        case PHOTO_MERRY->"Merry's school picture: a tooth out, the pigtails at two heights.";
        case PHOTO_WEDDING->"John and Sarah on their wedding day.";
        case PHOTO_BABY->"Sarah with a new baby on the old couch, and a small Marjorie leaning in to see.";
        case PHOTO_SISTERS->"Marjorie giving Merry a piggyback across the yard, both of them laughing.";
        case CRUCIFIX->"A crucifix over the bed, on a new nail.";
        case MERRY_DRAWING->"Four people holding hands in front of a house, in crayon, and a cat they do not have.";
        case PRINT_SOAP->"SOAP OPERA, over bubbles. A joke print.";
        case PRINT_WHALE->"WHALE HELLO, and the whale saying it.";
        case TUB_SHOWER->"Shampoo and conditioner. The rubber duck has been turned to face the wall.";
        case VANITY_FAMILY->"Four toothbrushes in one cup. The purple one is chewed flat.";
        case VANITY_KIDS->"Two toothbrushes, detangling spray, hair ties round the tap.";
        case STEP_STOOL->"Merry's step stool, for the mirror.";
        case HAMPER->"Lights, darks, colours. All three bags are full.";
        case TOWEL_BAR->"Four towels. One of them has a duck on it.";
        case DIRTY_TOWELS->"A wet towel on the floor with a sock rolled up in it.";
        case BATH_MAT->"The bath mat is still damp.";
        case LINEN_SHELVES->"Towels, sheets and board games. It smells of dryer sheets.";
        case FRUIT_BOWL->"Apples, and a banana going brown.";
        case CORK_BOARD->"The chore chart, more stars in Merry's column. The parish bulletin.";
        case BACKPACKS->"Two school bags. Marjorie's is covered in pen; Merry's has a cat keyring.";
        case COVERED->"The family's sofa and old set, under dust sheets taped down by the crew.";
        case SCHEDULE_BOARD->"The shooting schedule, the days crossed off. A priest's name with a question mark.";
        case STUFFED->"A bear, a lamb and a frog in a row, facing the door.";
        case CEILING_STARS->"Glow-in-the-dark stars on the ceiling.";
        case DRAWINGS->"Three drawings: the cardboard house, her sister, and a monster with enormous feet.";
        case TOYS->"A stuffed rabbit, letter blocks, a plastic pony.";
        case FAIRY_LIGHTS->"Fairy lights along the wall. Half the bulbs work.";
        case POSTER_C->"A band poster with half the faces scratched out in pen.";
        case PINBOARD->"Photo-booth strips, two ticket stubs, a pressed flower.";
        case GUITAR->"A guitar with a string missing, a chord chart taped to the neck.";
        case BOOMBOX->"A boombox and a stack of loose CDs.";
        case NIGHTSTAND_JOHN->"His side: a Bible with a rosary on it, the alarm set early.";
        case NIGHTSTAND_SARAH->"Her side: a paperback face down, her reading glasses on top.";
        case DESK->"The family computer and a stack of job listings, two of them circled.";
        case FRIDGE,FRIDGE_TOP->"Magnets, a takeout menu, one of Merry's drawings.";
        case CONSOLE->"The mail. The bills are on top, unopened.";
        case CALENDAR->"Appointments in two hands. One day in red capitals: FILMING STARTS.";
        case WINDOW,WINDOW_HEAD->"The blinds are drawn for the crew's lights.";
        case DOUBLE_BED->"Their bed, made in a hurry.";
        case DESK_KID->"Merry's homework, a spelling list with a gold star.";
        case POSTER_A->"A band poster, its corners curling.";
        case POSTER_B->"The planets, in order. Pluto added back in pen.";
        case CLOTHES->"Clothes where they were dropped.";
        case COAT_HOOKS->"John's navy coat, Sarah's red raincoat, Merry's school bag.";
        case COAT_HOOKS_B->"A trench coat, Merry's yellow raincoat, a long striped scarf.";
        case SIDEBOARD_L,SIDEBOARD_R->"The good placemats and the napkins nobody uses, in the sideboard.";
        case RANGE->"The stove is still warm. A pan soaks in the sink.";
        case RANGE_HOOD->"The hood light is left on at night.";
        case WALL_CABINET->"Plates, mismatched glasses, the cereal on the top shelf.";
        case MICROWAVE->"The clock on the microwave is four minutes fast.";
        case TOASTER->"Crumbs in the tray. The jar beside it is for coins.";
        case COFFEE_MAKER->"Half a pot of coffee from this morning.";
        case DISHWASHER->"Clean dishes nobody has put away.";
        case RUG_RUNNER,RUG_RUNNER_NARROW->"The runner, worn thin down the middle.";
        case RUG_LIVING->"The rug is worn paler where the sofa's feet go.";
        case MERRY_RUG->"Merry's rug: a moon, Saturn with its rings, and stars.";
        case RUG_SHAG->"Marjorie's rug. It needs vacuuming.";
        case SOFTBOX->"A softbox on its stand, aimed at where the family sits.";
        case DIRECTORS_CHAIR->"A canvas director's chair.";
        case VILLAGE_CART->"The crew's cart of cables and spare batteries.";
        case APPLE_BOXES->"Apple boxes, for standing a camera or a person taller.";
        case SANDBAGS->"Sandbags for the light stands.";
        case CRAFT_TABLE->"Coffee urn and a box of doughnuts.";
        case FOLDING_TABLE->"Tape, markers and spare call sheets.";
        default->null;
    };}
    private static String meal(int day,boolean plain){return switch(day){case 1->"Stew waits in the bowls while the crew finishes the interviews.";case 3->"Sandwiches, cut before anyone asked whether the girls were hungry.";case 4->"Soup has gone cool during the private conversation.";case 5->"Roast and vegetables, served around another upstairs evaluation.";case 6->"Potatoes and peas. The equipment is already being carried upstairs.";case 7->plain?"Merry's spaghetti has no sauce. Her plate looks ordinary.":"Spaghetti again. The family is eating after the programme has ended.";default->plain?"Plain spaghetti on Merry's plate.":"Spaghetti, eaten under the crew's lights.";};}
}
