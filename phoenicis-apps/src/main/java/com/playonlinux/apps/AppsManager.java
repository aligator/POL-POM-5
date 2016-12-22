package com.playonlinux.apps;

import com.playonlinux.apps.dto.ApplicationDTO;
import com.playonlinux.apps.dto.CategoryDTO;
import com.playonlinux.apps.dto.ScriptDTO;

import java.util.List;

public interface AppsManager {
    List<CategoryDTO> fetchInstallableApplications();

    default ScriptDTO getScript(List<String> path) {
        final ApplicationDTO applicationDTO = getApplication(path);

        if(applicationDTO != null) {
            for (ScriptDTO scriptDTO : applicationDTO.getScripts()) {
                if (path.get(2).equals(scriptDTO.getName())) {
                    return scriptDTO;
                }
            }
        }

        return null;
    }

    default ApplicationDTO getApplication(List<String> path) {
        for (CategoryDTO categoryDTO : fetchInstallableApplications()) {
            if (path.get(0).equals(categoryDTO.getName())) {
                for (ApplicationDTO applicationDTO : categoryDTO.getApplications()) {
                    if (path.get(1).equals(applicationDTO.getName())) {
                        return applicationDTO;
                    }
                }
            }
        }

        return null;
    }
}