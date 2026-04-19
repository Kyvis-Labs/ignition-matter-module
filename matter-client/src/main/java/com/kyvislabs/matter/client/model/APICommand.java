package com.kyvislabs.matter.client.model;

public enum APICommand {
    START_LISTENING("start_listening"),
    SERVER_DIAGNOSTICS("diagnostics"),
    SERVER_INFO("server_info"),
    GET_NODES("get_nodes"),
    GET_NODE("get_node"),
    COMMISSION_WITH_CODE("commission_with_code"),
    COMMISSION_ON_NETWORK("commission_on_network"),
    SET_WIFI_CREDENTIALS("set_wifi_credentials"),
    SET_THREAD_DATASET("set_thread_dataset"),
    OPEN_COMMISSIONING_WINDOW("open_commissioning_window"),
    DISCOVER("discover"),
    INTERVIEW_NODE("interview_node"),
    DEVICE_COMMAND("device_command"),
    REMOVE_NODE("remove_node"),
    GET_VENDOR_NAMES("get_vendor_names"),
    READ_ATTRIBUTE("read_attribute"),
    WRITE_ATTRIBUTE("write_attribute"),
    PING_NODE("ping_node"),
    GET_NODE_IP_ADDRESSES("get_node_ip_addresses"),
    IMPORT_TEST_NODE("import_test_node"),
    CHECK_NODE_UPDATE("check_node_update"),
    UPDATE_NODE("update_node"),
    SET_DEFAULT_FABRIC_LABEL("set_default_fabric_label"),
    SET_ACL_ENTRY("set_acl_entry"),
    SET_NODE_BINDING("set_node_binding");

    private final String value;

    APICommand(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
