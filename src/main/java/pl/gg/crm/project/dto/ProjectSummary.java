package pl.gg.crm.project.dto;

import pl.gg.crm.entity.Project;
import pl.gg.crm.entity.ProjectStatus;

public record ProjectSummary(
        Integer id,
        String name,
        ProjectStatus status
) {

    public static ProjectSummary from(Project project) {
        return new ProjectSummary(project.getId(), project.getProjectName(), project.getStatus());
    }
}
