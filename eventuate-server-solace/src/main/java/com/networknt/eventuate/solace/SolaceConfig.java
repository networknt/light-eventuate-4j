package com.networknt.eventuate.solace;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * A Kafka setting configuration file. It get from defined resource yml file
 *
 */
public class SolaceConfig {
    public static final String CONFIG_NAME = "solace";


    private String host;
    private String username;
    private String password;
    private String vpn;


    @JsonIgnore
    String description;


    public SolaceConfig() {
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVpn() {
        return vpn;
    }

    public void setVpn(String vpn) {
        this.vpn = vpn;
    }
}
