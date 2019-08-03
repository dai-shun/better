package com.daishun.better;

import com.alibaba.fastjson.JSON;
import com.daishun.better.dto.BetterConfig;
import com.daishun.better.exception.BetterException;
import com.daishun.better.utils.FileUtils;
import com.daishun.better.utils.GitUtils;
import lombok.SneakyThrows;
import org.eclipse.jgit.util.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author daishun
 * @since 2019/8/2
 */
public class Better {

    private static final String CHARSET = StandardCharsets.UTF_8.name();

    public static void main(String[] args) {
        String localPath = FileUtils.getUserDir();
        File projectDir = FileUtils.getFile(localPath);
        if (!projectDir.exists()) {
            throw new BetterException("%s not exist", projectDir.getName());
        } else {
            File[] files = projectDir.listFiles();
            if (files != null && files.length != 0) {
                throw new BetterException("require a empty directory,but %s is not empty!", projectDir.getName());
            }
        }
        BetterConfig config = getConfig(projectDir);
        String exampleProjectName = config.getTemplate();
        String examplePackageName = exampleProjectName.replaceAll("\\-", "");
        String projectName = config.getArtifactId();
        String packageName = projectName.replaceAll("\\-", "");
        //初始化项目
        initProject(localPath, exampleProjectName);
        //替换项目模板
        Map<String, String> replaceMap = new HashMap<>();
        replaceMap.put(exampleProjectName, projectName);
        replaceMap.put(examplePackageName, packageName);
        replaceMap.put("com\\.daishun", config.getGroupId());
        replaceMap.put("com" + File.separator + "daishun", config.getGroupId().replaceAll("\\.", File.separator));
        replaceAll(localPath, projectDir, replaceMap);
    }

    @SneakyThrows
    private static BetterConfig getConfig(File projectDir) {
        File file = FileUtils.getFile(projectDir, "better.json");
        BetterConfig config = new BetterConfig();
        if (file.exists()) {
            String configJson = FileUtils.readFileToString(file, CHARSET);
            config = JSON.parseObject(configJson, BetterConfig.class);
        }
        if (StringUtils.isEmptyOrNull(config.getGroupId())) {
            config.setGroupId("com.daishun");
        }
        if (StringUtils.isEmptyOrNull(config.getArtifactId())) {
            config.setTemplate(projectDir.getName());
        }
        if (StringUtils.isEmptyOrNull(config.getTemplate())) {
            config.setTemplate("spring-mybatis-simple-example");
        }
        return config;
    }

    @SneakyThrows
    private static void initProject(String localPath, String exampleName) {
        String uuid = UUID.randomUUID().toString();
        File tempDir = new File(localPath, "." + uuid);
        try {
            tempDir.mkdir();
            GitUtils.cloneRepository(tempDir.getAbsolutePath(), "https://github.com/dai-shun/spring-examples.git");
            File destFile = FileUtils.getFile(localPath);
            for (File file : tempDir.listFiles()) {
                //只保留一个模板文件夹
                if (!exampleName.equals(file.getName())) {
                    FileUtils.deleteQuietly(file);
                }
            }
            if (tempDir.listFiles() == null || tempDir.listFiles().length == 0) {
                throw new BetterException("指定的模板%s不存在", exampleName);
            }
            File srcFile = FileUtils.getFile(tempDir, exampleName);
            for (File fromFile : srcFile.listFiles()) {
                File toFile = FileUtils.getFile(destFile, fromFile.getName());
                FileUtils.move(fromFile, toFile);
            }
            FileUtils.deleteQuietly(FileUtils.getFile(destFile, exampleName));
        } finally {
            if (tempDir.exists()) {
                FileUtils.deleteDirectory(tempDir);
            }
        }
    }

    @SneakyThrows
    private static void replaceAll(String localPath, File file, Map<String, String> replaceMap) {
        if (file.isFile()) {
            String data = FileUtils.readFileToString(file, StandardCharsets.UTF_8.name());
            String relativePath = FileUtils.getRelativePath(localPath, file.getAbsolutePath());
            for (String key : replaceMap.keySet()) {
                String value = replaceMap.get(key);
                data = data.replaceAll(key, value);
                relativePath = relativePath.replaceAll(key, value);
            }
            FileUtils.deleteQuietly(file);
            String newFilePath = localPath + File.separator + relativePath;
            File newFile = new File(newFilePath);
            FileUtils.touch(newFile);
            FileUtils.writeStringToFile(newFile, data, StandardCharsets.UTF_8.name());
            System.out.println(String.format("creating: ./%s", relativePath));
        } else {
            for (File cFile : file.listFiles()) {
                replaceAll(localPath, cFile, replaceMap);
                File[] files = file.listFiles();
                if (files == null || files.length == 0) {
                    FileUtils.deleteDirectory(file);
                }
            }
        }
    }
}
