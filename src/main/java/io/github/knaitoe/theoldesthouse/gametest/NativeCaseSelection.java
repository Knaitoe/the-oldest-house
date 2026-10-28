package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.lang.reflect.Method;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;

/** Optional local selection of unchanged native cases. Default/CI registration is untouched. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_selected") @PrefixGameTestTemplate(false)
public final class NativeCaseSelection {
    @GameTestGenerator public static Collection<TestFunction> selected() throws ReflectiveOperationException,java.io.IOException {
        String wanted=System.getProperty("the_oldest_house.selectedGameTests","");if(wanted.isBlank())return List.of();
        var factory=GameTestRegistry.class.getDeclaredMethod("turnMethodIntoTestFunction",Method.class);factory.setAccessible(true);var result=new ArrayList<TestFunction>();var unique=new HashSet<String>();
        for(String name:wanted.split(",")){
            if(!name.matches("[A-Za-z]+Tests#[A-Za-z][A-Za-z0-9]*")||!unique.add(name))throw new IllegalArgumentException("Invalid or duplicate native case: "+name);
            var parts=name.split("#");var type=Class.forName(NativeCaseSelection.class.getPackageName()+"."+parts[0]);var method=type.getDeclaredMethod(parts[1],GameTestHelper.class);if(method.getAnnotation(GameTest.class)==null)throw new IllegalArgumentException("Not a native case: "+name);
            var original=(TestFunction)factory.invoke(null,method);var source=net.minecraft.resources.ResourceLocation.parse(original.structureName());String alias=source.getNamespace().replace(TheOldestHouse.MOD_ID+"_","");String selected=TheOldestHouse.MOD_ID+"_selected:"+alias;
            var file=net.neoforged.fml.ModList.get().getModFileById(TheOldestHouse.MOD_ID).getFile();
            var old=file.findResource("data",source.getNamespace(),"structure",source.getPath()+".nbt");var copy=file.findResource("data",TheOldestHouse.MOD_ID+"_selected","structure",alias+".nbt");
            if(!Arrays.equals(java.nio.file.Files.readAllBytes(old),java.nio.file.Files.readAllBytes(copy)))throw new IllegalStateException("Selection must use the byte-identical original native template: "+name);
            result.add(new TestFunction(original.batchName(),original.testName(),selected,original.rotation(),original.maxTicks(),original.setupTicks(),original.required(),original.manualOnly(),original.maxAttempts(),original.requiredSuccesses(),original.skyAccess(),original.function()));
        }
        return result;
    }
}
