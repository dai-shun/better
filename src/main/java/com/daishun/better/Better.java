package com.daishun.better;

import com.daishun.better.dto.BetterConfig;
import com.daishun.better.exception.BetterException;
import com.daishun.better.utils.FileUtils;
import com.daishun.better.utils.GitUtils;
import lombok.SneakyThrows;
import org.apache.commons.cli.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
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

    public static void main(String[] args) {
        parseOption(args);
    }

    private static void parseOption(String[] args) {
        Options options = createOptions();
        BetterConfig config = getConfig(options, args);
        String projectPath = config.getProjectPath();
        File projectDir = FileUtils.getFile(projectPath);
        FileUtils.requireEmptyDir(projectDir);
        //初始化项目
        initProject(projectPath, config.getTemplate());
        resolveFiles(config);
    }

    private static Options createOptions() {
        Options options = new Options();
        Option opt = new Option("h", "help", false, "Print help");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("G", "groupId", true, "Maven groupId");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("A", "artifactId", true, "Maven artifactId");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("T", "template", true, "Spring template name");
        opt.setRequired(true);
        options.addOption(opt);
        return options;
    }

    @SneakyThrows
    private static BetterConfig getConfig(Options options, String[] args) {
        HelpFormatter help = new HelpFormatter();
        help.setWidth(110);
        CommandLine commandLine = null;
        CommandLineParser parser = new PosixParser();
        commandLine = parser.parse(options, args);
        if (commandLine.hasOption('h')) {
            help.printHelp("better", options, true);
        }
        Option[] opts = commandLine.getOptions();
        BetterConfig config = new BetterConfig();
        if (opts != null) {
            for (Option option : opts) {
                String name = option.getLongOpt();
                String value = commandLine.getOptionValue(name);
                if ("groupId".equals(name)) {
                    config.setGroupId(value);
                } else if ("artifactId".equals(name)) {
                    config.setArtifactId(value);
                } else if ("template".equals(name)) {
                    config.setTemplate(value);
                }
            }
        }
        config.setProjectPath(FileUtils.getUserDir());
        return config;
    }


    private static void resolveFiles(BetterConfig config) {
        Map<String, String> replaceMap = getReplaceMap(config.getProjectPath(), config);
        replaceAll(config.getProjectPath(), new File(config.getProjectPath()), replaceMap);
    }

    @SneakyThrows
    private static Map<String, String> getReplaceMap(String templatePath, BetterConfig config) {
        File pomFile = FileUtils.getFile(templatePath, "pom.xml");
        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(FileUtils.openInputStream(pomFile));
        String text = document.getTextContent();
        Node projectNode = document.getElementsByTagName("project").item(0);
        NodeList nodeList = projectNode.getChildNodes();
        String groupId = config.getGroupId();
        String artifactId = config.getArtifactId();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if ("artifactId".equals(node.getNodeName())) {
                artifactId = node.getTextContent().trim();
            } else if ("groupId".equals(node.getNodeName())) {
                groupId = node.getTextContent().trim();
            }
        }
        Map<String, String> replaceMap = new HashMap<>();
        replaceMap.put(artifactId, config.getArtifactId());
        replaceMap.put(artifactId.replaceAll("\\-", ""), config.getArtifactId().replaceAll("\\-", ""));
        replaceMap.put(groupId.replaceAll("\\.", "\\\\\\."), config.getGroupId());
        String templateGroupPath = groupId.replaceAll("\\.", File.separator);
        String newGroupPath = config.getGroupId().replaceAll("\\.", File.separator);
        replaceMap.put(templateGroupPath, newGroupPath);
        return replaceMap;
    }

    @SneakyThrows
    private static void initProject(String projectPath, String template) {
        String uuid = UUID.randomUUID().toString();
        File tempDir = new File(projectPath, "." + uuid);
        try {
            if (!tempDir.mkdir()) {
                throw new BetterException("init project fail!");
            }
            GitUtils.cloneRepository(tempDir.getAbsolutePath(), "https://github.com/dai-shun/spring-examples.git");
            File destFile = FileUtils.getFile(projectPath);
            File[] templates = tempDir.listFiles();
            if (templates == null) {
                throw new BetterException("cannot find any template!");
            }
            for (File file : templates) {
                if (!template.equals(file.getName())) {
                    FileUtils.deleteQuietly(file);
                }
            }
            File srcFile = FileUtils.getFile(tempDir, template);
            if (srcFile == null) {
                throw new BetterException("template %s not exist!", template);
            }
            File[] templateFiles = srcFile.listFiles();
            if (templateFiles != null) {
                for (File fromFile : templateFiles) {
                    File toFile = FileUtils.getFile(destFile, fromFile.getName());
                    FileUtils.move(fromFile, toFile);
                }
            }
            FileUtils.deleteQuietly(FileUtils.getFile(destFile, template));
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
