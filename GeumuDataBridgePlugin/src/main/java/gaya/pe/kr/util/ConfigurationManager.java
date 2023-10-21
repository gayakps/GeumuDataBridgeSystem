package gaya.pe.kr.util;

import gaya.pe.kr.core.GeumuDataBridgePlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class ConfigurationManager {

    // 수많은 Config 생성 및 제거를 여기서 담당함.

    private static class SingleTon {
        private static final ConfigurationManager CONFIGURATION_MANAGER = new ConfigurationManager();
    }

    public static synchronized ConfigurationManager getInstance() {
        return SingleTon.CONFIGURATION_MANAGER;
    }

    Plugin plugin = GeumuDataBridgePlugin.getPlugin();
    File pluginDataFolder = GeumuDataBridgePlugin.getPlugin().getDataFolder();

    public FileConfiguration getConfiguration(String relativePath, String resourcePath) {
        File file = new File(pluginDataFolder, relativePath);
        return ( file.exists() ? YamlConfiguration.loadConfiguration(file) : getDefaultConfiguration(file, resourcePath));
    }

    private FileConfiguration getDefaultConfiguration(File path, String resourcePath) {
        try {
            Reader reader = new InputStreamReader(plugin.getResource(resourcePath), StandardCharsets.UTF_8);
            FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(reader);
            fileConfiguration.save(path);
            GeumuDataBridgePlugin.log(String.format("&f[ &b%s &f]의&f 설정파일이 정상 &e생성&f 되었습니다", path.getName()));
            return fileConfiguration;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveConfiguration(Plugin plugin, FileConfiguration configuration, String relativePath) {
        try {
            configuration.save(new File(plugin.getDataFolder(), relativePath));
            GeumuDataBridgePlugin.log(String.format("&f[&6%s&f]의 파일이 &e성공적&f으로 저장 되었습니다", relativePath));
        } catch (IOException e) {
            e.printStackTrace();
            GeumuDataBridgePlugin.log(String.format("&f[&6%s&f]의 파일의 저장을 &4실패 했습니다", relativePath));
        }

    }

    public Set<String> getConfigurationSection(FileConfiguration configuration, String path) throws NullPointerException {
        try {
            return configuration.getConfigurationSection(path).getKeys(false);
        } catch ( NullPointerException e ) {
            return new HashSet<>();
        }
    }




}