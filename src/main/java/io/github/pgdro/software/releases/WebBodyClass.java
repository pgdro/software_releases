/** Copyright 2026 Daniel-Gheorghe Popiniuc */
package io.github.pgdro.software.releases;

import io.github.pgdro.software.releases.environment.EnvironmentCapturingAssembleClass;
import io.github.pgdro.software.releases.environment.EnvironmentSoftwareReleasesClass;
import io.github.pgdro.tools.core.BasicStructuresClass;
import io.github.pgdro.tools.core.ConfigurationClass;
import io.github.pgdro.tools.core.LogExposureClass;
import io.github.pgdro.tools.core.file.FileStatisticsClass;
import io.github.pgdro.tools.core.time.TimingClass;
import io.github.pgdro.tools.dynamic.JsonOperationsClass;
import io.github.pgdro.tools.dynamic.database.DatabaseSpecificSqLiteClass;
import io.github.pgdro.tools.dynamic.web.html.TableClass;

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SequencedMap;
import java.util.function.Supplier;
import tools.jackson.databind.JsonNode;

/**
 * Web body content
 */
public final class WebBodyClass {
    /** variable for dynamic body */
    private final Map<String, Supplier<String>> mapPageBody = Map.ofEntries(
            Map.entry("locationSun",                       this::bodyLocationSunDetails),
            Map.entry(ConfigurationClass.STR_ENV_DTLS,     this::bodyEnvironmentDetails),
            Map.entry(ConfigurationClass.STR_SOFTWARE_RLS, this::bodySoftwareReleases),
            Map.entry(ConfigurationClass.STR_FILE_HASHING, this::bodyFileHashing),
            Map.entry(ConfigurationClass.STR_TS,           this::bodyTableStatistics)
    );

    /**
     * Constructor
     */
    public WebBodyClass() {
        super();
    }

    /**
     * Dispatcher for dynamic body content
     * @param requestedPage given Page ID
     * @return String with body content
     */
    public String dispatchPageBody(final String requestedPage) {
        return mapPageBody.getOrDefault(requestedPage, this::bodySoftwareReleases).get();
    }

    /**
     * Outputs file statistics into an HTML table
     * @return String
     */
    private String bodyEnvironmentDetails() {
        final Properties objFeatures = new Properties();
        objFeatures.put(ConfigurationClass.STR_NEW_TAB, ConfigurationClass.STR_CATEGORY);
        final List<Properties> envDetails = EnvironmentCapturingAssembleClass
                .packageCurrentEnvironmentDetailsIntoListOfProperties();
        final List<String> desiredOrder = List.of(ConfigurationClass.STR_CATEGORY, "Element", "Value");
        final List<SequencedMap<Object, Object>> orderedList = envDetails.stream()
                .map(prop -> BasicStructuresClass.ListAndMapSubClass.sortProperties(prop, desiredOrder))
                .toList();
        return "<div><a href=\"?page=downloadEnvironmentDetailsAsJSONfile\">Dowload to a JSON file</a></div>"
                + TableClass.getListOfSequencedMapIntoHtmlTable(orderedList, objFeatures);
    }

    /**
     * Outputs file statistics into an HTML table
     * @return String
     */
    private String bodyFileHashing() {
        final String[] inAlgorithms = {"SHA-256"};
        FileStatisticsClass.setChecksumAlgorithms(inAlgorithms);
        final String[] folderNames = WebClass.getFolderNames();
        final List<Properties> foldersStatistics = new ArrayList<>();
        for (final String crtFolderName: folderNames) {
            final String strFeedback = String.format("Will process folder %s", crtFolderName);
            LogExposureClass.LOGGER.info(strFeedback);
            final ZonedDateTime refTimeStamp = TimingClass.getCurrentZonedDateTime();
            final List<Properties> crtFileStatistics = FileStatisticsClass
                    .getFileStatisticsIntoListOfProperties(crtFolderName, refTimeStamp);
            foldersStatistics.addAll(crtFileStatistics);
        }
        final List<String> desiredOrder = List.of(
                "Folder",
                "File",
                "Size [bytes]",
                ConfigurationClass.STR_SIZE,
                "SHA-256",
                "Last Modified Timestamp",
                "Last Modified Aging");
        final List<SequencedMap<Object, Object>> orderedList = foldersStatistics.stream()
                .map(prop -> BasicStructuresClass.ListAndMapSubClass.sortProperties(prop, desiredOrder))
                .toList();
        return TableClass.getListOfSequencedMapIntoHtmlTable(orderedList, new Properties());
    }

    /**
     * Sun details for all Locations with JSON
     * @return String with UI of Locations as tabs
     */
    private String bodyLocationSunDetails() {
        final JsonNode jsonArray = JsonOperationsClass.getJsonFileNodes(Path.of(WebClass.getJsonLocationsFile()));
        final StringBuilder sbReturn = new StringBuilder(1000);
        sbReturn.append("<div id=\"tabStandard\" class=\"tabber\">");
        jsonArray.forEach(crtLocation -> {
            SunClass.setZoneId(crtLocation.get("TimeZoneName").toString().replace("\"", ""));
            SunClass.setLatitude(Double.parseDouble(crtLocation.get("Latitude").toString()));
            SunClass.setLongitude(Double.parseDouble(crtLocation.get("Longitude").toString()));
            final String strTabTitle
                    = crtLocation.get("LocationPlaceDivisionCountry").toString().replace("\"", "");
            final Map<String, String> mapSunRiseAndSet = SunClass.getSunRiseAndSet(strTabTitle);
            final String strFeedback = String.format("LocationPlaceDivisionCountry is %s and has details as %s",
                    strTabTitle,
                    mapSunRiseAndSet.toString());
            LogExposureClass.LOGGER.debug(strFeedback);
            final SequencedMap<String, String> sortedSun = BasicStructuresClass
                    .ListAndMapSubClass
                    .sortMap(mapSunRiseAndSet, true, true);
            sbReturn.append("<div class=\"tabbertab\" title=\"")
                    .append(strTabTitle)
                    .append("\"><table style=\"float:left;\">");
            sortedSun.forEach((crtKey, crtValue) -> {
                if (!crtValue.equals(strTabTitle)) {
                    sbReturn.append("<tr><th style=\"text-align:left;\">")
                           .append(crtKey)
                           .append("</th><td>")
                           .append(crtValue)
                           .append("</td></tr>");
                }
            });
            sbReturn.append("</table>"
                    + "<div style=\"float:none;clear:both;height:5px;\">&nbsp;</div>"
                    + "</div><!-- %s -->");
        });
        sbReturn.append("</div><!-- tabStandard -->");
        return sbReturn.toString();
    }

    /**
     * expose Software Release details from internal DB
     * @return String software releases details
     */
    private String bodySoftwareReleases() {
        String strReturn = "No software releases found.";
        final List<Properties> softwareReleases = EnvironmentSoftwareReleasesClass.consolidateSoftwareReleases();
        if (!softwareReleases.isEmpty()) {
            final List<String> desiredOrder = List.of("Organization", "Product", "Version", "Date", "Files");
            final List<SequencedMap<Object, Object>> orderedList = softwareReleases.stream()
                    .map(prop -> BasicStructuresClass.ListAndMapSubClass.sortProperties(prop, desiredOrder))
                    .toList();
            final Properties objFeatures = new Properties();
            objFeatures.put(ConfigurationClass.STR_NEW_TAB, "Profile");
            objFeatures.put(ConfigurationClass.STR_COUNTER, "#");
            strReturn = TableClass.getListOfSequencedMapIntoHtmlTable(orderedList, objFeatures);
        }
        return strReturn;
    }

    /**
     * Exposing Raw Value from String
     * @param inputString relevant String to convert
     * @return converted String
     */
    private String bodyTableStatistics() {
        final List<SequencedMap<Object, Object>> inList
        =  DatabaseSpecificSqLiteClass.SqLiteStatisticsSubClass.getTableStatisticsIntoListForHtmlTable();
        return TableClass.getListOfSequencedMapIntoHtmlTable(inList, new Properties());
    }

}