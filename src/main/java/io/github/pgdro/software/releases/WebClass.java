/** Copyright 2026 Daniel-Gheorghe Popiniuc */
package io.github.pgdro.software.releases;

import gg.jte.TemplateEngine;
import gg.jte.output.Utf8ByteOutput;
import io.github.pgdro.software.releases.environment.EnvironmentCapturingAssembleClass;
import io.github.pgdro.tools.core.ConfigurationClass;
import io.github.pgdro.tools.core.LogExposureClass;
import io.github.pgdro.tools.core.ProjectClass;
import io.github.pgdro.tools.core.time.TimingClass;
import io.github.pgdro.tools.dynamic.database.DatabaseSpecificSqLiteClass;
import io.github.pgdro.tools.dynamic.web.html.FileInfoClass;
import io.github.pgdro.tools.dynamic.web.html.HtmlClass;
import io.github.pgdro.tools.dynamic.web.undertow.JavaTemplateRenderingClass;
import io.github.pgdro.tools.dynamic.web.undertow.UndertowServerClass;
import io.github.pgdro.tools.dynamic.web.undertow.UndertowParametersClass;
import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.Supplier;
import org.jspecify.annotations.NonNull;

/**
 * Web interface class
 */
public final class WebClass {
    /** Constant for "Software Releases" */
    public static final String STR_SOFT_RELEASES = "Software Releases";
    /** Menu */
    private static final SequencedMap<String, Map<String, String>> MAP_MENU = new LinkedHashMap<>();
    /** Variable for Folders relevant for Checksum Exposure */
    private static String[] strFolderNames = new String[0];
    /** Variable for JSON file with Locations */
    private static String jsonLocations;

    static {
        buildMenu();
    }

    private WebClass() {
        // intentionally blank
    }

    /**
     * Getter for jsonLocations
     */
    public static String[] getFolderNames() {
        return Arrays.copyOf(strFolderNames, strFolderNames.length);
    }

    /**
     * Getter for jsonLocations
     */
    public static String getJsonLocationsFile() {
        return jsonLocations;
    }

    /**
     * Menu builder
     */
    private static void buildMenu() {
        MAP_MENU.put(ConfigurationClass.STR_SOFTWARE_RLS, Map.of(
                ConfigurationClass.STR_ICON, "fa-brands fa-dev fa-2x",
                ConfigurationClass.STR_MENU, STR_SOFT_RELEASES,
                ConfigurationClass.STR_TITLE, STR_SOFT_RELEASES));
        MAP_MENU.put(ConfigurationClass.STR_TS, Map.of(
                ConfigurationClass.STR_ICON, "fa-solid fa-square-poll-horizontal fa-2x",
                ConfigurationClass.STR_MENU, "SQLite Table Statistics",
                ConfigurationClass.STR_TITLE, "SQLite Table Statistics"));
        MAP_MENU.put(ConfigurationClass.STR_FILE_HASHING, Map.of(
                ConfigurationClass.STR_ICON, "fa-solid fa-hashtag fa-2x",
                ConfigurationClass.STR_MENU, "Downloads File Hashing",
                ConfigurationClass.STR_TITLE, "Downloads File Hashing"));
        MAP_MENU.put(ConfigurationClass.STR_ENV_DTLS, Map.of(
                ConfigurationClass.STR_ICON, "fa-solid fa-computer fa-2x",
                ConfigurationClass.STR_MENU, "Environment Details",
                ConfigurationClass.STR_TITLE, "Environment Details"));
        MAP_MENU.put("locationSun", Map.of(
                ConfigurationClass.STR_ICON, "fa-solid fa-business-time fa-2x",
                ConfigurationClass.STR_MENU, "Location Time",
                ConfigurationClass.STR_TITLE, "Location Time"));
    }

    /**
     * Handle HTML content
     * @param inExchange input Exchange
     * @param page id for the content
     */
    private static void handleHtmlContent(
            final HttpServerExchange inExchange,
            final String page,
            final String inPortNumber) {
        JavaTemplateRenderingClass.setContentDispositionAndTypeValuesForHtmlContent();
        UndertowServerClass.handleCommonThings(inExchange);
        final TemplateEngine templateEngine = UndertowServerClass.createTemplateEngine();
        final Utf8ByteOutput output = new Utf8ByteOutput();
        JavaTemplateRenderingClass.setOutput(output);
        packAllParameters(page, inPortNumber);
        JavaTemplateRenderingClass.renderTemplate(templateEngine, "standard.jte");
    }

    /**
     * Handle JSON content with Environment details
     * @param inExchange input Exchange
     */
    private static void handleJsonContent(final @NonNull HttpServerExchange inExchange) {
        final String jsonEnvironment = EnvironmentCapturingAssembleClass.packageCurrentEnvironmentDetailsIntoJson();
        final Utf8ByteOutput outputJson = new Utf8ByteOutput();
        outputJson.writeContent(jsonEnvironment);
        JavaTemplateRenderingClass.setOutput(outputJson);
        JavaTemplateRenderingClass.setContentTypeValue("application/json");
        final String strContentDisp = String.format("attachment; filename=\"environment__%s__%s.json\";",
                EnvironmentCapturingAssembleClass.getComputerName("UNNAMED_COMPUTER"),
                TimingClass.getCurrentDateTimeUniveralTimeCoordination().replaceAll("[-:\\s\\.]", "_"));
        JavaTemplateRenderingClass.setContentDisposition(strContentDisp);
        final HeaderMap header = inExchange.getResponseHeaders();
        JavaTemplateRenderingClass.handleResponseHeader(header);
        final Sender response = inExchange.getResponseSender();
        JavaTemplateRenderingClass.handleRawResponseSender(response, jsonEnvironment);
    }

    /**
     * Handle web content
     * @return PathHandler web content
     */
    @NonNull
    public static HttpHandler handleWebContent(final String inPortNumber) {
        return exchange -> {
            final ZonedDateTime startWebTimeStamp = TimingClass.getCurrentZonedDateTime();
            UndertowServerClass.handleQueryParametersAndPage(exchange);
            JavaTemplateRenderingClass.setServerExchange(exchange);
            final String page = UndertowParametersClass.getPageParameter();
            final String jsonPage = "downloadEnvironmentDetailsAsJSONfile";
            if (page.equalsIgnoreCase(jsonPage)) {
                handleJsonContent(exchange);
            } else {
                handleHtmlContent(exchange, page, inPortNumber);
            }
            final ZonedDateTime stopWebTimeStamp = TimingClass.getCurrentZonedDateTime();
            final String strFeedbackEnd = TimingClass.LogSubClass.logDuration(startWebTimeStamp,
                    stopWebTimeStamp,
                    String.format("Page %s processing got completed", page));
            LogExposureClass.LOGGER.info(strFeedbackEnd);
        };
    }

    /**
     * Packing all parameters to Template
     */
    private static void packAllParameters(final String page, final String inPortNumber) {
        final gg.jte.Content cHeaderTags = output
                -> output.writeContent("<link rel=\"stylesheet\" href=\"/web/static/sr.css\">");
        JavaTemplateRenderingClass.packParameter("headerTags", cHeaderTags);
        JavaTemplateRenderingClass.packParameter("page", page);
        JavaTemplateRenderingClass.packParameter("portNumber", inPortNumber);
        String title = page;
        if (!ConfigurationClass.STR_LOCALIZATION.equalsIgnoreCase(page)) {
            final Map<String, String> menuEntry = MAP_MENU.get(page);
            title = menuEntry != null ? menuEntry.getOrDefault(ConfigurationClass.STR_TITLE, page) : page;
        }
        JavaTemplateRenderingClass.packParameter("title", title);
        final gg.jte.Content myMenu = output -> output.writeContent(HtmlClass.buildMenuString(MAP_MENU));
        JavaTemplateRenderingClass.packParameter("menu", myMenu);
        final WebInfoContextSubClass classInfo = new WebInfoContextSubClass();
        final gg.jte.Content infoContext = output -> output.writeContent(classInfo.dispatchPageInfoContext(page));
        JavaTemplateRenderingClass.packParameter("infoContext", infoContext);
        final WebBodyClass classBody  = new WebBodyClass();
        final gg.jte.Content bodyContent = output -> output.writeContent(classBody.dispatchPageBody(page));
        JavaTemplateRenderingClass.packParameter("mainContent", bodyContent);
        JavaTemplateRenderingClass.packCommonParameters();
    }

    /**
     * Setter for strFolderNames
     * @param inFolderNames list of Folders relevant for checksum exposure
     */
    public static void setFolderNamesForChecksumExposure(@NonNull final String... inFolderNames) {
        strFolderNames = Arrays.copyOf(inFolderNames, inFolderNames.length);
    }

    /**
     * Setter for jsonLocations
     * @param inJsonLocations JSON file with Locations as array
     */
    public static void setJsonLocationsFile(@NonNull final String inJsonLocations) {
        jsonLocations = inJsonLocations;
    }

    /**
     * Web content
     */
    private static final class WebInfoContextSubClass {
        /** Constant for empty content */
        private static final String EMPTY_CONTEXT = "<script>"
                + "document.getElementById('infoContextId').style='display:none;';"
                + "</script>";
        /** variable for dynamic info context */
        private final Map<String, Supplier<String>> mapPageInfoContxt = Map.ofEntries(
                Map.entry(ConfigurationClass.STR_ENV_DTLS,     this::infoContextEnvironmentDetails),
                Map.entry(ConfigurationClass.STR_SOFTWARE_RLS, this::infoContextInternalDatabaseStatistics),
                Map.entry(ConfigurationClass.STR_TS,           this::infoContextInternalDatabaseStatistics)
        );

        /**
         * Dispatcher for dynamic info context
         * @param requestedPage given Page ID
         * @return String with body context
         */
        private String dispatchPageInfoContext(final String requestedPage) {
            return mapPageInfoContxt.getOrDefault(requestedPage, this::infoContextEmpty).get();
        }

        /**
         * Build Object Details Information Box
         * @return String
         */
        private String infoContextEmpty() {
            return EMPTY_CONTEXT;
        }

        /**
         * Build Environment Details Information Box
         * @return String
         */
        private String infoContextEnvironmentDetails() {
            return FileInfoClass.gatherFileStatistics(Path.of(ProjectClass.getPomFile()));
        }


        /**
         * Build Object Details Information Box
         * @return String
         */
        private String infoContextInternalDatabaseStatistics() {
            return FileInfoClass.gatherFileStatistics(
                    Path.of(DatabaseSpecificSqLiteClass.getInternalDatabase()));
        }
    }

}
