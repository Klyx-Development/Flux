package org.klyx.flux.plugin;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public final class ExampleFiles {
    private final JavaPlugin plugin;
    private final String label;
    private final String resourceFolder;
    private final String suffix;
    private final List<String> names;
    private final Path directory;

    public ExampleFiles(
            JavaPlugin plugin, String label, String resourceFolder, String suffix, List<String> names, Path directory
    ) {
        this.plugin = plugin;
        this.label = label;
        this.resourceFolder = resourceFolder;
        this.suffix = suffix;
        this.names = names;
        this.directory = directory;
    }

    public void copyMissing() {
        try {
            Files.createDirectories(this.directory);
        } catch (IOException e) {
            this.plugin.getSLF4JLogger().error("Couldn't create {}", this.directory, e);
            return;
        }

        for (String name : this.names) {
            copy(name);
        }
    }

    private void copy(String name) {
        Path target = this.directory.resolve(name + this.suffix);
        if (Files.exists(target)) return;

        try (InputStream stream = this.plugin.getResource(this.resourceFolder + "/" + name + this.suffix)) {
            if (stream == null) {
                this.plugin.getSLF4JLogger().warn("The {} \"{}\" is missing", this.label, name);
                return;
            }

            Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            this.plugin.getSLF4JLogger().error("Couldn't copy the {} {}", this.label, name, e);
        }
    }
}
