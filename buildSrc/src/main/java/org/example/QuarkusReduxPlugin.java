package org.example;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;


public class QuarkusReduxPlugin implements Plugin<Project> {

    @Override
    public void apply(Project target) {
        target.getPluginManager().apply(JavaPlugin.class);
        createSourceSets(target);
        createConfigurations(target);
        registeringTasks(target);
    }

    private void registeringTasks(Project target) {
        TaskContainer tasks = target.getTasks();


        TaskProvider<GenTask> quarkusGenerateCode = tasks.register(Constants.QUARKUS_GENERATE_CODE_TASK_NAME, GenTask.class, LaunchMode.NORMAL);

        TaskProvider<GenTask> quarkusGenerateCodeDev = tasks.register(Constants.QUARKUS_GENERATE_CODE_DEV_TASK_NAME, GenTask.class, LaunchMode.DEVELOPMENT);
        quarkusGenerateCodeDev.configure(task -> {
            task.dependsOn(quarkusGenerateCode);
        });

        TaskProvider<GenTask> quarkusGenerateCodeTests = tasks.register(Constants.QUARKUS_GENERATE_CODE_TESTS_TASK_NAME, GenTask.class, LaunchMode.TEST);

        target.getPlugins().withType(
            JavaPlugin.class,
            javaPlugin -> {
                SourceSetContainer sourceSets = target.getExtensions().getByType(SourceSetContainer.class);
                SourceSet mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
                SourceSet testSourceSet = sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME);
                mainSourceSet.getJava().srcDirs(quarkusGenerateCode, quarkusGenerateCodeDev);
                testSourceSet.getJava().srcDirs(quarkusGenerateCodeTests);
            });
    }

    private void createSourceSets(Project project) {
        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        sourceSets.create(Constants.INTEGRATION_TEST_SOURCE_SET_NAME);
        sourceSets.create(Constants.NATIVE_TEST_SOURCE_SET_NAME);
        sourceSets.create(Constants.QUARKUS_GENERATED_SOURCES);
        sourceSets.create(Constants.QUARKUS_TEST_GENERATED_SOURCES);
    }

    private void createConfigurations(Project project) {

        final ConfigurationContainer configContainer = project.getConfigurations();

        // Custom configuration to be used for the dependencies of the testNative task
        configContainer.getByName(Constants.NATIVE_TEST_IMPLEMENTATION_CONFIGURATION_NAME)
            .extendsFrom(configContainer.findByName(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME));
        configContainer.getByName(Constants.NATIVE_TEST_RUNTIME_ONLY_CONFIGURATION_NAME)
            .extendsFrom(configContainer.findByName(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME));

        // create a custom configuration to be used for the dependencies of the quarkusIntTest task
        configContainer
            .maybeCreate(Constants.INTEGRATION_TEST_IMPLEMENTATION_CONFIGURATION_NAME)
            .extendsFrom(configContainer.findByName(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME));
        configContainer.maybeCreate(Constants.INTEGRATION_TEST_RUNTIME_ONLY_CONFIGURATION_NAME)
            .extendsFrom(configContainer.findByName(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME));

        ApplicationDeploymentClasspathBuilder.initConfigurations(project);

        // Also initialize the configurations that are specific to a LaunchMode
        for (LaunchMode launchMode : LaunchMode.values()) {
            new ApplicationDeploymentClasspathBuilder(project, launchMode);
        }
    }

}
