package com.yisiliang.idea.plugins.apusic.conf;

import com.intellij.execution.configurations.LogFileOptions;
import com.intellij.execution.configurations.PredefinedLogFile;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ApusicLogFile {

    public static final String TOMCAT_LOCALHOST_LOG_ID = "Apusic Localhost Log";
    public static final String TOMCAT_CATALINA_LOG_ID = "Apusic Catalina Log";
    public static final String TOMCAT_ACCESS_LOG_ID = "Apusic Access Log";
    public static final String TOMCAT_MANAGER_LOG_ID = "Apusic Manager Log";
    public static final String TOMCAT_HOST_MANAGER_LOG_ID = "Apusic Host Manager Log";

    private final String id;
    private final String filename;
    private boolean enabled;

    public ApusicLogFile(String id, String filename) {
        this.id = id;
        this.filename = filename;
    }

    public ApusicLogFile(String id, String filename, boolean enabled) {
        this(id, filename);
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public LogFileOptions createLogFileOptions(PredefinedLogFile file, @Nullable Path logsDirPath) {
        Path logsPath = logsDirPath == null ? Paths.get("logs") : logsDirPath;
        return new LogFileOptions(file.getId(), logsPath.resolve(filename) + ".*", file.isEnabled());
    }

    public PredefinedLogFile createPredefinedLogFile() {
        return new PredefinedLogFile(id, enabled);
    }

}
