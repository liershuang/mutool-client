package com.mutool.client.plugin;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.mutool.client.constant.SystemConstant;
import com.mutool.client.constant.UrlConstant;
import com.mutool.client.model.PluginJarInfo;
import com.mutool.client.utils.XJavaFxSystemUtil;
import com.mutool.core.util.ConfigureUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


@Slf4j
@Component
public class PluginManager {

    /**
     * 插件列表
     */
    private final List<PluginJarInfo> pluginList = new ArrayList<>();


    /**
     * 获取插件列表（只读）
     *
     * @return
     */
    public List<PluginJarInfo> getPluginList() {
        return Collections.unmodifiableList(this.pluginList);
    }

    /**
     * 获取插件文件对象列表
     */
    public List<File> getPluginJarFileList() {
        return FileUtil.loopFiles(getLocalPluginJarDir(), file -> file.getName().endsWith(".jar"));
    }

    /**
     * 通过插件名获取插件完整信息
     *
     * @param jarName
     * @return
     */
    public PluginJarInfo getPlugin(String jarName) {
        return this.pluginList.stream()
                .filter(plugin -> Objects.equals(plugin.getJarName(), jarName))
                .findFirst().orElse(null);
    }

    /**
     * 加载本地配置文件中插件信息列表到内存
     */
    public void loadLocalPlugins() {
        try {
            Path path = Paths.get(getLocalPluginConfig());
            if (!Files.exists(path)) {
                return;
            }
            String json = new String(Files.readAllBytes(path), SystemConstant.CHARSET_UTF_8);
            JSON.parseArray(json, PluginJarInfo.class).forEach(plugin -> {
                //启动加载设置下载标志为false，实际以已有的jar包存在为准
                plugin.setIsDownload(Boolean.FALSE);
                //如果不存在则添加，否则更新本地版本号、是否下载、是否启用标志
                this.addOrUpdatePlugin(plugin, exist -> {
                    exist.setLocalVersionNumber(plugin.getLocalVersionNumber());
//                    exist.setIsDownload(plugin.getIsDownload());
                    exist.setIsEnable(plugin.getIsEnable());
                });
            });
        } catch (IOException e) {
            log.error("读取插件配置失败", e);
        }
    }

    /**
     * 下载或更新服务起插件列表信息
     */
    public void loadServerPlugins() {
        try {
            String json = HttpUtil.get(UrlConstant.SERVER_PLUGINS_URL);
            JSON.parseArray(json, PluginJarInfo.class).forEach(plugin -> {
                //如果不存在则添加，否则更新远程名称、简介、版本、下载地址
                this.addOrUpdatePlugin(plugin, exist -> {
                    exist.setName(plugin.getName());
                    exist.setSynopsis(plugin.getSynopsis());
                    exist.setVersion(plugin.getVersion());
                    exist.setVersionNumber(plugin.getVersionNumber());
                    exist.setDownloadUrl(plugin.getDownloadUrl());
                });
            });
            //更新本地配置信息
            savePluginInfoToLocalFile();
        } catch (Exception e) {
            log.error("下载插件列表失败", e);
        }
    }

    /**
     * 异步下载或更新服务器插件列表信息，便于界面上展示 loading 动画
     *
     * @return
     */
    public CompletableFuture<Void> loadServerPluginsAsync() {
        return CompletableFuture.runAsync(this::loadServerPlugins);
    }

    ////////////////////////////////////////////////////////////// 下载插件

    /**
     * 下载插件到本地
     *
     * @param pluginJarInfo
     * @return
     * @throws IOException
     */
    @Deprecated
    public File downloadPlugin(PluginJarInfo pluginJarInfo) throws IOException {
        PluginJarInfo plugin = getPlugin(pluginJarInfo.getJarName());
        if (plugin == null) {
            throw new IllegalStateException("没有找到插件 " + pluginJarInfo.getJarName());
        }

        File file = new File(getLocalPluginJarDir(), pluginJarInfo.getJarName() + "-" + pluginJarInfo.getVersion() + ".jar");
        HttpUtil.downloadFile(pluginJarInfo.getDownloadUrl(), file);

        plugin.setIsDownload(true);
        plugin.setIsEnable(true);
        plugin.setLocalVersionNumber(plugin.getVersionNumber());
        this.savePluginInfoToLocalFile();

        return file;
    }

    public File downloadPlugin(String pluginJarName) throws IOException {
        PluginJarInfo plugin = getPlugin(pluginJarName);
        if (plugin == null) {
            throw new IllegalStateException("没有找到插件 " + pluginJarName);
        }
        String jarFilePath = getLocalPluginJarDir() + pluginJarName + "-" + plugin.getVersion() + ".jar";
        FileUtil.del(jarFilePath);
        HttpUtil.downloadFile(plugin.getDownloadUrl(), jarFilePath);

        plugin.setIsDownload(true);
        plugin.setIsEnable(true);
        plugin.setLocalVersionNumber(plugin.getVersionNumber());
        //保存插件配置信息到本地
        if (FileUtil.exist(jarFilePath)) {
            this.savePluginInfoToLocalFile();
        }
        return FileUtil.file(jarFilePath);
    }

    /**
     * 删除插件，重启后生效
     *
     * @param pluginJarName
     */
    public void deletePlugin(String pluginJarName) {
        PluginJarInfo plugin = getPlugin(pluginJarName);
        if (plugin == null) {
            throw new IllegalStateException("没有找到插件 " + pluginJarName);
        }
        String jarFilePath = getLocalPluginJarDir() + pluginJarName + "-" + plugin.getVersion() + ".jar";
        FileUtil.del(jarFilePath);

        this.pluginList.stream().filter(i -> i.getJarName().equals(pluginJarName))
                .findFirst().get().setIsDownload(Boolean.FALSE);
    }

    /**
     * 保存插件配置到本地文件
     *
     * @throws IOException
     */
    public void savePluginInfoToLocalFile() throws IOException {
        String pluginConfigFile = getLocalPluginConfig();
        String json = JSON.toJSONString(this.pluginList, true);
        FileUtil.writeBytes(json.getBytes(SystemConstant.CHARSET_UTF_8), pluginConfigFile);
    }

    /**
     * 添加插件jar包到系统中
     */
    public void addPluginJarToSystem() {
        try {
            // 获取所有的.jar
            List<File> jarFiles = FileUtil.loopFiles(getLocalPluginJarDir(), file -> file.getName().endsWith(".jar"));
            if (CollectionUtil.isEmpty(jarFiles)) {
                return;
            }
            for (File file : jarFiles) {
                XJavaFxSystemUtil.addJarClass(file);
                //更新jar下载标志
                String fileName = file.getName().split("-")[0];
                PluginJarInfo plugin = getPlugin(fileName);
                if (plugin != null) {
                    plugin.setIsDownload(Boolean.TRUE);
                }
            }
        } catch (Exception e) {
            log.error("添加libs中jar包到系统中异常:", e);
        }
    }


    /**
     * 新增或更新插件信息到内存
     *
     * @param pluginJarInfo 插件信息
     * @param ifExists      插件信息处理逻辑
     */
    private void addOrUpdatePlugin(PluginJarInfo pluginJarInfo, Consumer<PluginJarInfo> ifExists) {
        PluginJarInfo exists = getPlugin(pluginJarInfo.getJarName());
        if (exists == null) {
            this.pluginList.add(pluginJarInfo);
        } else {
            ifExists.accept(exists);
        }
    }

    /**
     * 获取本地插件jar包目录
     */
    public String getLocalPluginJarDir() {
        return ConfigureUtil.getPluginPath() + "libs/";
    }

    /**
     * 本地插件配置信息json文件路径
     */
    private static String getLocalPluginConfig() {
        return ConfigureUtil.getPluginPath() + "system_plugin_list.json";
    }

}
