package forgprod.abilities.tooltip.configuration;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.campaign.ui.UITable;

/**
 * Utilizes indirect reflection technique by Lyravega to access and modify nested private elements of UI in obfuscated code.
 * Update-resistant, does not explicitly use obfuscated names. Singleton-pattern class.
 * @author Ontheheavens
 * Written with help of Viravain and Lyravega.
 * Unused since 12.12.2022 because tables were swapped for custom panels, left for later use.
 * @since 27.11.2022
 */

@SuppressWarnings("unused")
public class TableHandler {

    private static TableHandler handlerInstance;
    private final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private MethodHandle getMethodName;
    private MethodHandle getFieldType;
    private MethodHandle setAccessible;
    private MethodHandle unreflect;
    private  MethodHandle unreflectGetter;

    private TableHandler() {
        try {
            Class<?> fieldClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());
            Class<?> methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());
            Class<?> accessibleObjectClass = Class.forName("java.lang.reflect.AccessibleObject", false, Class.class.getClassLoader());

            this.getFieldType = lookup.findVirtual(fieldClass, "getType", MethodType.methodType(Class.class));
            this.getMethodName = lookup.findVirtual(methodClass, "getName", MethodType.methodType(String.class));
            this.setAccessible = lookup.findVirtual(accessibleObjectClass, "setAccessible", MethodType.methodType(void.class, boolean.class));
            this.unreflect = lookup.findVirtual(lookup.getClass(), "unreflect", MethodType.methodType(MethodHandle.class, methodClass));
            this.unreflectGetter = lookup.findVirtual(lookup.getClass(), "unreflectGetter", MethodType.methodType(MethodHandle.class, fieldClass));
            // Since the tooltip re-renders every frame, we don't want our error messages to utterly flood the log file.
            // Any issue with this class will result only in tables left unattended.
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException ignored) {}
    }

    public static TableHandler getHandlerInstance() {
        if (handlerInstance == null) {
            handlerInstance = new TableHandler();
        }
        return handlerInstance;
    }

    public void configureTable(TooltipMakerAPI tooltip) throws Throwable {
        UITable table = retrieveTable(tooltip);
        if (table == null) { return; }
        Object scrollingList = retrieveScrollingList(table);
        if (scrollingList == null) { return; }
        HashMap<Object, Object> rowMap = retrieveRowMap(scrollingList);
        if (rowMap == null) { return; }
        for (Map.Entry<Object, Object> rowEntry : rowMap.entrySet()) {
            Object setSoundMethod = null;
            Class<?> rowClass = rowEntry.getKey().getClass();
            for (Object method : rowClass.getDeclaredMethods()) {
                String checkedMethodName = (String) getMethodName.invoke(method);
                if (checkedMethodName.equals("setMouseOverSound")) {
                    setSoundMethod = method;
                    break;
                }
            }
            MethodHandle soundSetterMH;
            if (setSoundMethod != null) {
                soundSetterMH = (MethodHandle) unreflect.invoke(lookup, setSoundMethod);
                soundSetterMH.invoke(rowEntry.getKey(), null);
            } else { return; }
        }
        table.setItemsSelectable(false);
        table.setWithBorder(false);
        table.setShowHeader(false);
    }

    private UITable retrieveTable(TooltipMakerAPI tooltip) throws Throwable {
        Class<?> tooltipSuperclass = tooltip.getClass().getSuperclass();
        Object tableField = null;
        // Finding UITable field among tooltip fields.
        for (Object field : tooltipSuperclass.getDeclaredFields()) {
            Class<?> fieldClass = (Class<?>) getFieldType.invoke(field);
            // Looking specifically for UITable type here, unlikely to change on updates.
            if (fieldClass.getName().equals(UITable.class.getName())) {
                tableField = field;
                break;
            }
        }
        //Setting access to UITable field.
        setAccessible.invoke(tableField, true);
        MethodHandle tableFieldGetter = (MethodHandle) unreflectGetter.invoke(lookup, tableField);
        Object reflectedTable = tableFieldGetter.invoke(tooltip);
        UITable readyTable;
        if (reflectedTable.getClass().getName().equals(UITable.class.getName())) {
            readyTable = (UITable) reflectedTable;
        } else { return null; }
        return readyTable;
    }

    private Object retrieveScrollingList(UITable table) throws Throwable {
        Class<?> tableClass = table.getClass();
        Object scrollingList = null;
        for (Object field : tableClass.getDeclaredFields()) {
                // Checking method names of every field's type class to find the right field.
                Class<?> fieldClass = (Class<?>) getFieldType.invoke(field);
                for (Object method : fieldClass.getDeclaredMethods()) {
                    String checkedMethodName = (String) getMethodName.invoke(method);
                    String neededMethodName = "swapItems"; // We should check for the rarest one just to be safe.
                    boolean hasRightMethod = checkedMethodName.equals(neededMethodName);
                    if (hasRightMethod) {
                        scrollingList = field;
                        break;
                    }
                }
                if (scrollingList != null) { break; }
        }
        setAccessible.invoke(scrollingList, true);
        MethodHandle scrollingListFieldGetter;
        if (scrollingList != null) {
            scrollingListFieldGetter = (MethodHandle) unreflectGetter.invoke(lookup, scrollingList);
        } else { return null; }
        Object reflectedScrollingList;
        reflectedScrollingList = scrollingListFieldGetter.invoke(table);
        return reflectedScrollingList;
    }

    @SuppressWarnings("unchecked")
    private HashMap<Object, Object> retrieveRowMap(Object scrollingList) throws Throwable {
        Class<?> scrollingListClass = scrollingList.getClass();
        Object rowMapField = null;
        for (Object field : scrollingListClass.getDeclaredFields()) {
            // Checking for type here, we have a Map (NOT HashMap) to look for.
            Class<?> fieldClass = (Class<?>) getFieldType.invoke(field);
            if (fieldClass.getName().equals(Map.class.getName())) {
                rowMapField = field;
                break;
            }
        }
        setAccessible.invoke(rowMapField, true);
        MethodHandle rowMapFieldGetter;
        if (rowMapField != null) {
            rowMapFieldGetter = (MethodHandle) unreflectGetter.invoke(lookup, rowMapField);
        } else { return null; }
        Object reflectedRowMap;
        reflectedRowMap = rowMapFieldGetter.invoke(scrollingList);
        HashMap<Object, Object> readyRowMap;
        Class<?> rowMapClass = reflectedRowMap.getClass();
        Class<?> resultClass = HashMap.class;
        if (rowMapClass.getName().equals(resultClass.getName())) {
            readyRowMap = (HashMap<Object, Object>) resultClass.cast(reflectedRowMap);
        } else { return null; }
        return readyRowMap;
    }

}
