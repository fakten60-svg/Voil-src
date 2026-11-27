package lol.lwes.voil.gui.clickgui.component;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.event.InputEventType;
import lol.lwes.voil.gui.clickgui.ClickGuiComponent;
import lol.lwes.voil.gui.clickgui.ClickGuiScreen;
import lol.lwes.voil.gui.clickgui.TabFilter;
import lol.lwes.voil.setting.ItemSlotsSetting;
import lol.lwes.voil.util.render.Render2DUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

public class ItemSlotsSettingComponent extends SettingComponent {
    private int columns = 9;
    private float slotSize = 20.0F;
    private float slotGap = 1.0F;
    private float iconSize = 16.0F;
    private float padding = 3.0F;
    private float searchHeight = 14.0F;
    private float cornerRadius = 4.0F;
    private float headerHeight = 22.0F;
    private int visibleRows = 6;
    private final ItemSlotsSetting setting;
    private boolean pickerOpen = false;
    private int selectedSlot = -1;
    private TabFilter category = TabFilter.ALL;
    private final StringBuilder searchText = new StringBuilder();
    private boolean searchFocused = false;
    private float scrollOffset = 0.0F;
    private List filteredItems = new ArrayList();
    private String hoveredItemName = null;

    public ItemSlotsSettingComponent(ItemSlotsSetting itemslotssetting, ClickGuiScreen clickguiscreen) {
        super(itemslotssetting, clickguiscreen);
        this.setting = itemslotssetting;
    }

    @Override
    protected float getExtraHeight() {
        if (!this.pickerOpen) {
            return 0.0F;
        }

        float f = this.iconSize + this.padding + this.headerHeight * this.visibleRows + 4.0F;
        if (this.category == TabFilter.ALL) {
            f += this.searchHeight + this.cornerRadius;
        }

        return f;
    }

    private int getRowCount() {
        return Math.max(1, (int)Math.floor(this.getInnerWidth() / this.headerHeight));
    }

    private void drawSlot(int i, int j) {
        this.drawPicker(i, j);
        if (this.category == TabFilter.ALL) {
            this.drawSearchBox();
        }

        this.drawItemGrid(i, j);
    }

    private float getSlotsHeight() {
        return this.getPickerHeight() + this.iconSize + this.padding;
    }

    public void mouseClicked(double d0, double d1, int i) {
        for (int j = 0; j < this.columns; j++) {
            float f = this.getSlotX(j);
            float f1 = this.getScrollbarX();
            if (ClickGuiComponent.isPointInRect(f, f1, f + this.slotSize, f1 + this.slotSize, d0, d1)) {
                if (i == 0) {
                    if (this.pickerOpen && this.selectedSlot == j) {
                        this.clearSearch();
                    } else {
                        this.pickerOpen = true;
                        this.selectedSlot = j;
                        List list = this.getCategories();
                        this.category = (TabFilter)list.get(0);
                        this.scrollOffset = 0.0F;
                        this.searchText.setLength(0);
                        this.hoveredItemName = null;
                        this.searchFocused = this.category == TabFilter.ALL;
                    }
                } else if (i == 1) {
                    this.setting.clearSlot(j);
                    if (this.pickerOpen && this.selectedSlot == j) {
                        this.clearSearch();
                    }
                }

                return;
            }
        }

        if (this.pickerOpen) {
            List list2 = this.getCategories();
            float f8 = this.getPickerHeight();
            float f9 = this.getInnerWidth() / list2.size();

            for (int k1 = 0; k1 < list2.size(); k1++) {
                float f2 = this.getInnerLeft() + k1 * f9;
                if (ClickGuiComponent.isPointInRect(f2, f8, f2 + f9, f8 + this.iconSize, d0, d1)) {
                    TabFilter tabfilter = (TabFilter)list2.get(k1);
                    if (this.category != tabfilter) {
                        this.category = tabfilter;
                        this.scrollOffset = 0.0F;
                        this.searchText.setLength(0);
                        this.hoveredItemName = null;
                        this.searchFocused = this.category == TabFilter.ALL;
                    }

                    return;
                }
            }

            if (this.category == TabFilter.ALL) {
                float f10 = this.getSlotsHeight();
                float f13 = this.getInnerLeft();
                float f4 = f10 + this.searchHeight;
                float f5 = this.getInnerRight();
                float f6 = f10;
                float f7 = f13;
                if (ClickGuiComponent.isPointInRect(f7, f6, f5, f4, d0, d1)) {
                    this.searchFocused = true;
                    return;
                }
            }

            float f11 = this.getItemGridTop();
            float f12 = this.getSearchY();
            if (d1 >= f11 && d1 <= f11 + f12 && i == 0) {
                int l1 = this.getRowCount();
                float f3 = (float)d0 - this.getGridY();
                if (f3 >= 0.0F && f3 < l1 * this.headerHeight) {
                    int k = (int)(f3 / this.headerHeight);
                    int l = (int)((d1 - f11 + this.scrollOffset) / this.headerHeight) * l1 + k;
                    List list1 = this.getItemsForCategory(this.category);
                    if (l >= 0 && l < list1.size() && this.selectedSlot >= 0) {
                        ItemSlotsSetting itemslotssetting = this.setting;
                        int j1 = this.selectedSlot;
                        Item item = (Item)list1.get(l);
                        int i1 = j1;
                        itemslotssetting.setItem(i1, item);
                        this.clearSearch();
                    }
                }
            } else {
                this.searchFocused = false;
            }
        }
    }

    private void clearSearch() {
        this.pickerOpen = false;
        this.selectedSlot = -1;
        this.searchFocused = false;
        this.searchText.setLength(0);
        this.hoveredItemName = null;
    }

    public void render(int i, int j) {
        VoilClient.fonts.getSelectedFont().render(this.setting.getName(), this.getInnerLeft(), this.getTextY(), GuiTheme.normalTextColor.getRGB());

        for (int k = 0; k < this.columns; k++) {
            float f = this.getSlotX(k);
            float f1 = this.getScrollbarX();
            float f4 = f + this.slotSize;
            float f5 = f1 + this.slotSize;
            double d1 = i;
            double d0 = j;
            boolean flag = ClickGuiComponent.isPointInRect(f, f1, f4, f5, d1, d0);
            boolean flag1 = this.pickerOpen && k == this.selectedSlot;
            Color color = flag1
                ? new Color(GuiTheme.accentColor1.getRed(), GuiTheme.accentColor1.getGreen(), GuiTheme.accentColor1.getBlue(), 55)
                : new Color(0, 0, 0, flag ? 110 : 70);
            Color color1 = flag1 ? GuiTheme.accentColor1 : new Color(255, 255, 255, flag ? 100 : 45);
            Render2DUtil.drawRoundedRect(f, f1, this.slotSize, this.slotSize, 3.0F, 1.0F, color);
            Render2DUtil.drawOutline(f, f1, this.slotSize, this.slotSize, 3.0F, 0.3F, 0.4F, color1);
            if (!this.setting.isSlotEmpty(k)) {
                Item item = this.setting.getItem(k);
                float f2 = f + (this.slotSize - 16.0F) / 2.0F;
                float f3 = f1 + (this.slotSize - 16.0F) / 2.0F;
                Render2DUtil.drawItem(new ItemStack(item), f2, f3, 1.0F, false);
            }
        }

        if (this.pickerOpen) {
            this.drawSlot(i, j);
        }
    }

    public void mouseScrolled(double d0, double d1, double d2) {
        if (this.pickerOpen) {
            float f = this.getItemGridTop();
            float f1 = this.getSearchY();
            if (d0 >= this.getInnerLeft() && d0 <= this.getInnerRight() && d1 >= f && d1 <= f + f1) {
                this.scrollOffset = this.scrollOffset - (float)d2 * this.headerHeight;
                this.addActiveInput(InputEventType.Scroll);
            }
        }
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (this.pickerOpen && this.searchFocused && this.category == TabFilter.ALL) {
            if (chr >= ' ' && chr != 127) {
                this.searchText.append(chr);
                this.updateFilteredItems();
            }
        }
    }

    public void handlePickerKey(int i) {
        if (this.pickerOpen) {
            if (i == 256) {
                if (this.searchText.length() > 0) {
                    this.searchText.setLength(0);
                } else {
                    this.clearSearch();
                }
            } else if (this.searchFocused && this.category == TabFilter.ALL) {
                if (i == 259 && this.searchText.length() > 0) {
                    this.searchText.deleteCharAt(this.searchText.length() - 1);
                } else if (i == 257) {
                    this.updateFilteredItems();
                    if (!this.filteredItems.isEmpty() && this.selectedSlot >= 0) {
                        ItemSlotsSetting itemslotssetting = this.setting;
                        int k = this.selectedSlot;
                        Item item = (Item)this.filteredItems.get(0);
                        int j = k;
                        itemslotssetting.setItem(j, item);
                        this.clearSearch();
                    }
                }
            }
        }
    }

    private float getPickerHeight() {
        return this.getBottom();
    }

    private void updateFilteredItems() {
        String s = this.searchText.toString().toLowerCase().trim();
        if (!s.equals(this.hoveredItemName)) {
            this.hoveredItemName = s;
            ArrayList arraylist = new ArrayList();

            for (Item item : Registries.ITEM) {
                if (item != Items.AIR && (s.isEmpty() || Registries.ITEM.getId(item).getPath().contains(s))) {
                    arraylist.add(item);
                }
            }

            this.filteredItems = arraylist;
            this.scrollOffset = 0.0F;
        }
    }

    private float getSearchY() {
        return this.headerHeight * this.visibleRows;
    }

    private List getItemsForCategory(TabFilter tabfilter) {
        int i = tabfilter.ordinal();
        List list;
        if (i != 0) {
            if (i != 1) {
                throw new MatchException(null, null);
            }

            this.updateFilteredItems();
            list = this.filteredItems;
        } else {
            list = this.setting.getRecommendedItems();
        }

        return list;
    }

    private void drawItemGrid(int i, int j) {
        List list = this.getItemsForCategory(this.category);
        int k = this.getRowCount();
        float f = this.getGridY();
        float f1 = this.getItemGridTop();
        float f2 = this.getSearchY();
        Render2DUtil.drawRoundedRect(this.getInnerLeft(), f1, this.getInnerWidth(), f2, 3.0F, 1.0F, new Color(0, 0, 0, 60));
        int j2 = list.size();
        j2 += k;
        int l = (j2 - 1) / k;
        float f3 = Math.max(0.0F, l * this.headerHeight - f2);
        this.scrollOffset = Math.max(0.0F, Math.min(this.scrollOffset, f3));
        int i1 = (int)Math.floor(this.scrollOffset / this.headerHeight);
        j2 = i1 + this.visibleRows;
        int j1 = j2 + 1;

        for (int k1 = Math.max(0, i1); k1 <= j1 && k1 < l; k1++) {
            float f4 = f1 + k1 * this.headerHeight - this.scrollOffset;
            if (!(f4 + this.headerHeight <= f1) && !(f4 >= f1 + f2)) {
                for (int l1 = 0; l1 < k; l1++) {
                    j2 = k1 * k;
                    int i2 = j2 + l1;
                    if (i2 >= list.size()) {
                        break;
                    }

                    float f5;
                    boolean flag;
                    label47: {
                        f5 = f + l1 * this.headerHeight;
                        if (j >= f1 && j <= f1 + f2) {
                            float f10 = f5 + this.headerHeight;
                            float f11 = f4 + this.headerHeight;
                            double d1 = i;
                            double d0 = j;
                            if (ClickGuiComponent.isPointInRect(f5, f4, f10, f11, d1, d0)) {
                                flag = true;
                                break label47;
                            }
                        }

                        flag = false;
                    }

                    if (flag && f4 >= f1 && f4 + this.headerHeight <= f1 + f2) {
                        float f9 = f5 + 1.0F;
                        float f8 = f4 + 1.0F;
                        Render2DUtil.drawRoundedRect(f9, f8, this.headerHeight - 2.0F, this.headerHeight - 2.0F, 2.0F, 1.0F, new Color(255, 255, 255, 35));
                    }

                    float f6 = f5 + (this.headerHeight - 16.0F) / 2.0F;
                    float f7 = f4 + (this.headerHeight - 16.0F) / 2.0F;
                    if (f7 + 16.0F > f1 && f7 < f1 + f2) {
                        Render2DUtil.drawItem(new ItemStack((ItemConvertible)list.get(i2)), f6, f7, 1.0F, false);
                    }
                }
            }
        }
    }

    private List getCategories() {
        ArrayList arraylist = new ArrayList();
        if (!this.setting.getRecommendedItems().isEmpty()) {
            arraylist.add(TabFilter.RECOMMENDED);
        }

        arraylist.add(TabFilter.ALL);
        return arraylist;
    }

    private static String getCategoryLabel(TabFilter tabfilter) {
        int i = tabfilter.ordinal();
        String s;
        if (i != 0) {
            if (i != 1) {
                throw new MatchException(null, null);
            }

            s = "";
        } else {
            s = "Recommended";
        }

        return s;
    }

    private float getGridWidth() {
        return this.getInnerRight() - this.getGridHeight();
    }

    private float getItemGridTop() {
        return this.category == TabFilter.ALL
            ? this.getSlotsHeight() + this.searchHeight + this.cornerRadius
            : this.getPickerHeight() + this.iconSize + this.padding;
    }

    private float getGridY() {
        float f = this.getRowCount() * this.headerHeight;
        return this.getInnerLeft() + (this.getInnerWidth() - f) / 2.0F;
    }

    private float getScrollbarX() {
        return this.width + (this.getRowHeight() - this.slotSize) / 2.0F;
    }

    private float getGridHeight() {
        return this.columns * this.slotSize + (this.columns - 1) * this.slotGap;
    }

    private float getSlotX(int i) {
        return this.getGridWidth() + i * (this.slotSize + this.slotGap);
    }

    private void drawPicker(int i, int j) {
        List list = this.getCategories();
        float f5 = this.getPickerHeight();
        float f6 = this.getInnerWidth() / list.size();

        for (int k = 0; k < list.size(); k++) {
            TabFilter tabfilter = (TabFilter)list.get(k);
            float f7 = this.getInnerLeft() + k * f6;
            boolean flag = tabfilter == this.category;
            float f11 = f7 + f6;
            float f12 = f5 + this.iconSize;
            double d1 = i;
            double d0 = j;
            boolean flag1 = ClickGuiComponent.isPointInRect(f7, f5, f11, f12, d1, d0);
            Color color1 = flag
                ? new Color(GuiTheme.accentColor1.getRed(), GuiTheme.accentColor1.getGreen(), GuiTheme.accentColor1.getBlue(), 60)
                : new Color(0, 0, 0, flag1 ? 60 : 35);
            float f = 1.0F;
            float f1 = 0.0F;
            float f2 = this.iconSize;
            float f3 = f6;
            float f4 = f5;
            float f9 = f7;
            Render2DUtil.drawRoundedRect(f9, f4, f3, f2, f1, f, color1);
            if (flag) {
                float f10 = f5 + this.iconSize - 1.0F;
                Color color = GuiTheme.accentColor1;
                f = 1.0F;
                f1 = 0.0F;
                f2 = 1.0F;
                f3 = f6;
                f4 = f10;
                Render2DUtil.drawRoundedRect(f7, f4, f3, f2, f1, f, color);
            }

            String s = getCategoryLabel(tabfilter);
            float f8 = VoilClient.fonts.getSelectedFont().getWidth(s);
            VoilClient.fonts
                .getSelectedFont()
                .render(
                    s, f7 + (f6 - f8) / 2.0F, f5 + (this.iconSize - 13.0F) / 2.0F, flag ? GuiTheme.accentColor1.getRGB() : GuiTheme.normalTextColor.getRGB()
                );
        }
    }

    private void drawSearchBox() {
        float f6 = this.getSlotsHeight();
        float f7 = this.getInnerLeft();
        float f8 = this.getInnerWidth();
        Render2DUtil.drawRoundedRect(f7, f6, f8, this.searchHeight, 3.0F, 1.0F, new Color(0, 0, 0, 95));
        Color color = this.searchFocused ? GuiTheme.accentColor1 : new Color(255, 255, 255, 60);
        float f = 0.4F;
        float f1 = 0.3F;
        float f2 = 3.0F;
        float f3 = this.searchHeight;
        float f4 = f8;
        float f5 = f6;
        Render2DUtil.drawOutline(f7, f5, f4, f3, f2, f1, f, color);
        boolean flag = this.searchText.length() == 0;
        String s1;
        if (flag) {
            byte b2 = 0;
            b2 = 1;
            s1 = "Search items...";
        } else {
            s1 = this.searchText.toString();
        }

        String s = s1;
        int i = flag ? new Color(170, 170, 170, 150).getRGB() : GuiTheme.normalTextColor.getRGB();
        VoilClient.fonts.getSelectedFont().render(s, f7 + 4.0F, f6 + (this.searchHeight - 13.0F) / 2.0F, i);
        if (this.searchFocused && System.currentTimeMillis() / 500L % 2L == 0L) {
            float f10 = f7 + 4.0F + (flag ? 0.0F : VoilClient.fonts.getSelectedFont().getWidth(this.searchText.toString())) + 1.0F;
            float f9 = f6 + 3.0F;
            Render2DUtil.drawRoundedRect(f10, f9, 0.5F, this.searchHeight - 6.0F, 0.25F, 1.0F, new Color(255, 255, 255, 200));
        }
    }

    @Override
    public void mouseClicked2(double d0, double d1, int i) {
        this.mouseClicked(d0, d1, i);
    }

    @Override
    public void render2(MatrixStack matrixstack, int i, int j, float f) {
        this.render(i, j);
    }

    @Override
    public void mouseScrolled2(double d0, double d1, double d3, double d2) {
        this.mouseScrolled(d0, d1, d2);
    }

    @Override
    public void keyReleased2(int i, int j, int k) {
        this.handlePickerKey(i);
    }
}
