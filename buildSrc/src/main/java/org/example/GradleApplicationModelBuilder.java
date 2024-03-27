package org.example;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.tooling.provider.model.ParameterizedToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

import java.io.File;
import java.util.function.Consumer;

public class GradleApplicationModelBuilder implements ParameterizedToolingModelBuilder<ModelParameter> {
    @Override
    public boolean canBuild(String modelName) {
        return false;
    }

    @Override
    public Object buildAll(String modelName, Project project) {
        return null;
    }

    @Override
    public Class<ModelParameter> getParameterType() {
        return null;
    }

    @Override
    public Object buildAll(String modelName, ModelParameter parameter, Project project) {
        final LaunchMode mode = LaunchMode.valueOf(parameter.getMode());
        final ApplicationDeploymentClasspathBuilder classpathBuilder = new ApplicationDeploymentClasspathBuilder(project,
            mode);
        final Configuration classpathConfig = classpathBuilder.getRuntimeConfiguration();
        final Configuration deploymentConfig = classpathBuilder.getDeploymentConfiguration();
        return null;
    }
}
