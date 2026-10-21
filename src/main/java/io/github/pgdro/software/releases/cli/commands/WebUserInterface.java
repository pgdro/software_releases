package io.github.pgdro.software.releases.cli.commands;

import io.github.pgdro.software.releases.WebClass;
import io.github.pgdro.tools.core.InteractiveInputClass;
import io.github.pgdro.tools.dynamic.database.DatabaseSpecificSqLiteClass;
import io.github.pgdro.tools.dynamic.web.UndertowClass;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;

/**
 * Supports web interface
 */
@CommandLine.Command(
        name = "WebUserInterface",
        description = "Initiate Web User Interface")
public class WebUserInterface implements Runnable {

    /**
     * adds the options defined in
     * CommonInteractiveClass.LocalDatabaseFileMixinClass to this command
     */
    @Mixin
    private final InteractiveInputClass.LocalDatabaseFileMixinClass optLocalDbFile
            = new InteractiveInputClass.LocalDatabaseFileMixinClass();

    /**
     * adds the options defined in
     * CommonInteractiveClass.PortOptionMixinClass to this command
     */
    @Mixin
    private final InteractiveInputClass.PortOptionMixinClass optPortNumber
            = new InteractiveInputClass.PortOptionMixinClass();

    /**
     * adds the options defined in
     * CommonInteractiveClass.FolderNameOptionMixinClass to this command
     */
    @Mixin
    private final InteractiveInputClass.FolderNameOptionMixinClass optFolderNames
            = new InteractiveInputClass.FolderNameOptionMixinClass();

    /**
     * String for out FileName
     */
    @CommandLine.Option(
            names = {"-jl", "--jsonLocations"},
            description = "JSON file name with array of Locations",
            arity = InteractiveInputClass.ARITY_ONLY_ONE,
            required = true)
    private String strJsonLocations;

    /**
     * Main logic
     */
    @Override
    public void run() {
        final String strPortNumber = String.valueOf(optPortNumber.getPortNumber());
        UndertowClass.setWebPort(strPortNumber);
        DatabaseSpecificSqLiteClass.setInternalDatabase(optLocalDbFile.getLocalDbFile());
        WebClass.setFolderNamesForChecksumExposure(optFolderNames.getFolderNames());
        WebClass.setJsonLocationsFile(strJsonLocations);
        UndertowClass.setRootHandler(WebClass.handleWebContent(strPortNumber));
        UndertowClass.runWebServer();
    }

    /**
     * Constructor
     */
    protected WebUserInterface() {
        // intentionally blank
    }
}
