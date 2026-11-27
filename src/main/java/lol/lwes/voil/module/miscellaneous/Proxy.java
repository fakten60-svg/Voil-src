package lol.lwes.voil.module.miscellaneous;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.miscellaneous.ProxyEntry;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ButtonSetting;
import lol.lwes.voil.setting.HiddenModeSetting;
import lol.lwes.voil.setting.ProxyTextSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.setting.StringSetting;
import lol.lwes.voil.setting.TransientModeSetting;
import lol.lwes.voil.setting.TransientStringSetting;
import lol.lwes.voil.util.ObjectList;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.crypto.ConfigCipher;
import lol.lwes.voil.util.proxy.ProxyManager;
import lol.lwes.voil.util.proxy.ProxyType;

public class Proxy extends Module {
    private static final Path PROXY_FILE = SystemUtil.getAppDataDir().resolve("proxies.json");
    public final BooleanSetting hideInfo = new BooleanSetting("Hide Info", false, "Hide proxy details (for recording)");
    private final StringSetting status = new TransientStringSetting("Status", "Not connected", "Status line");
    private final ButtonSetting toggleProxy = new ButtonSetting("Toggle Proxy", this::toggleProxy, "Toggles the proxy");
    private final TransientModeSetting currentProxy = new TransientModeSetting("Current Proxy", "1", "Select the current proxy", new String[0]);
    private final ButtonSetting newProxy = new ButtonSetting("New Proxy", this::addProxy, "Add a new proxy entry");
    private final ButtonSetting deleteProxy = new ButtonSetting("Delete Proxy", this::deleteProxy, "Delete the current proxy entry");
    private final TransientModeSetting proxyType = new HiddenModeSetting("Proxy Type", "SOCKS5", "Type of proxy", new String[]{"SOCKS4", "HTTP", "SOCKS5"});
    private final ProxyTextSetting host = new ProxyTextSetting(this, "Host", "127.0.0.1", "Proxy server ip address");
    public final ProxyTextSetting port = new ProxyTextSetting(this, "Port", "65535", "Proxy server port");
    private final ProxyTextSetting username = new ProxyTextSetting(this, "Username", "", "Username for proxy authentication");
    private final ProxyTextSetting password = new ProxyTextSetting(this, "Password", "", "Password for proxy authentication");
    private static ObjectList proxies = new ObjectList();
    private boolean connectionFailed;
    private boolean connecting;

    public Proxy() {
        super("Proxy", "Takes effect on the initial server join!", Category.Miscellaneous);
        Setting[] asetting = new Setting[]{
            this.hideInfo,
            this.status,
            this.toggleProxy,
            this.currentProxy,
            this.newProxy,
            this.deleteProxy,
            this.proxyType,
            this.host,
            this.port,
            this.username,
            this.password
        };
        this.addSettings(asetting);
        TransientModeSetting transientmodesetting2 = this.currentProxy;
        Runnable runnable = this::saveCurrentProxy;
        transientmodesetting2.setBeforeSwitch(runnable);
        TransientModeSetting transientmodesetting = this.currentProxy;
        Runnable runnable1 = this::loadSelectedProxy;
        transientmodesetting.setAfterSwitch(runnable1);
        TransientModeSetting transientmodesetting1 = this.proxyType;
        Runnable runnable2 = this::saveCurrentProxy;
        transientmodesetting1.setAfterSwitch(runnable2);
        ButtonSetting buttonsetting = this.deleteProxy;
        BooleanSupplier booleansupplier = () -> proxies.size() > 1;
        buttonsetting.setVisibleWhen(booleansupplier);
        this.addProxy();

        try {
            if (Files.exists(PROXY_FILE)) {
                String s = ConfigCipher.decryptString(1, false, PROXY_FILE.toString().getBytes(), Files.readString(PROXY_FILE));
                proxies = new Gson().fromJson(s, ObjectList.class);
                this.loadSelectedProxy();
                this.refreshProxyList(() -> 0);
            }
        } catch (Throwable throwable) {
        }
    }

    private void refreshProxyList(Supplier<Integer> supplier) {
        ArrayList<String> arraylist = new ArrayList<>();

        for (int i = 0; i < proxies.size(); i++) {
            arraylist.add(String.valueOf(i + 1));
        }

        TransientModeSetting transientmodesetting = this.currentProxy;
        int j = (Integer)supplier.get();
        ArrayList arraylist1 = arraylist;
        transientmodesetting.setModes(arraylist1, j);
    }

    private void deleteProxy() {
        if (proxies.size() >= 2) {
            this.saveCurrentProxy();
            int i = this.currentProxy.getIndex();
            proxies.remove(i);
            this.refreshProxyList(() -> Math.max(0, i - 1));
            this.loadSelectedProxy();
        }
    }

    public void saveCurrentProxy() {
        if (!proxies.isEmpty()) {
            ProxyEntry proxyconfig = (ProxyEntry)proxies.get(this.currentProxy.getIndex());
            proxyconfig.fields()[0] = this.proxyType.getMode();
            proxyconfig.fields()[1] = this.host.getValue();
            proxyconfig.fields()[2] = this.port.getValue();
            proxyconfig.fields()[3] = this.username.getValue();
            proxyconfig.fields()[4] = this.password.getValue();
        }
    }

    private void toggleProxy() {
        if (this.connecting) {
            return;
        }

        if (ProxyManager.getProxy() != null) {
            ProxyManager.clear();
            this.status.setValue("Not connected");
            return;
        }

        if (this.connectionFailed) {
            this.connectionFailed = false;
            this.status.setValue("Not connected");
            return;
        }

        int i = this.currentProxy.getIndex();
        ProxyEntry proxyconfig = (ProxyEntry)proxies.get(i);
        String s = i + 1 + "@:" + proxyconfig.fields()[2];
        lol.lwes.voil.util.proxy.ProxyConfig proxyconfig1;
        try {
            proxyconfig1 = lol.lwes.voil.util.proxy.ProxyConfig.create(
                ProxyType.valueOf(proxyconfig.fields()[0]),
                proxyconfig.fields()[1],
                Integer.parseInt(proxyconfig.fields()[2]),
                Objects.requireNonNullElse(proxyconfig.fields()[3], "").trim(),
                Objects.requireNonNullElse(proxyconfig.fields()[4], "").trim()
            );
        } catch (RuntimeException exception) {
            this.status.setValue("Invalid proxy settings");
            this.connectionFailed = true;
            return;
        }

        ProxyManager.setProxy(proxyconfig1);
        this.status.setValue("Using " + s + " (no connectivity test)");
    }

    @Override
    public void setEnabled(boolean flag) {
        if (flag) {
            this.connecting = false;
        } else {
            this.setExpanded(false);
        }
    }

    private void addProxy() {
        this.saveCurrentProxy();
        ProxyEntry proxyconfig = new ProxyEntry(new String[]{"SOCKS5", "127.0.0.1", "65535", "", ""});
        proxies.add(proxyconfig);
        this.refreshProxyList(() -> proxies.indexOf(proxyconfig));
        this.loadSelectedProxy();
    }

    private void loadSelectedProxy() {
        ProxyEntry proxyconfig = (ProxyEntry)proxies.get(this.currentProxy.getIndex());
        String s = proxyconfig.fields()[0];
        this.proxyType.setModeSilently(s);
        String s1 = proxyconfig.fields()[1];
        this.host.setRaw(s1);
        String s2 = proxyconfig.fields()[2];
        this.port.setRaw(s2);
        String s3 = proxyconfig.fields()[3];
        this.username.setRaw(s3);
        String s4 = proxyconfig.fields()[4];
        this.password.setRaw(s4);
    }

    private void printTrace(String s) {
        StackTraceElement[] astacktraceelement = Thread.currentThread().getStackTrace();
        boolean flag = false;
        ArrayList<StackTraceElement> arraylist = new ArrayList<>();
        int j = astacktraceelement.length;

        for (int i = j - 1; i >= 0; i--) {
            StackTraceElement stacktraceelement = astacktraceelement[i];
            if (flag) {
                arraylist.add(stacktraceelement);
            }

            if (stacktraceelement.getClassName().contains("voil")) {
                flag = true;
            }
        }

        List<StackTraceElement> list = Lists.reverse(arraylist);
        System.err.println(" Print Trace: " + s);

        for (StackTraceElement stacktraceelement1 : list) {
            PrintStream printstream = System.err;
            String s6 = stacktraceelement1.getClassName();
            String s7 = stacktraceelement1.getMethodName();
            int k = stacktraceelement1.getLineNumber();
            String s1 = stacktraceelement1.getFileName();
            String s2 = s7;
            String s3 = s6;
            printstream.println("  at " + s3 + "." + s2 + "(" + s1 + ":" + k + ")");
        }
    }

    @Override
    public void setExpanded(boolean flag) {
        if (!flag) {
            try {
                Files.createDirectories(PROXY_FILE.getParent());
                String s = new GsonBuilder().setPrettyPrinting().create().toJson(proxies);
                Files.writeString(PROXY_FILE, ConfigCipher.encryptString(1, false, PROXY_FILE.toString().getBytes(), s));
            } catch (Throwable throwable) {
            }
        }

        super.setExpanded(flag);
    }
}
