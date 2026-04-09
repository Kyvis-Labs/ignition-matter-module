package com.matter.ignition.gateway;

import java.util.List;
import java.util.Map;

/**
 * Static lookup tables for human-readable Matter device type, cluster, and attribute names.
 */
class MatterNames {

    private MatterNames() {}

    private static final Map<Integer, String> DEVICE_TYPES = Map.ofEntries(
            Map.entry(10, "DoorLock"),
            Map.entry(11, "DoorLockController"),
            Map.entry(14, "AggregatorEndpoint"),
            Map.entry(15, "GenericSwitch"),
            Map.entry(17, "PowerSource"),
            Map.entry(19, "ContactSensor"),
            Map.entry(21, "ContactSensor"),
            Map.entry(22, "RootNode"),
            Map.entry(256, "OnOffLight"),
            Map.entry(257, "DimmableLight"),
            Map.entry(259, "OnOffLightSwitch"),
            Map.entry(261, "DimmerSwitch"),
            Map.entry(262, "ColorDimmerSwitch"),
            Map.entry(263, "MotionSensor"),
            Map.entry(266, "OccupancySensor"),
            Map.entry(268, "ColorTemperatureLight"),
            Map.entry(769, "Thermostat"),
            Map.entry(770, "Fan"),
            Map.entry(771, "AirQualitySensor"),
            Map.entry(772, "AirPurifier"),
            Map.entry(774, "TemperatureSensor"),
            Map.entry(775, "PressureSensor"),
            Map.entry(776, "FlowSensor"),
            Map.entry(777, "HumiditySensor"),
            Map.entry(2112, "SmokeCOAlarm")
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

}
