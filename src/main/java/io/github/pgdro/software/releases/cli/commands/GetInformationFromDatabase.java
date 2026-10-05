package io.github.pgdro.software.releases.cli.commands;

import io.github.pgdro.tools.core.CommonInteractiveClass;
import io.github.pgdro.tools.core.ConfigurationClass;
import io.github.pgdro.tools.core.LogExposureClass;
import io.github.pgdro.tools.dynamic.JsonOperationsClass;
import io.github.pgdro.tools.dynamic.database.DatabaseSpecificMySqlClass;
import io.github.pgdro.tools.dynamic.database.DatabaseSpecificSnowflakeClass;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.jspecify.annotations.NonNull;
import picocli.CommandLine;
import tools.jackson.databind.JsonNode;

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "GetInformationFromDatabase",
                     description = "Gets information from Database into Log file")
public class GetInformationFromDatabase implements Runnable {

    /**
     * Known Database Types
     */
    /* default */ static final List<String> LST_DB_TYPES = Arrays.asList(
        "MySQL",
        "Snowflake"
    );

    /**
     * Known Information Types
     */
    /* default */ static final List<String> LST_INFO_TYPES = Arrays.asList(
        "Columns",
        "Databases",
        "Schemas",
        "TablesAndViews",
        "Views",
        "ViewsLight"
    );

    /**
     * String for Database Type
     */
    @CommandLine.Option(
        names = { "-dbTp", "--databaseType" },
        description = "Type of Database",
        arity = CommonInteractiveClass.ARITY_ONLY_ONE,
        required = true,
        completionCandidates = DatabaseTypes.class)
    private String strDbType;

    /**
     * String for Information Type
     */
    @CommandLine.Option(
        names = { "-infTp", "--informationType" },
        description = "Type of Information",
        arity = CommonInteractiveClass.ARITY_ONE_OR_MORE,
        required = true,
        completionCandidates = InfoTypes.class)
    private String strInfoType;

    /**
     * Listing available options
     */
    /* default */ static class DatabaseTypes implements Iterable<String> {
        @Override
        public Iterator<String> iterator() {
            return LST_DB_TYPES.iterator();
        }
    }

    /**
     * Listing available options
     */
    /* default */ static class InfoTypes implements Iterable<String> {
        @Override
        public Iterator<String> iterator() {
            return LST_INFO_TYPES.iterator();
        }
    }

    private static @NonNull Properties getEnvironmentVariableValueForMySql() {
        final Properties properties = new Properties();
        final String envValue = ConfigurationClass.getEnvironmentVariableValue("MYSQL");
        final JsonNode ndMySql = JsonOperationsClass.getJsonFileNodes(envValue);
        properties.put("ServerName", JsonOperationsClass.getJsonValue(ndMySql, "/ServerName"));
        properties.put("Port", JsonOperationsClass.getJsonValue(ndMySql, "/Port"));
        properties.put("Username", JsonOperationsClass.getJsonValue(ndMySql, "/Username"));
        properties.put("Password", JsonOperationsClass.getJsonValue(ndMySql, "/Password"));
        properties.put("ServerTimezone", JsonOperationsClass.getJsonValue(ndMySql, "/ServerTimezone"));
        return properties;
    }

    /**
     * Action logic
     *
     * @param strDatabaseType type of Database (predefined values)
     */
    private static void performAction(final @NonNull String strDatabaseType, final String strLclInfoType) {
        Properties properties = new Properties();
        switch (strDatabaseType) {
            case "MySQL":
                properties = getEnvironmentVariableValueForMySql();
                DatabaseSpecificMySqlClass.performMySqlPreDefinedAction(strLclInfoType, properties);
                break;
            case "Snowflake":
                DatabaseSpecificSnowflakeClass.performSnowflakePreDefinedAction(strLclInfoType, properties);
                break;
            default:
                final String strFeedback = String.format(
                        "Unknown %s argument received in %s, do not know what to do with it, therefore will quit, bye!",
                        strDatabaseType,
                        StackWalker.getInstance().walk(frames
                                -> frames.findFirst().map(frame
                                -> frame.getClassName() + "." + frame.getMethodName())
                                .orElse(LogExposureClass.STR_I18N_UNKN)));
                LogExposureClass.LOGGER.error(strFeedback);
                break;
        }
    }

    /**
     * Main logic for the command
     */
    @Override
    public void run() {
        if (!LST_DB_TYPES.contains(strDbType)) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "Invalid value for --databaseType: " + strDbType + ". Valid values are: " + LST_DB_TYPES
            );
        }
        if (!LST_INFO_TYPES.contains(strInfoType)) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "Invalid value for --informationType: " + strInfoType + ". Valid values are: " + LST_INFO_TYPES
            );
        }
        performAction(strDbType, strInfoType);
    }

    /**
     * Constructor
     */
    protected GetInformationFromDatabase() {
        // intentionally left blank
    }

}
