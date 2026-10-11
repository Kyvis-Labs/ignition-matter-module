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
            Map.entry(128, "BooleanStateConfiguration"),
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

    // Attribute names. Entries for every cluster the provider can publish are
    // generated from the matter.js model; hand-written entries take precedence
    // where they differ, because they are already bound by UDTs (the only
    // disagreements are casing: UniqueID vs UniqueId, VendorID vs VendorId).
    //
    // Without an entry an attribute publishes as Attr_<id> -- which is how
    // BooleanStateConfiguration first appeared, hiding SensorFault behind
    // "Attr_7".
    private static final Map<String, String> ATTR_NAMES = Map.ofEntries(
            Map.entry("3/0", "IdentifyTime"),
            Map.entry("3/1", "IdentifyType"),
            Map.entry("6/0", "OnOff"),
            Map.entry("6/16384", "GlobalSceneControl"),
            Map.entry("6/16385", "OnTime"),
            Map.entry("6/16386", "OffWaitTime"),
            Map.entry("6/16387", "StartUpOnOff"),
            Map.entry("8/0", "CurrentLevel"),
            Map.entry("8/1", "RemainingTime"),
            Map.entry("8/2", "MinLevel"),
            Map.entry("8/3", "MaxLevel"),
            Map.entry("8/4", "CurrentFrequency"),
            Map.entry("8/5", "MinFrequency"),
            Map.entry("8/6", "MaxFrequency"),
            Map.entry("8/15", "Options"),
            Map.entry("8/16", "OnOffTransitionTime"),
            Map.entry("8/17", "OnLevel"),
            Map.entry("8/18", "OnTransitionTime"),
            Map.entry("8/19", "OffTransitionTime"),
            Map.entry("8/20", "DefaultMoveRate"),
            Map.entry("8/16384", "StartUpCurrentLevel"),
            Map.entry("29/0", "DeviceTypeList"),
            Map.entry("29/1", "ServerList"),
            Map.entry("29/2", "ClientList"),
            Map.entry("29/3", "PartsList"),
            Map.entry("29/4", "TagList"),
            Map.entry("29/5", "EndpointUniqueId"),
            Map.entry("31/0", "ACL"),
            Map.entry("31/1", "Extension"),
            Map.entry("31/3", "SubjectsPerAccessControlEntry"),
            Map.entry("31/4", "TargetsPerAccessControlEntry"),
            Map.entry("31/5", "AccessControlEntriesPerFabric"),
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
            Map.entry("40/20", "ProductAppearance"),
            Map.entry("40/21", "SpecificationVersion"),
            Map.entry("40/22", "MaxPathsPerInvoke"),
            Map.entry("40/24", "ConfigurationVersion"),
            Map.entry("47/0", "Status"),
            Map.entry("47/1", "Order"),
            Map.entry("47/2", "Description"),
            Map.entry("47/3", "WiredAssessedInputVoltage"),
            Map.entry("47/4", "WiredAssessedInputFrequency"),
            Map.entry("47/5", "WiredCurrentType"),
            Map.entry("47/6", "WiredAssessedCurrent"),
            Map.entry("47/7", "WiredNominalVoltage"),
            Map.entry("47/8", "WiredMaximumCurrent"),
            Map.entry("47/9", "WiredPresent"),
            Map.entry("47/10", "ActiveWiredFaults"),
            Map.entry("47/11", "BatVoltage"),
            Map.entry("47/12", "BatPercentRemaining"),
            Map.entry("47/13", "BatTimeRemaining"),
            Map.entry("47/14", "BatChargeLevel"),
            Map.entry("47/15", "BatReplacementNeeded"),
            Map.entry("47/16", "BatReplaceability"),
            Map.entry("47/17", "BatPresent"),
            Map.entry("47/18", "ActiveBatFaults"),
            Map.entry("47/19", "BatReplacementDescription"),
            Map.entry("47/20", "BatCommonDesignation"),
            Map.entry("47/21", "BatAnsiDesignation"),
            Map.entry("47/22", "BatIecDesignation"),
            Map.entry("47/23", "BatApprovedChemistry"),
            Map.entry("47/24", "BatCapacity"),
            Map.entry("47/25", "BatQuantity"),
            Map.entry("47/26", "BatChargeState"),
            Map.entry("47/27", "BatTimeToFullCharge"),
            Map.entry("47/28", "BatFunctionalWhileCharging"),
            Map.entry("47/29", "BatChargingCurrent"),
            Map.entry("47/30", "ActiveBatChargeFaults"),
            Map.entry("47/31", "EndpointList"),
            Map.entry("48/0", "Breadcrumb"),
            Map.entry("48/1", "BasicCommissioningInfo"),
            Map.entry("48/2", "RegulatoryConfig"),
            Map.entry("48/3", "LocationCapability"),
            Map.entry("48/4", "SupportsConcurrentConnection"),
            Map.entry("49/0", "MaxNetworks"),
            Map.entry("49/1", "Networks"),
            Map.entry("49/4", "InterfaceEnabled"),
            Map.entry("49/5", "LastNetworkingStatus"),
            Map.entry("49/6", "LastNetworkID"),
            Map.entry("49/7", "LastConnectErrorValue"),
            Map.entry("51/0", "NetworkInterfaces"),
            Map.entry("51/1", "RebootCount"),
            Map.entry("51/2", "UpTime"),
            Map.entry("51/8", "TestEventTriggersEnabled"),
            Map.entry("57/0", "DataModelRevision"),
            Map.entry("57/1", "VendorName"),
            Map.entry("57/2", "VendorID"),
            Map.entry("57/3", "ProductName"),
            Map.entry("57/4", "ProductId"),
            Map.entry("57/5", "NodeLabel"),
            Map.entry("57/6", "Location"),
            Map.entry("57/7", "HardwareVersion"),
            Map.entry("57/8", "HardwareVersionString"),
            Map.entry("57/9", "SoftwareVersion"),
            Map.entry("57/10", "SoftwareVersionString"),
            Map.entry("57/11", "ManufacturingDate"),
            Map.entry("57/12", "PartNumber"),
            Map.entry("57/13", "ProductURL"),
            Map.entry("57/14", "ProductLabel"),
            Map.entry("57/15", "SerialNumber"),
            Map.entry("57/16", "LocalConfigDisabled"),
            Map.entry("57/17", "Reachable"),
            Map.entry("57/18", "UniqueID"),
            Map.entry("57/19", "CapabilityMinima"),
            Map.entry("57/20", "ProductAppearance"),
            Map.entry("57/21", "SpecificationVersion"),
            Map.entry("57/22", "MaxPathsPerInvoke"),
            Map.entry("57/24", "ConfigurationVersion"),
            Map.entry("59/0", "NumberOfPositions"),
            Map.entry("59/1", "CurrentPosition"),
            Map.entry("59/2", "MultiPressMax"),
            Map.entry("62/0", "NOCs"),
            Map.entry("62/1", "Fabrics"),
            Map.entry("62/2", "SupportedFabrics"),
            Map.entry("62/3", "CommissionedFabrics"),
            Map.entry("62/4", "TrustedRootCertificates"),
            Map.entry("62/5", "CurrentFabricIndex"),
            Map.entry("63/0", "GroupKeyMap"),
            Map.entry("63/1", "GroupTable"),
            Map.entry("63/2", "MaxGroupsPerFabric"),
            Map.entry("63/3", "MaxGroupKeysPerFabric"),
            Map.entry("69/0", "StateValue"),
            Map.entry("80/0", "Description"),
            Map.entry("80/1", "StandardNamespace"),
            Map.entry("80/2", "SupportedModes"),
            Map.entry("80/3", "CurrentMode"),
            Map.entry("80/4", "StartUpMode"),
            Map.entry("80/5", "OnMode"),
            Map.entry("128/0", "CurrentSensitivityLevel"),
            Map.entry("128/1", "SupportedSensitivityLevels"),
            Map.entry("128/2", "DefaultSensitivityLevel"),
            Map.entry("128/3", "AlarmsActive"),
            Map.entry("128/4", "AlarmsSuppressed"),
            Map.entry("128/5", "AlarmsEnabled"),
            Map.entry("128/6", "AlarmsSupported"),
            Map.entry("128/7", "SensorFault"),
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
            Map.entry("257/0", "LockState"),
            Map.entry("257/1", "LockType"),
            Map.entry("257/2", "ActuatorEnabled"),
            Map.entry("257/3", "DoorState"),
            Map.entry("257/4", "DoorOpenEvents"),
            Map.entry("257/5", "DoorClosedEvents"),
            Map.entry("257/6", "OpenPeriod"),
            Map.entry("257/17", "NumberOfTotalUsersSupported"),
            Map.entry("257/18", "NumberOfPinUsersSupported"),
            Map.entry("257/19", "NumberOfRfidUsersSupported"),
            Map.entry("257/20", "NumberOfWeekDaySchedulesSupportedPerUser"),
            Map.entry("257/21", "NumberOfYearDaySchedulesSupportedPerUser"),
            Map.entry("257/22", "NumberOfHolidaySchedulesSupported"),
            Map.entry("257/23", "MaxPinCodeLength"),
            Map.entry("257/24", "MinPinCodeLength"),
            Map.entry("257/25", "MaxRfidCodeLength"),
            Map.entry("257/26", "MinRfidCodeLength"),
            Map.entry("257/27", "CredentialRulesSupport"),
            Map.entry("257/28", "NumberOfCredentialsSupportedPerUser"),
            Map.entry("257/33", "Language"),
            Map.entry("257/34", "LedSettings"),
            Map.entry("257/35", "AutoRelockTime"),
            Map.entry("257/36", "SoundVolume"),
            Map.entry("257/37", "OperatingMode"),
            Map.entry("257/38", "SupportedOperatingModes"),
            Map.entry("257/39", "DefaultConfigurationRegister"),
            Map.entry("257/40", "EnableLocalProgramming"),
            Map.entry("257/41", "EnableOneTouchLocking"),
            Map.entry("257/42", "EnableInsideStatusLed"),
            Map.entry("257/43", "EnablePrivacyModeButton"),
            Map.entry("257/44", "LocalProgrammingFeatures"),
            Map.entry("257/48", "WrongCodeEntryLimit"),
            Map.entry("257/49", "UserCodeTemporaryDisableTime"),
            Map.entry("257/50", "SendPinOverTheAir"),
            Map.entry("257/51", "RequirePinForRemoteOperation"),
            Map.entry("257/52", "SecurityLevel"),
            Map.entry("257/53", "ExpiringUserTimeout"),
            Map.entry("257/128", "AliroReaderVerificationKey"),
            Map.entry("257/129", "AliroReaderGroupIdentifier"),
            Map.entry("257/130", "AliroReaderGroupSubIdentifier"),
            Map.entry("257/131", "AliroExpeditedTransactionSupportedProtocolVersions"),
            Map.entry("257/132", "AliroGroupResolvingKey"),
            Map.entry("257/133", "AliroSupportedBleuwbProtocolVersions"),
            Map.entry("257/134", "AliroBleAdvertisingVersion"),
            Map.entry("257/135", "NumberOfAliroCredentialIssuerKeysSupported"),
            Map.entry("257/136", "NumberOfAliroEndpointKeysSupported"),
            Map.entry("258/0", "Type"),
            Map.entry("258/5", "NumberOfActuationsLift"),
            Map.entry("258/6", "NumberOfActuationsTilt"),
            Map.entry("258/7", "ConfigStatus"),
            Map.entry("258/8", "CurrentPositionLiftPercentage"),
            Map.entry("258/9", "CurrentPositionTiltPercentage"),
            Map.entry("258/10", "OperationalStatus"),
            Map.entry("258/11", "TargetPositionLiftPercent100ths"),
            Map.entry("258/12", "TargetPositionTiltPercent100ths"),
            Map.entry("258/13", "EndProductType"),
            Map.entry("258/14", "CurrentPositionLiftPercent100ths"),
            Map.entry("258/15", "CurrentPositionTiltPercent100ths"),
            Map.entry("258/23", "Mode"),
            Map.entry("258/26", "SafetyStatus"),
            Map.entry("512/0", "MaxPressure"),
            Map.entry("512/1", "MaxSpeed"),
            Map.entry("512/2", "MaxFlow"),
            Map.entry("512/3", "MinConstPressure"),
            Map.entry("512/4", "MaxConstPressure"),
            Map.entry("512/5", "MinCompPressure"),
            Map.entry("512/6", "MaxCompPressure"),
            Map.entry("512/7", "MinConstSpeed"),
            Map.entry("512/8", "MaxConstSpeed"),
            Map.entry("512/9", "MinConstFlow"),
            Map.entry("512/10", "MaxConstFlow"),
            Map.entry("512/11", "MinConstTemp"),
            Map.entry("512/12", "MaxConstTemp"),
            Map.entry("512/16", "PumpStatus"),
            Map.entry("512/17", "EffectiveOperationMode"),
            Map.entry("512/18", "EffectiveControlMode"),
            Map.entry("512/19", "Capacity"),
            Map.entry("512/20", "Speed"),
            Map.entry("512/21", "LifetimeRunningHours"),
            Map.entry("512/22", "Power"),
            Map.entry("512/23", "LifetimeEnergyConsumed"),
            Map.entry("512/32", "OperationMode"),
            Map.entry("512/33", "ControlMode"),
            Map.entry("512/34", "AlarmMask"),
            Map.entry("513/0", "LocalTemperature"),
            Map.entry("513/1", "OutdoorTemperature"),
            Map.entry("513/2", "Occupancy"),
            Map.entry("513/3", "AbsMinHeatSetpointLimit"),
            Map.entry("513/4", "AbsMaxHeatSetpointLimit"),
            Map.entry("513/5", "AbsMinCoolSetpointLimit"),
            Map.entry("513/6", "AbsMaxCoolSetpointLimit"),
            Map.entry("513/7", "PiCoolingDemand"),
            Map.entry("513/8", "PiHeatingDemand"),
            Map.entry("513/9", "HvacSystemTypeConfiguration"),
            Map.entry("513/16", "LocalTemperatureCalibration"),
            Map.entry("513/17", "OccupiedCoolingSetpoint"),
            Map.entry("513/18", "OccupiedHeatingSetpoint"),
            Map.entry("513/19", "UnoccupiedCoolingSetpoint"),
            Map.entry("513/20", "UnoccupiedHeatingSetpoint"),
            Map.entry("513/21", "MinHeatSetpointLimit"),
            Map.entry("513/22", "MaxHeatSetpointLimit"),
            Map.entry("513/23", "MinCoolSetpointLimit"),
            Map.entry("513/24", "MaxCoolSetpointLimit"),
            Map.entry("513/25", "MinSetpointDeadBand"),
            Map.entry("513/26", "RemoteSensing"),
            Map.entry("513/27", "ControlSequenceOfOperation"),
            Map.entry("513/28", "SystemMode"),
            Map.entry("513/30", "ThermostatRunningMode"),
            Map.entry("513/35", "TemperatureSetpointHold"),
            Map.entry("513/36", "TemperatureSetpointHoldDuration"),
            Map.entry("513/37", "ThermostatProgrammingOperationMode"),
            Map.entry("513/41", "ThermostatRunningState"),
            Map.entry("513/48", "SetpointChangeSource"),
            Map.entry("513/49", "SetpointChangeAmount"),
            Map.entry("513/50", "SetpointChangeSourceTimestamp"),
            Map.entry("513/52", "OccupiedSetback"),
            Map.entry("513/53", "OccupiedSetbackMin"),
            Map.entry("513/54", "OccupiedSetbackMax"),
            Map.entry("513/55", "UnoccupiedSetback"),
            Map.entry("513/56", "UnoccupiedSetbackMin"),
            Map.entry("513/57", "UnoccupiedSetbackMax"),
            Map.entry("513/58", "EmergencyHeatDelta"),
            Map.entry("513/64", "AcType"),
            Map.entry("513/65", "AcCapacity"),
            Map.entry("513/66", "AcRefrigerantType"),
            Map.entry("513/67", "AcCompressorType"),
            Map.entry("513/68", "AcErrorCode"),
            Map.entry("513/69", "AcLouverPosition"),
            Map.entry("513/70", "AcCoilTemperature"),
            Map.entry("513/71", "AcCapacityFormat"),
            Map.entry("513/72", "PresetTypes"),
            Map.entry("513/73", "ScheduleTypes"),
            Map.entry("513/74", "NumberOfPresets"),
            Map.entry("513/75", "NumberOfSchedules"),
            Map.entry("513/76", "NumberOfScheduleTransitions"),
            Map.entry("513/77", "NumberOfScheduleTransitionPerDay"),
            Map.entry("513/78", "ActivePresetHandle"),
            Map.entry("513/79", "ActiveScheduleHandle"),
            Map.entry("513/80", "Presets"),
            Map.entry("513/81", "Schedules"),
            Map.entry("513/82", "SetpointHoldExpiryTimestamp"),
            Map.entry("513/83", "MaxThermostatSuggestions"),
            Map.entry("513/84", "ThermostatSuggestions"),
            Map.entry("513/85", "CurrentThermostatSuggestion"),
            Map.entry("513/86", "ThermostatSuggestionNotFollowingReason"),
            Map.entry("768/0", "CurrentHue"),
            Map.entry("768/1", "CurrentSaturation"),
            Map.entry("768/2", "RemainingTime"),
            Map.entry("768/3", "CurrentX"),
            Map.entry("768/4", "CurrentY"),
            Map.entry("768/5", "DriftCompensation"),
            Map.entry("768/6", "CompensationText"),
            Map.entry("768/7", "ColorTemperatureMireds"),
            Map.entry("768/8", "ColorMode"),
            Map.entry("768/15", "Options"),
            Map.entry("768/16", "NumberOfPrimaries"),
            Map.entry("768/17", "Primary1X"),
            Map.entry("768/18", "Primary1Y"),
            Map.entry("768/19", "Primary1Intensity"),
            Map.entry("768/21", "Primary2X"),
            Map.entry("768/22", "Primary2Y"),
            Map.entry("768/23", "Primary2Intensity"),
            Map.entry("768/25", "Primary3X"),
            Map.entry("768/26", "Primary3Y"),
            Map.entry("768/27", "Primary3Intensity"),
            Map.entry("768/32", "Primary4X"),
            Map.entry("768/33", "Primary4Y"),
            Map.entry("768/34", "Primary4Intensity"),
            Map.entry("768/36", "Primary5X"),
            Map.entry("768/37", "Primary5Y"),
            Map.entry("768/38", "Primary5Intensity"),
            Map.entry("768/40", "Primary6X"),
            Map.entry("768/41", "Primary6Y"),
            Map.entry("768/42", "Primary6Intensity"),
            Map.entry("768/48", "WhitePointX"),
            Map.entry("768/49", "WhitePointY"),
            Map.entry("768/50", "ColorPointRx"),
            Map.entry("768/51", "ColorPointRy"),
            Map.entry("768/52", "ColorPointRIntensity"),
            Map.entry("768/54", "ColorPointGx"),
            Map.entry("768/55", "ColorPointGy"),
            Map.entry("768/56", "ColorPointGIntensity"),
            Map.entry("768/58", "ColorPointBx"),
            Map.entry("768/59", "ColorPointBy"),
            Map.entry("768/60", "ColorPointBIntensity"),
            Map.entry("768/16384", "EnhancedCurrentHue"),
            Map.entry("768/16385", "EnhancedColorMode"),
            Map.entry("768/16386", "ColorLoopActive"),
            Map.entry("768/16387", "ColorLoopDirection"),
            Map.entry("768/16388", "ColorLoopTime"),
            Map.entry("768/16389", "ColorLoopStartEnhancedHue"),
            Map.entry("768/16390", "ColorLoopStoredEnhancedHue"),
            Map.entry("768/16394", "ColorCapabilities"),
            Map.entry("768/16395", "ColorTempPhysicalMinMireds"),
            Map.entry("768/16396", "ColorTempPhysicalMaxMireds"),
            Map.entry("768/16397", "CoupleColorTempToLevelMinMireds"),
            Map.entry("768/16400", "StartUpColorTemperatureMireds"),
            Map.entry("1024/0", "MeasuredValue"),
            Map.entry("1024/1", "MinMeasuredValue"),
            Map.entry("1024/2", "MaxMeasuredValue"),
            Map.entry("1024/3", "Tolerance"),
            Map.entry("1024/4", "LightSensorType"),
            Map.entry("1026/0", "MeasuredValue"),
            Map.entry("1026/1", "MinMeasuredValue"),
            Map.entry("1026/2", "MaxMeasuredValue"),
            Map.entry("1026/3", "Tolerance"),
            Map.entry("1027/0", "MeasuredValue"),
            Map.entry("1027/1", "MinMeasuredValue"),
            Map.entry("1027/2", "MaxMeasuredValue"),
            Map.entry("1027/3", "Tolerance"),
            Map.entry("1027/16", "ScaledValue"),
            Map.entry("1027/17", "MinScaledValue"),
            Map.entry("1027/18", "MaxScaledValue"),
            Map.entry("1027/19", "ScaledTolerance"),
            Map.entry("1027/20", "Scale"),
            Map.entry("1028/0", "MeasuredValue"),
            Map.entry("1028/1", "MinMeasuredValue"),
            Map.entry("1028/2", "MaxMeasuredValue"),
            Map.entry("1028/3", "Tolerance"),
            Map.entry("1029/0", "MeasuredValue"),
            Map.entry("1029/1", "MinMeasuredValue"),
            Map.entry("1029/2", "MaxMeasuredValue"),
            Map.entry("1029/3", "Tolerance"),
            Map.entry("1030/0", "Occupancy"),
            Map.entry("1030/1", "OccupancySensorType"),
            Map.entry("1030/2", "OccupancySensorTypeBitmap"),
            Map.entry("1030/3", "HoldTime"),
            Map.entry("1030/4", "HoldTimeLimits"),
            Map.entry("1030/16", "PirOccupiedToUnoccupiedDelay"),
            Map.entry("1030/17", "PirUnoccupiedToOccupiedDelay"),
            Map.entry("1030/18", "PirUnoccupiedToOccupiedThreshold"),
            Map.entry("1030/32", "UltrasonicOccupiedToUnoccupiedDelay"),
            Map.entry("1030/33", "UltrasonicUnoccupiedToOccupiedDelay"),
            Map.entry("1030/34", "UltrasonicUnoccupiedToOccupiedThreshold"),
            Map.entry("1030/48", "PhysicalContactOccupiedToUnoccupiedDelay"),
            Map.entry("1030/49", "PhysicalContactUnoccupiedToOccupiedDelay"),
            Map.entry("1030/50", "PhysicalContactUnoccupiedToOccupiedThreshold"),
            Map.entry("*/65528", "GeneratedCommandList"),
            Map.entry("*/65529", "AcceptedCommandList"),
            Map.entry("*/65530", "EventList"),
            Map.entry("*/65531", "AttributeList"),
            Map.entry("*/65532", "FeatureMap"),
            Map.entry("*/65533", "ClusterRevision")
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
            145,  // ElectricalEnergyMeasurement
            // Added 2026-10-11: a Shelly Flood leak detector exposes only
            // BooleanState plus this, and this is where SensorFault lives -- a
            // leak detector that cannot tell you it is faulty is worth little.
            128   // BooleanStateConfiguration
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
