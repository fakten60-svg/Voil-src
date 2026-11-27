package lol.lwes.voil.setting;

import com.google.gson.JsonElement;
import java.util.Arrays;
import java.util.function.BooleanSupplier;
import lol.lwes.voil.util.MemoryWiper;

public abstract class Setting {
    private final String name;
    private final String description;
    private BooleanSupplier visibility;

    public Setting(String s, String s1) {
        this.name = s;
        this.description = s1;
    }

    public boolean isVisible() {
        return this.visibility == null || this.visibility.getAsBoolean();
    }

    public static void dependAny(Setting setting, ModeSetting modesetting, String[] astring) {
        BooleanSupplier booleansupplier = () -> Arrays.stream(astring).anyMatch(modesetting::is);
        setting.setVisibleWhen(booleansupplier);
    }

    public String getName() {
        return this.name.replaceAll("[&@#$%!\\^\\*\\(\\)\\-_+=]", "");
    }

    public boolean isPersistent() {
        return true;
    }

    public String getDescription() {
        return this.description.replaceAll("[&@#$%!\\^\\*\\(\\)\\-_+=]", "");
    }

    public static void dependSettingFalse(Setting setting, BooleanSetting booleansetting) {
        BooleanSupplier booleansupplier = () -> !booleansetting.isEnabled();
        setting.setVisibleWhen(booleansupplier);
    }

    public static void dependSetting(Setting setting, ModeSetting modesetting, String[] astring) {
        BooleanSupplier booleansupplier = () -> Arrays.stream(astring).anyMatch(modesetting::is);
        setting.setVisibleWhen(booleansupplier);
    }

    public static void dependSetting(Setting setting, BooleanSupplier booleansupplier) {
        setting.setVisibleWhen(booleansupplier);
    }

    public static void dependSetting(Setting setting, boolean flag) {
        BooleanSupplier booleansupplier = () -> flag;
        setting.setVisibleWhen(booleansupplier);
    }

    public void setVisibleWhen(BooleanSupplier booleansupplier) {
        this.visibility = booleansupplier;
    }

    public void a() {
        String s = this.name;
        MemoryWiper.wipeString(s);
    }

    public static void dependSettingFalse(Setting setting, boolean flag) {
        BooleanSupplier booleansupplier = () -> !flag;
        setting.setVisibleWhen(booleansupplier);
    }

    public static void dependSettingFalse(Setting setting, BooleanSupplier booleansupplier) {
        BooleanSupplier booleansupplier1 = () -> !booleansupplier.getAsBoolean();
        setting.setVisibleWhen(booleansupplier1);
    }

    public static void dependSetting(Setting setting, BooleanSupplier booleansupplier, ModeSetting modesetting, String[] astring) {
        BooleanSupplier booleansupplier1 = () -> booleansupplier.getAsBoolean() && Arrays.stream(astring).anyMatch(modesetting::is);
        setting.setVisibleWhen(booleansupplier1);
    }

    public static void dependSetting(Setting setting, BooleanSetting booleansetting) {
        BooleanSupplier booleansupplier = booleansetting::isEnabled;
        setting.setVisibleWhen(booleansupplier);
    }

    public JsonElement toJson() {
        throw new UnsupportedOperationException("not persistent");
    }

    public static void dependSetting(Setting setting, String s, ModeSetting modesetting) {
        BooleanSupplier booleansupplier = () -> modesetting.is(s);
        setting.setVisibleWhen(booleansupplier);
    }

    public static void dependSetting(Setting setting, String s, MultiSelectSetting multiselectsetting) {
        BooleanSupplier booleansupplier = () -> multiselectsetting.getOption(s).isPresent();
        setting.setVisibleWhen(booleansupplier);
    }

    public static void dependSettingBoolFalse(Setting setting, BooleanSetting booleansetting) {
        BooleanSupplier booleansupplier = () -> !booleansetting.isEnabled();
        setting.setVisibleWhen(booleansupplier);
    }

    public void fromJson() {
        throw new UnsupportedOperationException("not persistent");
    }

    public static void onSwitch(ModeSetting modesetting, Runnable runnable, String[] astring) {
        Runnable runnable1 = () -> {
            if (Arrays.stream(astring).anyMatch(modesetting::is)) {
                runnable.run();
            }
        };
        modesetting.setAfterSwitch(runnable1);
    }

    public void fromJson2(JsonElement jsonelement) {
        this.fromJson();
    }
}
