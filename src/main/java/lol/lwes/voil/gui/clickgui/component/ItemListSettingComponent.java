package lol.lwes.voil.gui.clickgui.component;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.event.InputEventType;
import lol.lwes.voil.gui.clickgui.ClickGuiComponent;
import lol.lwes.voil.gui.clickgui.ClickGuiScreen;
import lol.lwes.voil.gui.clickgui.component.ListingFilter;
import lol.lwes.voil.setting.ItemListSetting;
import lol.lwes.voil.util.render.Render2DUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

public class ItemListSettingComponent extends SettingComponent {
    private final float rowHeight = 20.0F;
    private final float itemHeight = 18.0F;
    private final int columns = 2;
    private final float gap = 4.0F;
    private final float iconSize = 16.0F;
    private final float padding = 3.0F;
    private final float searchHeight = 14.0F;
    private final float cornerRadius = 4.0F;
    private final float headerHeight = 22.0F;
    private final int visibleRows = 5;
    private final ItemListSetting setting;
    private boolean pickerOpen = false;
    private ListingFilter category = ListingFilter.ALL;
    private final StringBuilder searchText = new StringBuilder();
    private boolean searchFocused = false;
    private float scrollOffset = 0.0F;
    private float scrollMax = 0.0F;
    private List filteredItems = new ArrayList();
    private String hoveredItemName = null;

    public ItemListSettingComponent(ItemListSetting itemlistsetting, ClickGuiScreen clickguiscreen) {
        super(itemlistsetting, clickguiscreen);
        this.setting = itemlistsetting;
    }


    @Override
    protected float getExtraHeight() {
        if (!this.pickerOpen) {
            return 0.0F;
        }

        float f = this.getSlotSize() + 4.0F;
        f += 19.0F;
        if (this.category == ListingFilter.ALL) {
            f += 18.0F;
        }

        return f + 114.0F;
    }

    private float getListHeight() {
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

    private float getItemGridTop() {
        return this.category == ListingFilter.ALL ? this.getGridX() + 14.0F + 4.0F : this.getScrollbarX() + 16.0F + 3.0F;
    }

    public void mouseClicked(double d0, double d1, int i) {
        float f = this.getListY();
        float f1 = this.getGridWidth();
        if (ClickGuiComponent.isPointInRect(f, f1, f + 20.0F, f1 + 20.0F, d0, d1)) {
            if (i == 0) {
                if (this.pickerOpen) {
                    this.clearSearch();
                } else {
                    this.pickerOpen = true;
                    List list1 = this.getCategories();
                    this.category = (ListingFilter)list1.get(0);
                    this.scrollOffset = 0.0F;
                    this.scrollMax = 0.0F;
                    this.searchText.setLength(0);
                    this.hoveredItemName = null;
                    this.searchFocused = this.category == ListingFilter.ALL;
                }
            } else if (i == 1) {
                this.setting.clear();
            }
        } else if (this.pickerOpen) {
            float f2 = this.getListHeight();
            float f3 = this.getSlotSize();
            if (d1 >= f2 && d1 <= f2 + f3 && !this.setting.isEmpty()) {
                int j = this.getVisibleCount();
                float f4 = (float)d0 - this.getInnerLeft();
                if (f4 >= 0.0F && f4 < j * 18.0F) {
                    int k = (int)(f4 / 18.0F);
                    int l = (int)((d1 - f2 + this.scrollMax) / 18.0);
                    int l1 = l * j;
                    int i1 = l1 + k;
                    if (i1 >= 0 && i1 < this.setting.getItemCount()) {
                        this.setting.removeAt(i1);
                        return;
                    }
                }
            }

            List list2 = this.getCategories();
            float f10 = this.getScrollbarX();
            float f11 = this.getInnerWidth() / list2.size();

            for (int i2 = 0; i2 < list2.size(); i2++) {
                float f14 = this.getInnerLeft() + i2 * f11;
                if (ClickGuiComponent.isPointInRect(f14, f10, f14 + f11, f10 + 16.0F, d0, d1)) {
                    ListingFilter listingfilter = (ListingFilter)list2.get(i2);
                    if (this.category != listingfilter) {
                        this.category = listingfilter;
                        this.scrollOffset = 0.0F;
                        this.searchText.setLength(0);
                        this.hoveredItemName = null;
                        this.searchFocused = this.category == ListingFilter.ALL;
                    }

                    return;
                }
            }

            if (this.category == ListingFilter.ALL) {
                float f12 = this.getGridX();
                float f16 = this.getInnerLeft();
                float f17 = this.getInnerRight();
                float f6 = f12 + 14.0F;
                float f7 = f17;
                float f8 = f12;
                float f9 = f16;
                if (ClickGuiComponent.isPointInRect(f9, f8, f7, f6, d0, d1)) {
                    this.searchFocused = true;
                    return;
                }
            }

            float f13 = this.getItemGridTop();
            float f15 = this.getGridHeight();
            if (d1 >= f13 && d1 <= f13 + f15 && i == 0) {
                int j2 = this.getColumnCount();
                float f5 = (float)d0 - this.getGridY();
                if (f5 >= 0.0F && f5 < j2 * 22.0F) {
                    int j1 = (int)(f5 / 22.0F);
                    int k2 = (int)((d1 - f13 + this.scrollOffset) / 22.0);
                    int k1 = k2 * j2 + j1;
                    List list = this.getItemsForCategory(this.category);
                    if (k1 >= 0 && k1 < list.size()) {
                        ItemListSetting itemlistsetting = this.setting;
                        Item item = (Item)list.get(k1);
                        itemlistsetting.toggleItem(item);
                        this.setting.notifyChange();
                    }
                }
            } else {
                this.searchFocused = false;
            }
        }
    }

    private void drawItemRow(int i, int j) {
        List list = this.getItemsForCategory(this.category);
        int k = this.getColumnCount();
        float f = this.getGridY();
        float f1 = this.getItemGridTop();
        float f2 = this.getGridHeight();
        Render2DUtil.drawRoundedRect(this.getInnerLeft(), f1, this.getInnerWidth(), f2, 3.0F, 1.0F, new Color(0, 0, 0, 60));
        int j2 = list.size();
        j2 += k;
        int l = (j2 - 1) / k;
        float f3 = Math.max(0.0F, l * 22.0F - f2);
        this.scrollOffset = Math.max(0.0F, Math.min(this.scrollOffset, f3));
        int i1 = (int)Math.floor(this.scrollOffset / 22.0F);
        j2 = i1 + 5;
        int j1 = j2 + 1;

        for (int k1 = Math.max(0, i1); k1 <= j1 && k1 < l; k1++) {
            float f4 = f1 + k1 * 22.0F - this.scrollOffset;
            if (!(f4 + 22.0F <= f1) && !(f4 >= f1 + f2)) {
                for (int l1 = 0; l1 < k; l1++) {
                    j2 = k1 * k;
                    int i2 = j2 + l1;
                    if (i2 >= list.size()) {
                        break;
                    }

                    float f5;
                    Item item;
                    boolean flag2;
                    label51: {
                        f5 = f + l1 * 22.0F;
                        item = (Item)list.get(i2);
                        if (j >= f1 && j <= f1 + f2) {
                            float f12 = f5 + 22.0F;
                            float f13 = f4 + 22.0F;
                            double d1 = i;
                            double d0 = j;
                            if (ClickGuiComponent.isPointInRect(f5, f4, f12, f13, d1, d0)) {
                                flag2 = true;
                                break label51;
                            }
                        }

                        flag2 = false;
                    }

                    boolean flag = flag2;
                    boolean flag1 = this.setting.containsItem(item);
                    if (f4 >= f1 && f4 + 22.0F <= f1 + f2) {
                        if (flag1) {
                            float f10 = f5 + 1.0F;
                            float f8 = f4 + 1.0F;
                            Render2DUtil.drawRoundedRect(
                                f10,
                                f8,
                                20.0F,
                                20.0F,
                                2.0F,
                                1.0F,
                                new Color(GuiTheme.accentColor1.getRed(), GuiTheme.accentColor1.getGreen(), GuiTheme.accentColor1.getBlue(), 70)
                            );
                        }

                        if (flag) {
                            float f11 = f5 + 1.0F;
                            float f9 = f4 + 1.0F;
                            Render2DUtil.drawRoundedRect(f11, f9, 20.0F, 20.0F, 2.0F, 1.0F, new Color(255, 255, 255, 35));
                        }
                    }

                    float f6 = f5 + 3.0F;
                    float f7 = f4 + 3.0F;
                    if (f7 + 16.0F > f1 && f7 < f1 + f2) {
                        Render2DUtil.drawItem(new ItemStack(item), f6, f7, 1.0F, false);
                    }
                }
            }
        }
    }

    private void clearSearch() {
        this.pickerOpen = false;
        this.searchFocused = false;
        this.searchText.setLength(0);
        this.hoveredItemName = null;
    }

    public void render(int i, int j) {
        String s = this.setting.getName();
        VoilClient.fonts.getSelectedFont().render(s, this.getInnerLeft(), this.getTextY(), GuiTheme.normalTextColor.getRGB());
        float f = this.getListY();
        float f1 = this.getGridWidth();
        float f4 = f + 20.0F;
        float f5 = f1 + 20.0F;
        double d1 = i;
        double d0 = j;
        boolean flag = ClickGuiComponent.isPointInRect(f, f1, f4, f5, d1, d0);
        Color color = this.pickerOpen
            ? new Color(GuiTheme.accentColor1.getRed(), GuiTheme.accentColor1.getGreen(), GuiTheme.accentColor1.getBlue(), 55)
            : new Color(0, 0, 0, flag ? 110 : 70);
        Color color1 = this.pickerOpen ? GuiTheme.accentColor1 : new Color(255, 255, 255, flag ? 100 : 45);
        Render2DUtil.drawRoundedRect(f, f1, 20.0F, 20.0F, 3.0F, 1.0F, color);
        Render2DUtil.drawOutline(f, f1, 20.0F, 20.0F, 3.0F, 0.3F, 0.4F, color1);
        String s1 = String.valueOf(this.setting.getItemCount());
        float f2 = this.setting.getItemCount() >= 100 ? 9.0F : (this.setting.getItemCount() >= 10 ? 11.0F : 13.0F);
        float f3 = VoilClient.fonts.getSelectedFont().getWidth(s1, f2);
        VoilClient.fonts
            .getSelectedFont()
            .render(
                s1,
                f + (20.0F - f3) / 2.0F,
                f1 + (20.0F - f2) / 2.0F - 2.0F,
                f2,
                this.pickerOpen ? GuiTheme.accentColor1.getRGB() : GuiTheme.normalTextColor.getRGB()
            );
        if (this.pickerOpen) {
            this.drawSelectedItems(i, j);
            this.drawCategoryTabs(i, j);
            if (this.category == ListingFilter.ALL) {
                this.refreshFilter();
            }

            this.drawItemRow(i, j);
        }
    }

    public void mouseScrolled(double d0, double d1, double d2) {
        if (this.pickerOpen) {
            float f = this.getListHeight();
            float f1 = this.getSlotSize();
            if (d0 >= this.getInnerLeft() && d0 <= this.getInnerRight() && d1 >= f && d1 <= f + f1 && !this.setting.isEmpty()) {
                this.scrollMax -= (float)d2 * 18.0F;
                this.addActiveInput(InputEventType.Scroll);
            } else {
                float f2 = this.getItemGridTop();
                float f3 = this.getGridHeight();
                if (d0 >= this.getInnerLeft() && d0 <= this.getInnerRight() && d1 >= f2 && d1 <= f2 + f3) {
                    this.scrollOffset -= (float)d2 * 22.0F;
                    this.addActiveInput(InputEventType.Scroll);
                }
            }
        }
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (this.pickerOpen && this.searchFocused && this.category == ListingFilter.ALL) {
            if (chr >= ' ' && chr != 127) {
                this.searchText.append(chr);
                this.updateFilteredItems();
            }
        }
    }

    public void keyPressed(int i) {
        if (this.pickerOpen) {
            if (i == 256) {
                if (this.searchText.length() > 0) {
                    this.searchText.setLength(0);
                } else {
                    this.clearSearch();
                }
            } else if (this.searchFocused && this.category == ListingFilter.ALL) {
                if (i == 259 && this.searchText.length() > 0) {
                    StringBuilder stringbuilder = this.searchText;
                    int j = this.searchText.length();
                    stringbuilder.deleteCharAt(j - 1);
                } else if (i == 257) {
                    this.updateFilteredItems();
                    if (!this.filteredItems.isEmpty()) {
                        ItemListSetting itemlistsetting = this.setting;
                        Item item = (Item)this.filteredItems.get(0);
                        itemlistsetting.toggleItem(item);
                        this.setting.notifyChange();
                    }
                }
            }
        }
    }

    private float getGridX() {
        return this.getScrollbarX() + 16.0F + 3.0F;
    }

    private float getGridY() {
        float f = this.getColumnCount() * 22.0F;
        return this.getInnerLeft() + (this.getInnerWidth() - f) / 2.0F;
    }

    private int getColumnCount() {
        return Math.max(1, (int)Math.floor(this.getInnerWidth() / 22.0F));
    }

    private List getItemsForCategory(ListingFilter listingfilter) {
        int i = listingfilter.ordinal();
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

    private void refreshFilter() {
        float f6 = this.getGridX();
        float f7 = this.getInnerLeft();
        float f8 = this.getInnerWidth();
        Render2DUtil.drawRoundedRect(f7, f6, f8, 14.0F, 3.0F, 1.0F, new Color(0, 0, 0, 95));
        Color color = this.searchFocused ? GuiTheme.accentColor1 : new Color(255, 255, 255, 60);
        float f = 0.4F;
        float f1 = 0.3F;
        float f2 = 3.0F;
        float f3 = 14.0F;
        float f4 = f8;
        float f5 = f6;
        Render2DUtil.drawOutline(f7, f5, f4, f3, f2, f1, f, color);
        boolean flag = this.searchText.length() == 0;
        String s1;
        if (flag) {
            s1 = "Search items...";
        } else {
            s1 = this.searchText.toString();
        }

        String s = s1;
        int i = flag ? new Color(170, 170, 170, 150).getRGB() : GuiTheme.normalTextColor.getRGB();
        VoilClient.fonts.getSelectedFont().render(s, f7 + 4.0F, f6 + 0.5F, i);
        if (this.searchFocused && System.currentTimeMillis() / 500L % 2L == 0L) {
            float f10 = f7 + 4.0F + (flag ? 0.0F : VoilClient.fonts.getSelectedFont().getWidth(this.searchText.toString())) + 1.0F;
            float f9 = f6 + 3.0F;
            Render2DUtil.drawRoundedRect(f10, f9, 0.5F, 8.0F, 0.25F, 1.0F, new Color(255, 255, 255, 200));
        }
    }

    private List getCategories() {
        ArrayList arraylist = new ArrayList();
        if (!this.setting.getRecommendedItems().isEmpty()) {
            arraylist.add(ListingFilter.RECOMMENDED);
        }

        arraylist.add(ListingFilter.ALL);
        return arraylist;
    }

    private static String getCategoryLabel(ListingFilter listingfilter) {
        int i = listingfilter.ordinal();
        String s;
        if (i != 0) {
            if (i != 1) {
                throw new MatchException(null, null);
            }

            s = "All";
        } else {
            s = "Recommended";
        }

        return s;
    }

    private float getGridWidth() {
        return this.width + (this.getRowHeight() - 20.0F) / 2.0F;
    }

    private float getGridHeight() {
        return 110.0F;
    }

    private float getScrollbarX() {
        return this.getListHeight() + this.getSlotSize() + 4.0F;
    }

    private float getSlotSize() {
        if (this.setting.isEmpty()) {
            return 16.0F;
        }

        int i = this.getVisibleCount();
        int j = this.setting.getItemCount();
        j += i;
        return Math.min(Math.max((j - 1) / i, 1), 2) * 18.0F;
    }

    private float getListY() {
        return this.getInnerRight() - 20.0F;
    }

    private int getVisibleCount() {
        return Math.max(1, (int)Math.floor(this.getInnerWidth() / 18.0F));
    }

    private void drawSelectedItems(int i, int j) {
        float f = this.getListHeight();
        float f1 = this.getSlotSize();
        float f2 = this.getInnerLeft();
        float f3 = this.getInnerWidth();
        Render2DUtil.drawRoundedRect(f2, f, f3, f1, 3.0F, 1.0F, new Color(0, 0, 0, 60));
        if (this.setting.isEmpty()) {
            String s = "No items selected. Click below to add.";
            float f10 = VoilClient.fonts.getSelectedFont().getWidth(s);
            VoilClient.fonts.getSelectedFont().render(s, f2 + (f3 - f10) / 2.0F, f + (f1 - 9.0F) / 2.0F, new Color(170, 170, 170, 200).getRGB());
        } else {
            int k = this.getVisibleCount();
            List list = this.setting.getItems();
            int j2 = list.size();
            j2 += k;
            int l = (j2 - 1) / k;
            float f4 = Math.max(0.0F, l * 18.0F - f1);
            this.scrollMax = Math.max(0.0F, Math.min(this.scrollMax, f4));
            int i1 = (int)Math.floor(this.scrollMax / 18.0F);
            j2 = i1 + 2;
            int j1 = j2 + 1;

            for (int k1 = Math.max(0, i1); k1 <= j1 && k1 < l; k1++) {
                float f5 = f + k1 * 18.0F - this.scrollMax;
                if (!(f5 + 18.0F <= f) && !(f5 >= f + f1)) {
                    for (int l1 = 0; l1 < k; l1++) {
                        int i2 = k1 * k + l1;
                        if (i2 >= list.size()) {
                            break;
                        }

                        float f6;
                        boolean flag;
                        label51: {
                            f6 = f2 + l1 * 18.0F;
                            if (j >= f && j <= f + f1) {
                                float f12 = f6 + 18.0F;
                                float f13 = f5 + 18.0F;
                                double d1 = i;
                                double d0 = j;
                                if (ClickGuiComponent.isPointInRect(f6, f5, f12, f13, d1, d0)) {
                                    flag = true;
                                    break label51;
                                }
                            }

                            flag = false;
                        }

                        if (flag && f5 >= f && f5 + 18.0F <= f + f1) {
                            float f11 = f6 + 1.0F;
                            float f9 = f5 + 1.0F;
                            Render2DUtil.drawRoundedRect(f11, f9, 16.0F, 16.0F, 2.0F, 1.0F, new Color(255, 80, 80, 80));
                        }

                        float f7 = f6 + 1.0F;
                        float f8 = f5 + 1.0F;
                        if (f8 + 16.0F > f && f8 < f + f1) {
                            Render2DUtil.drawItem(new ItemStack((ItemConvertible)list.get(i2)), f7, f8, 1.0F, false);
                        }
                    }
                }
            }
        }
    }

    private void drawCategoryTabs(int i, int j) {
        List list = this.getCategories();
        float f5 = this.getScrollbarX();
        float f6 = this.getInnerWidth() / list.size();

        for (int k = 0; k < list.size(); k++) {
            ListingFilter listingfilter = (ListingFilter)list.get(k);
            float f7 = this.getInnerLeft() + k * f6;
            boolean flag = listingfilter == this.category;
            float f11 = f7 + f6;
            float f12 = f5 + 16.0F;
            double d1 = i;
            double d0 = j;
            boolean flag1 = ClickGuiComponent.isPointInRect(f7, f5, f11, f12, d1, d0);
            Color color1 = flag
                ? new Color(GuiTheme.accentColor1.getRed(), GuiTheme.accentColor1.getGreen(), GuiTheme.accentColor1.getBlue(), 60)
                : new Color(0, 0, 0, flag1 ? 60 : 35);
            float f = 1.0F;
            float f1 = 0.0F;
            float f2 = 16.0F;
            float f3 = f6;
            float f4 = f5;
            float f9 = f7;
            Render2DUtil.drawRoundedRect(f9, f4, f3, f2, f1, f, color1);
            if (flag) {
                float f10 = f5 + 16.0F - 1.0F;
                Color color = GuiTheme.accentColor1;
                f = 1.0F;
                f1 = 0.0F;
                f2 = 1.0F;
                f3 = f6;
                f4 = f10;
                Render2DUtil.drawRoundedRect(f7, f4, f3, f2, f1, f, color);
            }

            String s = getCategoryLabel(listingfilter);
            float f8 = VoilClient.fonts.getSelectedFont().getWidth(s);
            VoilClient.fonts
                .getSelectedFont()
                .render(s, f7 + (f6 - f8) / 2.0F, f5 + 1.5F, flag ? GuiTheme.accentColor1.getRGB() : GuiTheme.normalTextColor.getRGB());
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
        this.keyPressed(i);
    }
}
