package club.reaper.loader.main;

import club.reaper.loader.Injection;
import club.reaper.loader.downloader.FileDownloader;
import lombok.Getter;


import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Loader extends JavaPlugin {

    @Getter
    private final String prefix = "mldr";

    @Getter
    private final boolean async = false;

    public static Loader INSTANCE;

    @Getter
    private Injection plugin;

    @Override
    public void onEnable() {
        INSTANCE = this;
        loadPlugin();

    }


    @Override
    public void onDisable() {
        if (plugin != null)
            plugin.disablePlugin();
    }

    public void reloadPlugin() {
        if (plugin != null) {
            plugin.disablePlugin();
            loadPlugin();
        }
    }

    private void loadPlugin() {
        if (async) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
						try {
							plugin = new Injection(new FileDownloader().download());
						} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
								| IllegalBlockSizeException | BadPaddingException
								| InvalidAlgorithmParameterException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    plugin.enablePlugin(Loader.this);
                }
            }.runTask(this);
        } else {
            try {
				try {
					plugin = new Injection(new FileDownloader().download());
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
						| IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            plugin.enablePlugin(Loader.this);
        }
     
    }
    

}
