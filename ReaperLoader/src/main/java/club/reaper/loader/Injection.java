package club.reaper.loader;

import club.reaper.loader.accessor.FieldAccess;
import club.reaper.loader.accessor.Instance;
import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Getter
public class Injection {

    private File pluginFile;
    private File root;
    private Plugin handle;

    public Injection(File pluginFile) {
        this.pluginFile = pluginFile;
        Arrays.stream(this.pluginFile.getParentFile().listFiles()).forEach(file -> {
            String name = file.getName();
            if (name.endsWith(".jar") && !file.equals(this.pluginFile)) {
                file.delete();
            }
        });
    }

    public void enablePlugin(Plugin parent) {
        try {
            handle = parent.getPluginLoader().loadPlugin(pluginFile);
        } catch (InvalidPluginException e) {
            e.printStackTrace();
        }
        fixPluginDir(parent);
        fixConfig();
        checkFields(parent);
        parent.getServer().getPluginManager().enablePlugin(handle);
    }

    public void disablePlugin() {
        unload();
        if (pluginFile.exists())
            pluginFile.delete();
    }


    private void unload() {

        String name = handle.getName();

        PluginManager pluginManager = Bukkit.getPluginManager();

        pluginManager.disablePlugin(handle);

        List<Plugin> plugins = new FieldAccess(pluginManager.getClass(), "plugins").read(pluginManager);
        Map<String, Plugin> names = new FieldAccess(pluginManager.getClass(), "lookupNames").read(pluginManager);
        SimpleCommandMap commandMap = new FieldAccess(pluginManager.getClass(), "commandMap").read(pluginManager);
        Map<String, Command> commands = new FieldAccess(SimpleCommandMap.class, "knownCommands").read(commandMap);

        pluginManager.disablePlugin(handle);

        if (plugins != null)
            plugins.remove(handle);

        if (names != null)
            names.remove(name);

        if (commandMap != null) {
            for (Iterator<Map.Entry<String, Command>> it = commands.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Command> entry = it.next();
                if (entry.getValue() instanceof PluginCommand) {
                    PluginCommand c = (PluginCommand) entry.getValue();
                    if (c.getPlugin() == handle) {
                        c.unregister(commandMap);
                        it.remove();
                    }
                }
            }
        }

        ClassLoader cl = handle.getClass().getClassLoader();
        if (cl instanceof URLClassLoader) {
            new FieldAccess(cl.getClass(), "plugin").set(cl, null);
            new FieldAccess(cl.getClass(), "pluginInit").set(cl, null);
            closeLoader((URLClassLoader) cl);
        }

    }

    private void checkFields(Plugin parent) {
        Arrays.stream(handle.getClass().getFields()).filter(field -> field.isAnnotationPresent(Instance.class)).forEach(field -> {
            try {
                field.setAccessible(true);
                field.set(handle, parent);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        Arrays.stream(handle.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(Instance.class)).forEach(field -> {
            try {
                field.setAccessible(true);
                field.set(handle, parent);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    private void fixConfig() {
        File file;
        new FieldAccess(JavaPlugin.class, "configFile").set(handle, (file = new File(root, "config.yml")));
        new FieldAccess(JavaPlugin.class, "newConfig").set(handle, YamlConfiguration.loadConfiguration(file));
    }

    private void fixPluginDir(Plugin parent) {
        new FieldAccess(JavaPlugin.class, "dataFolder").set(handle, (root = new File(parent.getDataFolder().getParentFile(), handle.getDescription().getName().replaceAll(" ", "_"))));
    }

    private void closeLoader(URLClassLoader classLoader) {
        try {
            classLoader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
