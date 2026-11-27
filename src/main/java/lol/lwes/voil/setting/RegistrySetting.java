package lol.lwes.voil.setting;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class RegistrySetting extends Setting {
    private final Class registryType;
    private final RegistrySelectionMode selectionMode;
    private List available;
    private List selected;
    private static final String DOT_REGEX = "\\.";

    public RegistrySetting(String s, Class oclass, RegistrySelectionMode registryselectionmode, String s1) {
        super(s, s1);
        this.registryType = oclass;
        this.selectionMode = registryselectionmode;
        this.selected = new ArrayList();
        this.refresh();
    }

    public static String getBlockName(Block block) {
        String s = block.getTranslationKey();
        return normalizeId(s);
    }

    public static String getItemName(Item item) {
        String s = item.getTranslationKey();
        return normalizeId(s);
    }

    private static String normalizeId(String s) {
        String[] astring = s.split(DOT_REGEX);
        int i = astring.length;
        String[] astring1 = astring[i - 1].split("_");
        StringBuilder stringbuilder = new StringBuilder();

        for (String s1 : astring1) {
            stringbuilder.append(s1.substring(0, 1).toUpperCase()).append(s1.substring(1).toLowerCase()).append(" ");
        }

        return stringbuilder.toString().trim();
    }

    public RegistrySelectionMode getSelectionMode() {
        return this.selectionMode;
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    private void refresh() {
        this.available = new ArrayList();
        if (this.registryType == Item.class) {
            for (Field field : Items.class.getDeclaredFields()) {
                if (Item.class.isAssignableFrom(field.getType())) {
                    try {
                        this.available.add(field.get(null));
                    } catch (IllegalAccessException illegalaccessexception1) {
                        illegalaccessexception1.printStackTrace();
                    }
                }
            }
        } else if (this.registryType == Block.class) {
            for (Field field1 : Blocks.class.getDeclaredFields()) {
                if (Block.class.isAssignableFrom(field1.getType())) {
                    try {
                        this.available.add(field1.get(null));
                    } catch (IllegalAccessException illegalaccessexception) {
                        illegalaccessexception.printStackTrace();
                    }
                }
            }
        }
    }

    public void select(Object object) {
        if (this.selectionMode == RegistrySelectionMode.CHOOSE_ONE) {
            this.selected.clear();
        }

        if (this.available.contains(object)) {
            this.selected.add(object);
        }
    }

    public List getAvailable() {
        return this.available;
    }

    public List getSelected() {
        return this.selected;
    }

    public void setSelected(List list) {
        if (this.selectionMode == RegistrySelectionMode.CHOOSE_MULTI) {
            this.selected.clear();
            this.selected.addAll(list);
        }
    }
}
