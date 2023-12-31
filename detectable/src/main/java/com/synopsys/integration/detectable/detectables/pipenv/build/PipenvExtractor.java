package com.synopsys.integration.detectable.detectables.pipenv.build;

import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.detectable.ExecutableUtils;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectable.executable.DetectableExecutableRunner;
import com.synopsys.integration.detectable.detectables.pipenv.build.model.PipFreeze;
import com.synopsys.integration.detectable.detectables.pipenv.build.model.PipenvGraph;
import com.synopsys.integration.detectable.detectables.pipenv.build.parser.PipEnvJsonGraphParser;
import com.synopsys.integration.detectable.detectables.pipenv.build.parser.PipenvFreezeParser;
import com.synopsys.integration.detectable.detectables.pipenv.build.parser.PipenvTransformer;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.executable.ExecutableOutput;
import com.synopsys.integration.executable.ExecutableRunnerException;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class PipenvExtractor {
    private final DetectableExecutableRunner executableRunner;
    private final PipenvTransformer pipenvTransformer;
    private final PipenvFreezeParser pipenvFreezeParser;
    private final PipEnvJsonGraphParser pipEnvJsonGraphParser;

    public PipenvExtractor(
        DetectableExecutableRunner executableRunner,
        PipenvTransformer pipenvTransformer,
        PipenvFreezeParser pipenvFreezeParser,
        PipEnvJsonGraphParser pipEnvJsonGraphParser
    ) {
        this.executableRunner = executableRunner;
        this.pipenvTransformer = pipenvTransformer;
        this.pipenvFreezeParser = pipenvFreezeParser;
        this.pipEnvJsonGraphParser = pipEnvJsonGraphParser;
    }

    public Extraction extract(
        File directory,
        ExecutableTarget pythonExe,
        ExecutableTarget pipenvExe,
        File setupFile,
        String providedProjectName,
        String providedProjectVersionName,
        boolean includeOnlyProjectTree
    ) {
        Extraction extraction;

        try {
            String projectName = resolveProjectName(directory, pythonExe, setupFile, providedProjectName);
            String projectVersionName = resolveProjectVersionName(directory, pythonExe, setupFile, providedProjectVersionName);

            ExecutableOutput pipFreezeOutput = executableRunner.execute(ExecutableUtils.createFromTarget(directory, pipenvExe, Arrays.asList("run", "pip", "freeze")));
            ExecutableOutput graphOutput = executableRunner.execute(ExecutableUtils.createFromTarget(directory, pipenvExe, Arrays.asList("graph", "--bare", "--json-tree")));

            PipFreeze pipFreeze = pipenvFreezeParser.parse(pipFreezeOutput.getStandardOutputAsList());
            PipenvGraph pipenvGraph = pipEnvJsonGraphParser.parse(graphOutput.getStandardOutput());
            CodeLocation codeLocation = pipenvTransformer.transform(projectName, projectVersionName, pipFreeze, pipenvGraph, includeOnlyProjectTree);

            return new Extraction.Builder()
                .projectName(projectName)
                .projectVersion(projectVersionName)
                .success(codeLocation)
                .build();
        } catch (Exception e) {
            extraction = new Extraction.Builder().exception(e).build();
        }

        return extraction;
    }

    private String resolveProjectName(File directory, ExecutableTarget pythonExe, File setupFile, String providedProjectName) throws ExecutableRunnerException {
        String projectName = providedProjectName;

        if (StringUtils.isBlank(projectName) && setupFile != null && setupFile.exists()) {
            List<String> arguments = Arrays.asList(setupFile.getAbsolutePath(), "--name");
            List<String> output = executableRunner.execute(ExecutableUtils.createFromTarget(directory, pythonExe, arguments)).getStandardOutputAsList();
            projectName = output.get(output.size() - 1).replace('_', '-').trim();
        }

        return projectName;
    }

    private String resolveProjectVersionName(File directory, ExecutableTarget pythonExe, File setupFile, String providedProjectVersionName) throws ExecutableRunnerException {
        String projectVersionName = providedProjectVersionName;

        if (StringUtils.isBlank(projectVersionName) && setupFile != null && setupFile.exists()) {
            List<String> arguments = Arrays.asList(setupFile.getAbsolutePath(), "--version");
            List<String> output = executableRunner.execute(ExecutableUtils.createFromTarget(directory, pythonExe, arguments)).getStandardOutputAsList();
            projectVersionName = output.get(output.size() - 1).trim();
        }

        return projectVersionName;
    }

}
