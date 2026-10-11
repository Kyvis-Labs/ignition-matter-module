package com.matter.ignition.gateway;

import java.util.List;
import java.util.Map;

/**
 * Matter struct and unit metadata, GENERATED from the matter.js model
 * (@matter/model standard/elements/*.element.js). Do not hand-edit.
 *
 * <p>Matter serialises struct fields by numeric index, so a struct attribute
 * arrives as {@code {"0":0,"1":845061000}}. Without these tables the provider
 * can only JSON-stringify it; with them each field becomes its own tag under a
 * readable name.
 *
 * <p>Field types also carry units ({@code voltage-mV}, {@code energy-mWh},
 * {@code percent100ths}), which is what drives unit conversion -- so a new
 * cluster gets correct engineering units without touching any code.
 */
final class MatterStructs {

    private MatterStructs() {}

    /** "cluster/attr" -> declared type, "list:Element" for lists. */
    static final Map<String, String> ATTR_TYPES = Map.ofEntries(
            Map.entry("1030/4", "HoldTimeLimitsStruct"),
            Map.entry("144/10", "power-mVA"),
            Map.entry("144/11", "voltage-mV"),
            Map.entry("144/12", "amperage-mA"),
            Map.entry("144/13", "power-mW"),
            Map.entry("144/15", "list:HarmonicMeasurementStruct"),
            Map.entry("144/16", "list:HarmonicMeasurementStruct"),
            Map.entry("144/18", "amperage-mA"),
            Map.entry("144/2", "list:MeasurementAccuracyStruct"),
            Map.entry("144/3", "list:MeasurementRangeStruct"),
            Map.entry("144/4", "voltage-mV"),
            Map.entry("144/5", "amperage-mA"),
            Map.entry("144/6", "amperage-mA"),
            Map.entry("144/7", "amperage-mA"),
            Map.entry("144/8", "power-mW"),
            Map.entry("144/9", "power-mVAR"),
            Map.entry("145/0", "MeasurementAccuracyStruct"),
            Map.entry("145/1", "EnergyMeasurementStruct"),
            Map.entry("145/2", "EnergyMeasurementStruct"),
            Map.entry("145/3", "EnergyMeasurementStruct"),
            Map.entry("145/4", "EnergyMeasurementStruct"),
            Map.entry("145/5", "CumulativeEnergyResetStruct"),
            Map.entry("258/11", "percent100ths"),
            Map.entry("258/12", "percent100ths"),
            Map.entry("258/14", "percent100ths"),
            Map.entry("258/15", "percent100ths"),
            Map.entry("513/72", "list:PresetTypeStruct"),
            Map.entry("513/73", "list:ScheduleTypeStruct"),
            Map.entry("513/80", "list:PresetStruct"),
            Map.entry("513/81", "list:ScheduleStruct"),
            Map.entry("513/84", "list:ThermostatSuggestionStruct"),
            Map.entry("513/85", "ThermostatSuggestionStruct"),
            Map.entry("80/2", "list:ModeOptionStruct")
    );

    /** Struct name -> ordered fields, each "index|name|type". */
    static final Map<String, List<String>> STRUCT_FIELDS = Map.ofEntries(
            Map.entry("CumulativeEnergyResetStruct", List.of(
                    "0|ImportedResetTimestamp|epoch-s",
                    "1|ExportedResetTimestamp|epoch-s",
                    "2|ImportedResetSystime|systime-ms",
                    "3|ExportedResetSystime|systime-ms")),
            Map.entry("EnergyMeasurementStruct", List.of(
                    "0|Energy|energy-mWh",
                    "1|StartTimestamp|epoch-s",
                    "2|EndTimestamp|epoch-s",
                    "3|StartSystime|systime-ms",
                    "4|EndSystime|systime-ms",
                    "5|ApparentEnergy|energy-mVAh",
                    "6|ReactiveEnergy|energy-mVARh")),
            Map.entry("HarmonicMeasurementStruct", List.of(
                    "0|Order|uint8",
                    "1|Measurement|int64")),
            Map.entry("HoldTimeLimitsStruct", List.of(
                    "0|HoldTimeMin|uint16",
                    "1|HoldTimeMax|uint16",
                    "2|HoldTimeDefault|uint16")),
            Map.entry("MeasurementAccuracyRangeStruct", List.of(
                    "0|RangeMin|int64",
                    "1|RangeMax|int64",
                    "2|PercentMax|percent100ths",
                    "3|PercentMin|percent100ths",
                    "4|PercentTypical|percent100ths",
                    "5|FixedMax|uint64",
                    "6|FixedMin|uint64",
                    "7|FixedTypical|uint64")),
            Map.entry("MeasurementAccuracyStruct", List.of(
                    "0|MeasurementType|MeasurementTypeEnum",
                    "1|Measured|bool",
                    "2|MinMeasuredValue|int64",
                    "3|MaxMeasuredValue|int64",
                    "4|AccuracyRanges|list:MeasurementAccuracyRangeStruct")),
            Map.entry("MeasurementRangeStruct", List.of(
                    "0|MeasurementType|MeasurementTypeEnum",
                    "1|Min|int64",
                    "2|Max|int64",
                    "3|StartTimestamp|epoch-s",
                    "4|EndTimestamp|epoch-s",
                    "5|MinTimestamp|epoch-s",
                    "6|MaxTimestamp|epoch-s",
                    "7|StartSystime|systime-ms",
                    "8|EndSystime|systime-ms",
                    "9|MinSystime|systime-ms",
                    "10|MaxSystime|systime-ms")),
            Map.entry("ModeOptionStruct", List.of(
                    "0|Label|string",
                    "1|Mode|uint8",
                    "2|SemanticTags|list:SemanticTagStruct")),
            Map.entry("PresetStruct", List.of(
                    "0|PresetHandle|octstr",
                    "1|PresetScenario|PresetScenarioEnum",
                    "2|Name|string",
                    "5|BuiltIn|bool")),
            Map.entry("PresetTypeStruct", List.of(
                    "0|PresetScenario|PresetScenarioEnum",
                    "1|NumberOfPresets|uint8",
                    "2|PresetTypeFeatures|PresetTypeFeaturesBitmap")),
            Map.entry("ScheduleStruct", List.of(
                    "0|ScheduleHandle|octstr",
                    "1|SystemMode|SystemModeEnum",
                    "2|Name|string",
                    "3|PresetHandle|octstr",
                    "4|Transitions|list:ScheduleTransitionStruct",
                    "5|BuiltIn|bool")),
            Map.entry("ScheduleTransitionStruct", List.of(
                    "0|DayOfWeek|ScheduleDayOfWeekBitmap",
                    "1|TransitionTime|uint16",
                    "2|PresetHandle|octstr",
                    "3|SystemMode|SystemModeEnum",
                    "4|CoolingSetpoint|temperature",
                    "5|HeatingSetpoint|temperature")),
            Map.entry("ScheduleTypeStruct", List.of(
                    "0|SystemMode|SystemModeEnum",
                    "1|NumberOfSchedules|uint8",
                    "2|ScheduleTypeFeatures|ScheduleTypeFeaturesBitmap")),
            Map.entry("SemanticTagStruct", List.of(
                    "0|MfgCode|vendor-id",
                    "1|Value|uint16")),
            Map.entry("ThermostatSuggestionStruct", List.of(
                    "0|UniqueId|uint8",
                    "1|PresetHandle|octstr",
                    "2|EffectiveTime|epoch-s",
                    "3|ExpirationTime|epoch-s"))
    );
}
