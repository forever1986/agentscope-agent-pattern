package com.lin.subagent.core.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 从带有 YAML 前置标记的 Markdown 文件中加载Agent的Spec
 */
public final class AgentSpecLoader {

    private static final Logger logger = LoggerFactory.getLogger(AgentSpecLoader.class);

    private static final Yaml YAML = new Yaml();

    private AgentSpecLoader() {}

    public static List<AgentSpec> loadFromDirectory(String directoryPath) throws IOException {
        if (directoryPath==null || directoryPath.isEmpty()) {
            return List.of();
        }
        return loadFromDirectory(Paths.get(directoryPath));
    }

    public static List<AgentSpec> loadFromDirectory(Path rootPath) throws IOException {
        if (rootPath == null || !Files.exists(rootPath)) {
            logger.warn("Agent spec directory does not exist: {}", rootPath);
            return List.of();
        }
        if (!Files.isDirectory(rootPath)) {
            throw new IOException("Path is not a directory: " + rootPath);
        }

        List<AgentSpec> specs = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(rootPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".md"))
                    .forEach(
                            path -> {
                                try {
                                    AgentSpec spec = loadFromFile(path);
                                    if (spec != null) {
                                        specs.add(spec);
                                        logger.debug(
                                                "Loaded agent spec: {} from {}", spec.name(), path);
                                    }
                                } catch (Exception e) {
                                    logger.warn(
                                            "Failed to load agent spec from {}: {}",
                                            path,
                                            e.getMessage());
                                }
                            });
        }
        return specs;
    }

    public static AgentSpec loadFromFile(Path filePath) throws IOException {
        String content = Files.readString(filePath, StandardCharsets.UTF_8);
        return parse(content);
    }

    /**
     * 将带有 YAML 前置标记的 Markdown 内容解析为 AgentSpec。
     */
    public static AgentSpec parse(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return null;
        }
        if (!markdown.startsWith("---")) {
            logger.warn("Agent spec must start with YAML front matter (---)");
            return null;
        }

        int endIndex = markdown.indexOf("---", 3);
        if (endIndex == -1) {
            logger.warn("Agent spec front matter not properly closed with ---");
            return null;
        }

        String frontMatterStr = markdown.substring(3, endIndex).trim();
        String content = markdown.substring(endIndex + 3).trim();

        Map<String, Object> frontMatter;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = YAML.load(frontMatterStr);
            frontMatter = parsed;
        } catch (Exception e) {
            logger.warn("Failed to parse YAML front matter: {}", e.getMessage());
            return null;
        }

        if (frontMatter==null || frontMatter.isEmpty()) {
            return null;
        }

        String name = getString(frontMatter, "name");
        String description = getString(frontMatter, "description");

        if (name==null || name.isEmpty()) {
            logger.warn("Agent spec missing required 'name' in front matter");
            return null;
        }
        if (description==null || description.isEmpty()) {
            logger.warn("Agent spec missing required 'description' in front matter");
            return null;
        }

        List<String> toolNames = parseToolNames(getString(frontMatter, "tools"));
        String model = getString(frontMatter, "model");

        return new AgentSpec(name, description, content, toolNames, model);
    }

    private static String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString().trim() : null;
    }

    private static List<String> parseToolNames(String toolsStr) {
        if (toolsStr==null || toolsStr.isEmpty()) {
            return List.of();
        }
        return Stream.of(toolsStr.split(","))
                .map(String::trim)
                .filter(str -> !str.isEmpty())
                .toList();
    }
}
