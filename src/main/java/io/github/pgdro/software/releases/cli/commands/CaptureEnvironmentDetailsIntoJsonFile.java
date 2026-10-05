package io.github.pgdro.software.releases.cli.commands;

import io.github.pgdro.software.releases.environment.EnvironmentCapturingAssembleClass;
import io.github.pgdro.tools.core.CommonInteractiveClass;
import io.github.pgdro.tools.core.FileContentClass;
import io.github.pgdro.tools.core.LogExposureClass;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;

/**
 * Captures execution environment details into Log file
 */
@CommandLine.Command(name = "CaptureEnvironmentDetailsIntoJsonFile",
                     description = "Captures execution environment details into Log file")
public class CaptureEnvironmentDetailsIntoJsonFile implements Runnable {
    /**
     * adds the options defined in
     * CommonInteractiveClass.OutFileNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.OutFileNameOptionMixinClass optionOut
            = new CommonInteractiveClass.OutFileNameOptionMixinClass();

    /**
     * Main logic of the command
     */
    @Override
    public void run() {
        final String strEnvDetails = EnvironmentCapturingAssembleClass.packageCurrentEnvironmentDetailsIntoJson();
        final String strOutFileName = optionOut.getOutFileName();
        final String strFeedback = String.format(
                "Environment details are %s and will intend to write it to %s file",
                strEnvDetails,
                strOutFileName);
        LogExposureClass.LOGGER.info(strFeedback);
        FileContentClass.ContentWritingSubClass.writeRawTextToFile(strOutFileName, strEnvDetails);
    }

    /**
     * Private constructor to prevent instantiation
     */
    protected CaptureEnvironmentDetailsIntoJsonFile() {
        // intentionally left blank
    }

}
