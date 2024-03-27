package org.example;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.ListProperty;

import java.util.*;
import java.util.stream.Collectors;

public class ApplicationDeploymentClasspathBuilder {

    private static final String DEPLOYMENT_CONFIGURATION_SUFFIX = "Deployment";
    private static final String PLATFORM_CONFIGURATION_SUFFIX = "Platform";
    public static final String DEV_MODE_CONFIGURATION_NAME = "quarkusDev";

    private static String getLaunchModeAlias(LaunchMode mode) {
        if (mode == LaunchMode.DEVELOPMENT) {
            return "Dev";
        }
        if (mode == LaunchMode.TEST) {
            return "Test";
        }
        return "Prod";
    }

    private static String getRuntimeConfigName(LaunchMode mode, boolean base) {
        final StringBuilder sb = new StringBuilder();
        sb.append("quarkus").append(getLaunchModeAlias(mode));
        if (base) {
            sb.append("Base");
        }
        sb.append("RuntimeClasspathConfiguration");
        return sb.toString();
    }

    public static String getBaseRuntimeConfigName(LaunchMode mode) {
        return getRuntimeConfigName(mode, true);
    }

    public static String getFinalRuntimeConfigName(LaunchMode mode) {
        return getRuntimeConfigName(mode, false);
    }

    public static void initConfigurations(Project project) {
        final ConfigurationContainer configContainer = project.getConfigurations();

        // Custom configuration for dev mode
        configContainer.register(DEV_MODE_CONFIGURATION_NAME, config -> {
            config.extendsFrom(configContainer.getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME));
            config.setCanBeConsumed(false);
        });

        // Base runtime configurations for every launch mode
        configContainer
            .register(ApplicationDeploymentClasspathBuilder.getBaseRuntimeConfigName(LaunchMode.TEST), config -> {
                config.extendsFrom(configContainer.getByName(JavaPlugin.TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME));
                config.setCanBeConsumed(false);
            });

        configContainer
            .register(ApplicationDeploymentClasspathBuilder.getBaseRuntimeConfigName(LaunchMode.NORMAL), config -> {
                config.extendsFrom(configContainer.getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME));
                config.setCanBeConsumed(false);
            });

        configContainer
            .register(ApplicationDeploymentClasspathBuilder.getBaseRuntimeConfigName(LaunchMode.DEVELOPMENT), config -> {
                config.extendsFrom(
                    configContainer.getByName(DEV_MODE_CONFIGURATION_NAME),
                    configContainer.getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME),
                    configContainer.getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME));
                config.setCanBeConsumed(false);
            });
    }

    private final Project project;
    private final LaunchMode mode;

    private final String runtimeConfigurationName;
    private final String deploymentConfigurationName;
    private final String compileOnlyConfigurationName;


    public ApplicationDeploymentClasspathBuilder(Project project, LaunchMode mode) {
        this.project = project;
        this.mode = mode;
        this.runtimeConfigurationName = getFinalRuntimeConfigName(mode);
        this.deploymentConfigurationName = toDeploymentConfigurationName(this.runtimeConfigurationName);
        this.compileOnlyConfigurationName = "quarkus" + getLaunchModeAlias(mode) + "CompileOnlyConfiguration";

        setUpRuntimeConfiguration();
        setUpDeploymentConfiguration();
        setUpCompileOnlyConfiguration();
    }


    private void setUpRuntimeConfiguration() {
        if (!project.getConfigurations().getNames().contains(this.runtimeConfigurationName)) {
            project.getConfigurations().register(this.runtimeConfigurationName, configuration -> {
                configuration.setCanBeConsumed(false);
                configuration.extendsFrom(
                    project.getConfigurations()
                        .getByName(ApplicationDeploymentClasspathBuilder.getBaseRuntimeConfigName(mode)));
            });
        }
    }

    private void setUpDeploymentConfiguration() {
        if (!project.getConfigurations().getNames().contains(this.deploymentConfigurationName)) {
            project.getConfigurations().register(this.deploymentConfigurationName, configuration -> {
                configuration.setCanBeConsumed(false);
            });
        }
    }

    private void setUpCompileOnlyConfiguration() {
        if (!project.getConfigurations().getNames().contains(compileOnlyConfigurationName)) {
            project.getConfigurations().register(compileOnlyConfigurationName, config -> {
                config.extendsFrom(project.getConfigurations().getByName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME));
                config.shouldResolveConsistentlyWith(getDeploymentConfiguration());
                config.setCanBeConsumed(false);
            });
        }
    }

    /**
     * Forces deployment configuration to resolve to discover conditional dependencies.
     */
    public Configuration getRuntimeConfiguration() {
        this.getDeploymentConfiguration().resolve();
        return project.getConfigurations().getByName(this.runtimeConfigurationName);
    }

    public Configuration getDeploymentConfiguration() {
        return project.getConfigurations().getByName(this.deploymentConfigurationName);
    }

    public static String toDeploymentConfigurationName(String baseConfigurationName) {
        return baseConfigurationName + DEPLOYMENT_CONFIGURATION_SUFFIX;
    }
}
