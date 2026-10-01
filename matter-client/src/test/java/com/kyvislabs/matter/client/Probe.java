package com.kyvislabs.matter.client;

import com.kyvislabs.matter.client.model.MatterNodeData;
import com.kyvislabs.matter.client.model.ServerInfoMessage;

import java.util.List;

/**
 * Manual probe harness: connects to a Matter server, dumps the handshake and node
 * inventory, then streams events. Works against both python-matter-server and
 * matterjs-server.
 *
 * Usage: Probe <ws-url> [seconds-to-watch]
 */
public class Probe {
    public static void main(String[] args) throws Exception {
        String url = args.length > 0 ? args[0] : "ws://127.0.0.1:5580/ws";
        long watchSeconds = args.length > 1 ? Long.parseLong(args[1]) : 10;

        MatterClient client = new MatterClient(url);
        ServerInfoMessage info = client.connect();
        System.out.println("connected: " + url);
        System.out.println("  fabric_id            = " + info.getFabricId());
        System.out.println("  compressed_fabric_id = " + info.getCompressedFabricId());
        System.out.println("  schema_version       = " + info.getSchemaVersion()
                + " (min " + info.getMinSupportedSchemaVersion() + ")");
        System.out.println("  sdk_version          = " + info.getSdkVersion());

        List<MatterNodeData> nodes = client.startListening();
        System.out.println("nodes: " + nodes.size());
        for (MatterNodeData n : nodes) {
            System.out.printf("  node %s  available=%s  bridge=%s  attrs=%d%n",
                    Long.toUnsignedString(n.getNodeId()), n.isAvailable(), n.isBridge(),
                    n.getAttributes() == null ? 0 : n.getAttributes().size());
        }

        client.addEventListener((type, data) ->
                System.out.println("event " + type + " " + abbreviate(String.valueOf(data))));

        System.out.println("watching " + watchSeconds + "s ...");
        Thread.sleep(watchSeconds * 1000);
        client.close();
        System.out.println("done");
    }

    private static String abbreviate(String s) {
        return s.length() <= 160 ? s : s.substring(0, 160) + "...";
    }
}
