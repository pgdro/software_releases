/**
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
package io.github.pgdro.software.releases.cli;

import io.github.pgdro.software.releases.cli.commands.CaptureEnvironmentDetailsIntoJsonFile;
import io.github.pgdro.software.releases.cli.commands.GetInformationFromDatabase;
import io.github.pgdro.software.releases.cli.commands.GetRemoteMavenPackageDetails;
import io.github.pgdro.software.releases.cli.commands.WebUserInterface;
import io.github.pgdro.tools.core.CommonInteractiveClass;
import picocli.CommandLine;

/**
 * Main Command Line
 */
@CommandLine.Command(
        name = "top",
        subcommands = {
                CaptureEnvironmentDetailsIntoJsonFile.class,
                GetInformationFromDatabase.class,
                GetRemoteMavenPackageDetails.class,
                WebUserInterface.class
        }
)
public final class ApplicationSoftwareReleases {

    /**
     * Constructor
     *
     * @param args command-line arguments
     */
    /* default */ static void main(final String... args) {
        final String logFullFilePath = System.getProperty("java.io.tmpdir")
                + "LogsSoftwareReleases/Software-Releases";
        CommonInteractiveClass.startMeUpWithParameters(logFullFilePath, "/software-releases-pom.xml");
        final int intWebExitCode = new CommandLine(new ApplicationSoftwareReleases()).execute(args);
        CommonInteractiveClass.shutMeDownWithParameters(intWebExitCode, args[0]);
    }

    /** Constructor */
    private ApplicationSoftwareReleases() {
        // intentionally left blank
    }

}
