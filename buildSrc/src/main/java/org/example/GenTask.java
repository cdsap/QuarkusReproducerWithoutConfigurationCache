package org.example;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public abstract class GenTask extends DefaultTask {

    private final Property<ApplicationModel> applicationModel = getProject().getObjects().property(ApplicationModel.class);

    @Internal
    public Property<ApplicationModel> getAppModel() {
        return applicationModel;
    }

    private final LaunchMode launchMode;

    @TaskAction
    public void generateCode() {
        ApplicationModel applicationModel = create(getProject(), launchMode);
    }

    @Inject
    public GenTask(LaunchMode launchMode) {
        this.launchMode = launchMode;
    }

    public static ApplicationModel create(Project project, LaunchMode mode) {
        final ModelParameter params = new ModelParameterImpl();
        params.setMode(mode.toString());
        return (ApplicationModel) new GradleApplicationModelBuilder().buildAll(ApplicationModel.class.getName(), params,
            project);
    }
}
