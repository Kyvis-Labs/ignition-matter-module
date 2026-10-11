package com.matter.ignition.gateway;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Static lookup tables for human-readable Matter device type, cluster, and attribute names.
 */
class MatterNames {

    private MatterNames() {}

    // Device type id -> name. Generated from the matter.js model
    // (@matter/model standard/elements/*.element.js, matter.js 0.17.9) rather than
    // hand-written: the previous table had 770 as Fan, 774 as TemperatureSensor,
    // 775 as PressureSensor, 262 as ColorDimmerSwitch and 19 as ContactSensor, all
    // wrong. Endpoint names appear in every tag path, so a wrong name here is baked
    // into every binding downstream.
    private static final Map<Integer, String> DEVICE_TYPES = Map.ofEntries(
            Map.entry(10, "DoorLock"),
            Map.entry(11, "DoorLockController"),
            Map.entry(14, "Aggregator"),
            Map.entry(15, "GenericSwitch"),
            Map.entry(17, "PowerSource"),
            Map.entry(18, "OtaRequestor"),
            Map.entry(19, "BridgedNode"),
            Map.entry(20, "OtaProvider"),
            Map.entry(21, "ContactSensor"),
            Map.entry(22, "RootNode"),
            Map.entry(23, "SolarPower"),
            Map.entry(24, "BatteryStorage"),
            Map.entry(25, "SecondaryNetworkInterface"),
            Map.entry(34, "Speaker"),
            Map.entry(35, "CastingVideoPlayer"),
            Map.entry(36, "ContentApp"),
            Map.entry(39, "ModeSelect"),
            Map.entry(40, "BasicVideoPlayer"),
            Map.entry(41, "CastingVideoClient"),
            Map.entry(42, "VideoRemoteControl"),
            Map.entry(43, "Fan"),
            Map.entry(44, "AirQualitySensor"),
            Map.entry(45, "AirPurifier"),
            Map.entry(64, "IrrigationSystem"),
            Map.entry(65, "WaterFreezeDetector"),
            Map.entry(66, "WaterValve"),
            Map.entry(67, "WaterLeakDetector"),
            Map.entry(68, "RainSensor"),
            Map.entry(69, "SoilSensor"),
            Map.entry(112, "Refrigerator"),
            Map.entry(113, "TemperatureControlledCabinet"),
            Map.entry(114, "RoomAirConditioner"),
            Map.entry(115, "LaundryWasher"),
            Map.entry(116, "RoboticVacuumCleaner"),
            Map.entry(117, "Dishwasher"),
            Map.entry(118, "SmokeCoAlarm"),
            Map.entry(119, "CookSurface"),
            Map.entry(120, "Cooktop"),
            Map.entry(121, "MicrowaveOven"),
            Map.entry(122, "ExtractorHood"),
            Map.entry(123, "Oven"),
            Map.entry(124, "LaundryDryer"),
            Map.entry(144, "NetworkInfrastructureManager"),
            Map.entry(145, "ThreadBorderRouter"),
            Map.entry(256, "OnOffLight"),
            Map.entry(259, "OnOffLightSwitch"),
            Map.entry(262, "LightSensor"),
            Map.entry(263, "OccupancySensor"),
            Map.entry(266, "OnOffPlugInUnit"),
            Map.entry(267, "DimmablePlugInUnit"),
            Map.entry(304, "JointFabricAdministrator"),
            Map.entry(320, "Intercom"),
            Map.entry(321, "AudioDoorbell"),
            Map.entry(322, "Camera"),
            Map.entry(323, "VideoDoorbell"),
            Map.entry(324, "FloodlightCamera"),
            Map.entry(325, "SnapshotCamera"),
            Map.entry(326, "Chime"),
            Map.entry(327, "CameraController"),
            Map.entry(328, "Doorbell"),
            Map.entry(514, "WindowCovering"),
            Map.entry(515, "WindowCoveringController"),
            Map.entry(560, "Closure"),
            Map.entry(561, "ClosurePanel"),
            Map.entry(574, "ClosureController"),
            Map.entry(769, "Thermostat"),
            Map.entry(770, "TemperatureSensor"),
            Map.entry(771, "Pump"),
            Map.entry(772, "PumpController"),
            Map.entry(773, "PressureSensor"),
            Map.entry(774, "FlowSensor"),
            Map.entry(775, "HumiditySensor"),
            Map.entry(777, "HeatPump"),
            Map.entry(778, "ThermostatController"),
            Map.entry(1292, "EnergyEvse"),
            Map.entry(1293, "DeviceEnergyManagement"),
            Map.entry(1295, "WaterHeater"),
            Map.entry(1296, "ElectricalSensor"),
            Map.entry(1298, "MeterReferencePoint"),
            Map.entry(1299, "ElectricalEnergyTariff"),
            Map.entry(2112, "ControlBridge"),
            Map.entry(2128, "OnOffSensor")
    );

    private static final Map<Integer, String> CLUSTER_NAMES = Map.ofEntries(
            Map.entry(3, "Identify"),
            Map.entry(4, "Groups"),
            Map.entry(5, "Scenes"),
            Map.entry(6, "OnOff"),
            Map.entry(8, "LevelControl"),
            Map.entry(29, "Descriptor"),
            Map.entry(30, "Binding"),
            Map.entry(31, "AccessControl"),
            Map.entry(40, "BasicInformation"),
            Map.entry(41, "OtaSoftwareUpdateProvider"),
            Map.entry(42, "OtaSoftwareUpdateRequestor"),
            Map.entry(43, "LocalizationConfiguration"),
            Map.entry(44, "TimeFormatLocalization"),
            Map.entry(45, "UnitLocalization"),
            Map.entry(46, "PowerSourceConfiguration"),
            Map.entry(47, "PowerSource"),
            // Electrical metering. Absent before, so a metering plug published its
            // power under "Cluster_144" even once the cluster was allowed through.
            Map.entry(144, "ElectricalPowerMeasurement"),
            Map.entry(145, "ElectricalEnergyMeasurement"),
            Map.entry(156, "PowerTopology"),
            Map.entry(48, "GeneralCommissioning"),
            Map.entry(49, "NetworkCommissioning"),
            Map.entry(50, "DiagnosticLogs"),
            Map.entry(51, "GeneralDiagnostics"),
            Map.entry(52, "SoftwareDiagnostics"),
            Map.entry(53, "ThreadNetworkDiagnostics"),
            Map.entry(54, "WiFiNetworkDiagnostics"),
            Map.entry(55, "EthernetNetworkDiagnostics"),
            Map.entry(56, "TimeSynchronization"),
            Map.entry(57, "BridgedDeviceBasicInformation"),
            Map.entry(59, "Switch"),
            Map.entry(60, "AdministratorCommissioning"),
            Map.entry(62, "OperationalCredentials"),
            Map.entry(63, "GroupKeyManagement"),
            Map.entry(64, "FixedLabel"),
            Map.entry(65, "UserLabel"),
            Map.entry(69, "BooleanState"),
            Map.entry(80, "ModeSelect"),
            Map.entry(257, "DoorLock"),
            Map.entry(258, "WindowCovering"),
            Map.entry(512, "PumpConfigurationAndControl"),
            Map.entry(513, "Thermostat"),
            Map.entry(768, "ColorControl"),
            Map.entry(1024, "IlluminanceMeasurement"),
            Map.entry(1026, "TemperatureMeasurement"),
            Map.entry(1027, "PressureMeasurement"),
            Map.entry(1028, "FlowMeasurement"),
            Map.entry(1029, "RelativeHumidityMeasurement"),
            Map.entry(1030, "OccupancySensing")
    );

    private static final Map<String, String> ATTR_NAMES = Map.ofEntries(
            // Electrical metering names, generated from the matter.js model
            // (@matter/model standard/elements/*.element.js) rather than hand-typed.
            Map.entry("144/0", "PowerMode"),
            Map.entry("144/1", "NumberOfMeasurementTypes"),
            Map.entry("144/2", "Accuracy"),
            Map.entry("144/3", "Ranges"),
            Map.entry("144/4", "Voltage"),
            Map.entry("144/5", "ActiveCurrent"),
            Map.entry("144/6", "ReactiveCurrent"),
            Map.entry("144/7", "ApparentCurrent"),
            Map.entry("144/8", "ActivePower"),
            Map.entry("144/9", "ReactivePower"),
            Map.entry("144/10", "ApparentPower"),
            Map.entry("144/11", "RmsVoltage"),
            Map.entry("144/12", "RmsCurrent"),
            Map.entry("144/13", "RmsPower"),
            Map.entry("144/14", "Frequency"),
            Map.entry("144/15", "HarmonicCurrents"),
            Map.entry("144/16", "HarmonicPhases"),
            Map.entry("144/17", "PowerFactor"),
            Map.entry("144/18", "NeutralCurrent"),
            Map.entry("145/0", "Accuracy"),
            Map.entry("145/1", "CumulativeEnergyImported"),
            Map.entry("145/2", "CumulativeEnergyExported"),
            Map.entry("145/3", "PeriodicEnergyImported"),
            Map.entry("145/4", "PeriodicEnergyExported"),
            Map.entry("145/5", "CumulativeEnergyReset"),
            Map.entry("156/0", "AvailableEndpoints"),
            Map.entry("156/1", "ActiveEndpoints"),
            // Descriptor (29)
            Map.entry("29/0", "DeviceTypeList"),
            Map.entry("29/1", "ServerList"),
            Map.entry("29/2", "ClientList"),
            Map.entry("29/3", "PartsList"),
            // BasicInformation (40)
            Map.entry("40/0", "DataModelRevision"),
            Map.entry("40/1", "VendorName"),
            Map.entry("40/2", "VendorID"),
            Map.entry("40/3", "ProductName"),
            Map.entry("40/4", "ProductID"),
            Map.entry("40/5", "NodeLabel"),
            Map.entry("40/6", "Location"),
            Map.entry("40/7", "HardwareVersion"),
            Map.entry("40/8", "HardwareVersionString"),
            Map.entry("40/9", "SoftwareVersion"),
            Map.entry("40/10", "SoftwareVersionString"),
            Map.entry("40/11", "ManufacturingDate"),
            Map.entry("40/12", "PartNumber"),
            Map.entry("40/13", "ProductURL"),
            Map.entry("40/14", "ProductLabel"),
            Map.entry("40/15", "SerialNumber"),
            Map.entry("40/16", "LocalConfigDisabled"),
            Map.entry("40/17", "Reachable"),
            Map.entry("40/18", "UniqueID"),
            Map.entry("40/19", "CapabilityMinima"),
            // BridgedDeviceBasicInformation (57)
            Map.entry("57/1", "VendorName"),
            Map.entry("57/2", "VendorID"),
            Map.entry("57/3", "ProductName"),
            Map.entry("57/5", "NodeLabel"),
            Map.entry("57/7", "HardwareVersion"),
            Map.entry("57/8", "HardwareVersionString"),
            Map.entry("57/9", "SoftwareVersion"),
            Map.entry("57/10", "SoftwareVersionString"),
            Map.entry("57/11", "ManufacturingDate"),
            Map.entry("57/12", "PartNumber"),
            Map.entry("57/13", "ProductURL"),
            Map.entry("57/14", "ProductLabel"),
            Map.entry("57/15", "SerialNumber"),
            Map.entry("57/17", "Reachable"),
            Map.entry("57/18", "UniqueID"),
            // BooleanState (69)
            Map.entry("69/0", "StateValue"),
            // PowerSource (47)
            Map.entry("47/0", "Status"),
            Map.entry("47/1", "Order"),
            Map.entry("47/2", "Description"),
            Map.entry("47/11", "BatVoltage"),
            Map.entry("47/12", "BatPercentRemaining"),
            Map.entry("47/14", "BatChargeLevel"),
            Map.entry("47/15", "BatReplacementNeeded"),
            Map.entry("47/16", "BatReplaceability"),
            Map.entry("47/19", "BatReplacementDescription"),
            Map.entry("47/25", "BatQuantity"),
            // OnOff (6)
            Map.entry("6/0", "OnOff"),
            Map.entry("6/16384", "GlobalSceneControl"),
            Map.entry("6/16385", "OnTime"),
            Map.entry("6/16386", "OffWaitTime"),
            Map.entry("6/16387", "StartUpOnOff"),
            // GeneralDiagnostics (51)
            Map.entry("51/0", "NetworkInterfaces"),
            Map.entry("51/1", "RebootCount"),
            Map.entry("51/2", "UpTime"),
            Map.entry("51/8", "TestEventTriggersEnabled"),
            // AccessControl (31)
            Map.entry("31/0", "ACL"),
            Map.entry("31/1", "Extension"),
            Map.entry("31/3", "SubjectsPerAccessControlEntry"),
            Map.entry("31/4", "TargetsPerAccessControlEntry"),
            Map.entry("31/5", "AccessControlEntriesPerFabric"),
            // OperationalCredentials (62)
            Map.entry("62/0", "NOCs"),
            Map.entry("62/1", "Fabrics"),
            Map.entry("62/2", "SupportedFabrics"),
            Map.entry("62/3", "CommissionedFabrics"),
            Map.entry("62/4", "TrustedRootCertificates"),
            Map.entry("62/5", "CurrentFabricIndex"),
            // Global attributes
            Map.entry("*/65528", "GeneratedCommandList"),
            Map.entry("*/65529", "AcceptedCommandList"),
            Map.entry("*/65530", "EventList"),
            Map.entry("*/65531", "AttributeList"),
            Map.entry("*/65532", "FeatureMap"),
            Map.entry("*/65533", "ClusterRevision"),
            // OccupancySensing (1030)
            Map.entry("1030/0", "Occupancy"),
            Map.entry("1030/1", "OccupancySensorType"),
            Map.entry("1030/2", "OccupancySensorTypeBitmap"),
            // TemperatureMeasurement (1026)
            Map.entry("1026/0", "MeasuredValue"),
            Map.entry("1026/1", "MinMeasuredValue"),
            Map.entry("1026/2", "MaxMeasuredValue"),
            // RelativeHumidityMeasurement (1029)
            Map.entry("1029/0", "MeasuredValue"),
            Map.entry("1029/1", "MinMeasuredValue"),
            Map.entry("1029/2", "MaxMeasuredValue"),
            // IlluminanceMeasurement (1024)
            Map.entry("1024/0", "MeasuredValue"),
            Map.entry("1024/1", "MinMeasuredValue"),
            Map.entry("1024/2", "MaxMeasuredValue"),
            // NetworkCommissioning (49)
            Map.entry("49/0", "MaxNetworks"),
            Map.entry("49/1", "Networks"),
            Map.entry("49/4", "InterfaceEnabled"),
            Map.entry("49/5", "LastNetworkingStatus"),
            Map.entry("49/6", "LastNetworkID"),
            Map.entry("49/7", "LastConnectErrorValue"),
            // GeneralCommissioning (48)
            Map.entry("48/0", "Breadcrumb"),
            Map.entry("48/1", "BasicCommissioningInfo"),
            Map.entry("48/2", "RegulatoryConfig"),
            Map.entry("48/3", "LocationCapability"),
            Map.entry("48/4", "SupportsConcurrentConnection"),
            // GroupKeyManagement (63)
            Map.entry("63/0", "GroupKeyMap"),
            Map.entry("63/1", "GroupTable"),
            Map.entry("63/2", "MaxGroupsPerFabric"),
            Map.entry("63/3", "MaxGroupKeysPerFabric"),
            // Identify (3)
            Map.entry("3/0", "IdentifyTime"),
            Map.entry("3/1", "IdentifyType")
    );

    // Clusters that produce user-facing tags (everything else is filtered out)
    private static final Set<Integer> ALLOWED_CLUSTERS = Set.of(
            6,    // OnOff
            8,    // LevelControl
            59,   // Switch
            69,   // BooleanState
            80,   // ModeSelect
            257,  // DoorLock
            258,  // WindowCovering
            512,  // PumpConfigurationAndControl
            513,  // Thermostat
            768,  // ColorControl
            1024, // IlluminanceMeasurement
            1026, // TemperatureMeasurement
            1027, // PressureMeasurement
            1028, // FlowMeasurement
            1029, // RelativeHumidityMeasurement
            1030, // OccupancySensing
            // Metering, added 2026-10-11. Without these a Shelly Plug US Gen4 exposed
            // only OnOff -- its watts, volts and kWh were filtered out silently.
            144,  // ElectricalPowerMeasurement
            145   // ElectricalEnergyMeasurement
    );

    // Global attribute IDs always skipped from filtered view
    private static final Set<Integer> GLOBAL_ATTR_IDS = Set.of(
            65528, 65529, 65530, 65531, 65532, 65533
    );

    // Endpoints with these device type IDs are infrastructure (skipped from filtered view)
    private static final Set<Integer> INFRASTRUCTURE_DEVICE_TYPES = Set.of(
            22, // RootNode
            14  // AggregatorEndpoint
    );

    // Attributes promoted to device root: "clusterId/attrId" -> tag name
    private static final Map<String, String> PROMOTED_ATTRIBUTES = Map.of(
            "40/17", "Reachable",
            "57/17", "Reachable"
    );

    // PowerSource (47) attributes placed under Battery/ folder
    private static final Map<Integer, String> BATTERY_ATTRIBUTES = Map.of(
            0, "Status",
            2, "Description",
            11, "Voltage",
            12, "PercentRemaining",
            14, "ChargeLevel",
            15, "ReplacementNeeded",
            16, "Replaceability",
            19, "ReplacementDescription"
    );

    // BasicInformation (40) / BridgedDeviceBasicInformation (57) attributes placed under DeviceInfo/
    private static final Map<Integer, String> DEVICE_INFO_ATTRIBUTES = Map.of(
            1, "VendorName",
            3, "ProductName",
            8, "HardwareVersionString",
            10, "SoftwareVersionString",
            11, "ManufacturingDate",
            12, "PartNumber",
            15, "SerialNumber",
            18, "UniqueID"
    );

    static String clusterName(int id) {
        return CLUSTER_NAMES.getOrDefault(id, "Cluster_" + id);
    }

    static String attributeName(int clusterId, int attrId) {
        String name = ATTR_NAMES.get(clusterId + "/" + attrId);
        if (name != null) return name;
        name = ATTR_NAMES.get("*/" + attrId);
        if (name != null) return name;
        return "Attr_" + attrId;
    }

    static String resolveEndpointDeviceType(Object deviceTypeListValue) {
        if (!(deviceTypeListValue instanceof List<?> list)) return null;
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                Object typeObj = map.get("0");
                if (typeObj == null) typeObj = map.get(0);
                if (typeObj instanceof Number num) {
                    int typeId = num.intValue();
                    return DEVICE_TYPES.getOrDefault(typeId, "DeviceType_" + typeId);
                }
            }
        }
        return null;
    }

    static Integer resolveEndpointDeviceTypeId(Object deviceTypeListValue) {
        if (!(deviceTypeListValue instanceof List<?> list)) return null;
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                Object typeObj = map.get("0");
                if (typeObj == null) typeObj = map.get(0);
                if (typeObj instanceof Number num) {
                    return num.intValue();
                }
            }
        }
        return null;
    }

    static boolean isAllowedCluster(int clusterId) {
        return ALLOWED_CLUSTERS.contains(clusterId);
    }

    static boolean isGlobalAttribute(int attrId) {
        return GLOBAL_ATTR_IDS.contains(attrId);
    }

    static boolean isInfrastructureDeviceType(int deviceTypeId) {
        return INFRASTRUCTURE_DEVICE_TYPES.contains(deviceTypeId);
    }

    static String getPromotedName(int clusterId, int attrId) {
        return PROMOTED_ATTRIBUTES.get(clusterId + "/" + attrId);
    }

    static String getBatteryAttributeName(int attrId) {
        return BATTERY_ATTRIBUTES.get(attrId);
    }

    static String getDeviceInfoName(int clusterId, int attrId) {
        if (clusterId != 40 && clusterId != 57) return null;
        return DEVICE_INFO_ATTRIBUTES.get(attrId);
    }

}
