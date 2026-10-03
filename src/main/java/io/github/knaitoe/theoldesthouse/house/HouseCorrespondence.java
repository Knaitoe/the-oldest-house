package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Finite personal originals on existing paper surfaces; no scenery migration or story authority. */
public final class HouseCorrespondence {
    public static final String ID="house_correspondence_0431";
    public record Encounter(String binding,String note,String legacy,int chapter,ItemStack book,boolean taken) {}
    private static final List<CorrespondenceTexts.Note> CATALOGUE=catalogue();
    private HouseCorrespondence() {}
    private static List<CorrespondenceTexts.Note> catalogue() {
        var all=new ArrayList<>(CorrespondenceTexts.all());
        int[] gates={0,6,8,12,16};
        for(var thread:HouseMarginalia.Thread.values())for(int n=0;n<thread.chapters;n++)
            all.add(new CorrespondenceTexts.Note("R_"+thread.getSerializedName()+"_"+n,"","",
                    HouseWriting.WritingStyle.PLAIN,"legacy_"+thread.getSerializedName(),n,gates[n],thread,""));
        return List.copyOf(all);
    }
    public static CompoundTag record(LabyrinthData data,UUID reader){return data.state(ID).getCompound(reader.toString()).copy();}
    private static void save(LabyrinthData data,UUID reader,CompoundTag own){var all=data.state(ID);all.put(reader.toString(),own);data.setState(ID,all);}
    /** Called only by actual arrival/return callbacks, never by a tick or page click. */
    public static void crossed(ServerPlayer reader){
        if(!reader.isAlive()||reader.isSpectator())return;
        var data=LabyrinthData.get(reader.server);var own=record(data,reader.getUUID());
        own.putLong("Step",own.getLong("Step")+1);save(data,reader.getUUID(),own);
    }
    public static Encounter bind(ServerPlayer reader,BlockPos surface,HouseMarginalia.Thread thread){
        var data=LabyrinthData.get(reader.server);var own=record(data,reader.getUUID());
        String key=surface.asLong()+":"+thread.getSerializedName()+":"+HouseMarginalia.band(data.returnDepth(reader.getUUID()));
        var bindings=own.getCompound("Bindings");var entry=bindings.getCompound(key);long step=own.getLong("Step");
        if(!entry.contains("Step")||entry.getLong("Step")!=step){
            entry=new CompoundTag();entry.putLong("Step",step);
            var old=HouseMarginalia.record(data,reader.getUUID()).getCompound("Bindings").getCompound(key);
            if(old.contains("Book")&&!own.getCompound("LegacyUsed").getBoolean(key)){
                entry.putString("Legacy",key);entry.putInt("Chapter",old.getInt("Chapter"));
                if(old.getBoolean("Finished")||old.getInt("Chapter")<0){var used=own.getCompound("LegacyUsed");used.putBoolean(key,true);own.put("LegacyUsed",used);}
            }else{
                var note=choose(reader,own,thread);entry.putString("Note",note.id());
                var books=own.getCompound("Books");
                if(!books.contains(note.id())){
                    Map<String,String> facts=facts(reader,own,note,true);
                    ItemStack book=note.chain().startsWith("legacy_")?MarginaliaTexts.book(reader,note.surface(),note.installment()):CorrespondenceTexts.book(note,facts);
                    var custom=new CompoundTag();custom.putUUID("CorrespondenceReader",reader.getUUID());custom.putString("CorrespondenceId",note.id());
                    book.set(DataComponents.CUSTOM_DATA,CustomData.of(custom));books.put(note.id(),book.save(reader.registryAccess()));own.put("Books",books);
                }
            }
            bindings.put(key,entry);own.put("Bindings",bindings);save(data,reader.getUUID(),own);
        }
        String legacy=entry.getString("Legacy"),id=entry.getString("Note");
        var stored=legacy.isEmpty()?own.getCompound("Books").getCompound(id)
                :HouseMarginalia.record(data,reader.getUUID()).getCompound("Bindings").getCompound(legacy).getCompound("Book");
        boolean taken=legacy.isEmpty()?own.getCompound("Taken").getBoolean(id)
                :HouseMarginalia.record(data,reader.getUUID()).getCompound("Bindings").getCompound(legacy).getBoolean("Taken");
        return new Encounter(key,id,legacy,entry.getInt("Chapter"),ItemStack.parseOptional(reader.registryAccess(),stored),taken);
    }
    private static CorrespondenceTexts.Note choose(ServerPlayer reader,CompoundTag own,HouseMarginalia.Thread surface){
        int cursor=Math.floorMod(own.getInt("Cursor"),CATALOGUE.size());
        CorrespondenceTexts.Note chosen=null;int best=Integer.MAX_VALUE;
        for(int i=0;i<CATALOGUE.size();i++){
            var n=CATALOGUE.get(i);if(!eligible(reader,own,n))continue;
            int score=Math.floorMod(i-cursor,CATALOGUE.size())+(n.surface()==surface?0:14);
            if(score<best){best=score;chosen=n;}
        }
        if(chosen!=null)return chosen;
        // Revisit an existing original when the next installments are not yet due. Never mint filler.
        var books=own.getCompound("Books");
        for(int i=0;i<CATALOGUE.size();i++){var n=CATALOGUE.get((cursor+i)%CATALOGUE.size());if(books.contains(n.id()))return n;}
        throw new IllegalStateException("No mundane first letter available");
    }
    private static boolean eligible(ServerPlayer p,CompoundTag own,CorrespondenceTexts.Note n){
        var data=LabyrinthData.get(p.server);
        if(own.getCompound("Read").getBoolean(n.id())||data.returnDepth(p.getUUID())<n.depth())return false;
        if(!n.chain().isEmpty()){
            int next=own.getCompound("Next").getInt(n.chain());
            if(n.chain().startsWith("legacy_"))next=Math.max(next,HouseMarginalia.next(data,p.getUUID(),n.surface()));
            if(n.installment()!=next)return false;
            var steps=own.getCompound("ChainStep");
            if(steps.contains(n.chain())&&own.getLong("Step")-steps.getLong(n.chain())<2)return false;
        }
        if(n.chain().equals("L")&&!own.getCompound("Facts").getCompound("L").contains("possession")&&lost(p)==null)return false;
        var experience=HouseExperience.record(data,p.getUUID());
        return switch(n.id()){
            case "P01"->experience.getInt("Care")>0&&experience.hasUUID("CaredPet")&&!experience.getString("CaredName").isBlank();
            case "P02"->experience.getInt("Returns")>=2;
            case "P03"->HollowayVignette.personal(data,p.getUUID()).getBoolean("Looted");
            case "P04"->actualMarks(p,data)>=2;
            case "P05"->!experience.getString("LastRetreat").isBlank();
            case "P06"->retainedPhoto(p,data);
            case "P07"->!experience.getCompound("ReplySent").isEmpty();
            case "P08"->lost(p)!=null;
            default->true;
        };
    }
    public static boolean available(ServerPlayer reader,String id){
        var note=CATALOGUE.stream().filter(n->n.id().equals(id)).findFirst().orElse(null);
        return note!=null&&eligible(reader,record(LabyrinthData.get(reader.server),reader.getUUID()),note);
    }
    private static int actualMarks(ServerPlayer reader,LabyrinthData data){
        int count=0;var marks=data.state("navigation_marks").getList("Marks",Tag.TAG_COMPOUND);
        for(int i=0;i<marks.size();i++){
            var mark=marks.getCompound(i);if(!mark.hasUUID("Owner")||!reader.getUUID().equals(mark.getUUID("Owner")))continue;
            for(var level:reader.server.getAllLevels())if(level.dimension().location().toString().equals(mark.getString("Dimension"))){
                BlockPos at=BlockPos.of(mark.getLong("Pos"));
                if(level.hasChunkAt(at)&&level.getBlockState(at).is(mark.getBoolean("Chalk")?HouseBlocks.CHALK_MARK.get():HouseBlocks.TRAIL_LINE.get()))count++;
                break;
            }
            if(count>=2)return count;
        }return count;
    }
    private static boolean retainedPhoto(ServerPlayer reader,LabyrinthData data){
        var own=NovelVignettes.personal(data,reader.getUUID());if(!own.contains("Photo")||own.getBoolean("PhotoGivenAway"))return false;
        var original=ItemStack.parseOptional(reader.registryAccess(),own.getCompound("Photo"));
        var expected=original.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        if(!expected.hasUUID(NovelVignettes.PHOTO_ID))return false;
        var items=new ArrayList<>(reader.getInventory().items);items.add(reader.containerMenu.getCarried());items.addAll(reader.getInventory().offhand);
        for(var item:items){var tag=item.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
            if(tag.hasUUID(NovelVignettes.PHOTO_OWNER)&&tag.hasUUID(NovelVignettes.PHOTO_ID)
                    &&reader.getUUID().equals(tag.getUUID(NovelVignettes.PHOTO_OWNER))
                    &&expected.getUUID(NovelVignettes.PHOTO_ID).equals(tag.getUUID(NovelVignettes.PHOTO_ID)))return true;
        }return false;
    }
    private static MotherCollection.Entry lost(ServerPlayer reader){
        return MotherCollection.get(reader.server).all().stream()
                .filter(e->!e.pet&&reader.getUUID().equals(e.owner)&&e.claimant==null&&!e.item(reader.registryAccess()).isEmpty())
                .min(Comparator.comparing(e->e.id)).orElse(null);
    }
    private static Map<String,String> facts(ServerPlayer reader,CompoundTag own,CorrespondenceTexts.Note note,boolean snapshot){
        var values=new HashMap<String,String>();values.put("reader",CorrespondenceTexts.literal(reader.getGameProfile().getName()));
        var experience=HouseExperience.record(LabyrinthData.get(reader.server),reader.getUUID());
        if(!experience.getString("CaredName").isBlank())values.put("companion",CorrespondenceTexts.literal(experience.getString("CaredName")));
        var held=lost(reader);if(held!=null)values.put("possession",CorrespondenceTexts.literal(held.name));
        if(note.chain().equals("L")||note.chain().equals("N")){
            var all=own.getCompound("Facts");var stored=all.getCompound(note.chain());
            if(stored.isEmpty()&&snapshot){for(var value:values.entrySet())stored.putString(value.getKey(),value.getValue());all.put(note.chain(),stored);own.put("Facts",all);}
            for(String key:stored.getAllKeys())values.put(key,stored.getString(key));
        }
        return values;
    }
    public static void finish(ServerPlayer reader,Encounter encountered,HouseMarginalia.Thread thread){
        var data=LabyrinthData.get(reader.server);var own=record(data,reader.getUUID());
        if(!encountered.legacy().isEmpty()){
            HouseMarginalia.finishLegacy(reader,encountered.legacy(),thread,encountered.chapter());
            var used=own.getCompound("LegacyUsed");used.putBoolean(encountered.legacy(),true);own.put("LegacyUsed",used);
        }else{
            var read=own.getCompound("Read");if(read.getBoolean(encountered.note()))return;
            read.putBoolean(encountered.note(),true);own.put("Read",read);own.putInt("ReadCount",own.getInt("ReadCount")+1);
            for(int i=0;i<CATALOGUE.size();i++)if(CATALOGUE.get(i).id().equals(encountered.note())){
                var n=CATALOGUE.get(i);own.putInt("Cursor",(i+1)%CATALOGUE.size());
                if(!n.chain().isEmpty()){
                    var next=own.getCompound("Next");next.putInt(n.chain(),Math.max(next.getInt(n.chain()),n.installment()+1));own.put("Next",next);
                    var steps=own.getCompound("ChainStep");steps.putLong(n.chain(),own.getLong("Step"));own.put("ChainStep",steps);
                }break;
            }
        }save(data,reader.getUUID(),own);
    }
    public static boolean taken(ServerPlayer reader,Encounter encountered){
        var data=LabyrinthData.get(reader.server);
        return encountered.legacy().isEmpty()?record(data,reader.getUUID()).getCompound("Taken").getBoolean(encountered.note())
                :HouseMarginalia.record(data,reader.getUUID()).getCompound("Bindings").getCompound(encountered.legacy()).getBoolean("Taken");
    }
    public static void take(ServerPlayer reader,Encounter encountered,HouseMarginalia.Thread thread){
        var data=LabyrinthData.get(reader.server);
        if(!encountered.legacy().isEmpty()){
            HouseMarginalia.takeLegacy(reader,encountered.legacy());finish(reader,encountered,thread);
        }else{
            var own=record(data,reader.getUUID());var taken=own.getCompound("Taken");taken.putBoolean(encountered.note(),true);own.put("Taken",taken);save(data,reader.getUUID(),own);
        }
    }
    /** Clearly labelled missing facts are confined to operator specimens; previews never save progress. */
    public static ItemStack preview(ServerPlayer reader,String id){
        var note=CorrespondenceTexts.find(id);if(note==null)return ItemStack.EMPTY;
        var values=facts(reader,record(LabyrinthData.get(reader.server),reader.getUUID()),note,false);
        values.putIfAbsent("companion","a companion (preview)");values.putIfAbsent("possession","a lost belonging (preview)");
        return CorrespondenceTexts.book(note,values);
    }
    public static List<ItemStack> samples(ServerPlayer reader){return CorrespondenceTexts.all().stream().map(n->preview(reader,n.id())).toList();}
}
