package com.mizuledevelopment.shared.objects;

import lombok.Data;

import java.util.UUID;

@Data
public class ServerModel {

    private String host;
    private String name;
    private String containerID;
    private UUID uuid;
    private int rconPort;
    private int serverPort;
    private String rconPassword;
    private long time;

}
