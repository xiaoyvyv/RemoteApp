package com.freerdp.freerdpcore.domain;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Rdp配置标签模型
 */
public class RdpBookmark extends BaseRdpBookmark {
    private String hostname;
    private int port;
    private boolean enableGatewaySettings;
    private GatewaySettings gatewaySettings;

    public static final Parcelable.Creator<RdpBookmark> CREATOR =
            new Parcelable.Creator<RdpBookmark>() {
                public RdpBookmark createFromParcel(Parcel in) {
                    return new RdpBookmark(in);
                }

                @Override
                public RdpBookmark[] newArray(int size) {
                    return new RdpBookmark[size];
                }
            };


    public RdpBookmark() {
        super();
        init();
    }

    private void init() {
        type = TYPE_MANUAL;
        hostname = "";
        port = 3389;
        enableGatewaySettings = false;
        gatewaySettings = new GatewaySettings();
    }

    public RdpBookmark(Parcel parcel) {
        super(parcel);
        type = TYPE_MANUAL;
        hostname = parcel.readString();
        port = parcel.readInt();

        enableGatewaySettings = (parcel.readInt() == 1);
        gatewaySettings = parcel.readParcelable(GatewaySettings.class.getClassLoader());
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean getEnableGatewaySettings() {
        return enableGatewaySettings;
    }

    public void setEnableGatewaySettings(boolean enableGatewaySettings) {
        this.enableGatewaySettings = enableGatewaySettings;
    }

    public GatewaySettings getGatewaySettings() {
        return gatewaySettings;
    }

    public void setGatewaySettings(GatewaySettings gatewaySettings) {
        this.gatewaySettings = gatewaySettings;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(hostname);
        out.writeInt(port);
        out.writeInt(enableGatewaySettings ? 1 : 0);
        out.writeParcelable(gatewaySettings, flags);
    }

    @Override
    public void writeToSharedPreferences(SharedPreferences sharedPrefs) {
        super.writeToSharedPreferences(sharedPrefs);

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("bookmark.hostname", hostname);
        editor.putInt("bookmark.port", port);
        editor.putBoolean("bookmark.enable_gateway_settings", enableGatewaySettings);
        editor.putString("bookmark.gateway_hostname", gatewaySettings.getHostname());
        editor.putInt("bookmark.gateway_port", gatewaySettings.getPort());
        editor.putString("bookmark.gateway_username", gatewaySettings.getUsername());
        editor.putString("bookmark.gateway_password", gatewaySettings.getPassword());
        editor.putString("bookmark.gateway_domain", gatewaySettings.getDomain());
        editor.apply();
    }

    @Override
    public void readFromSharedPreferences(SharedPreferences sharedPrefs) {
        super.readFromSharedPreferences(sharedPrefs);

        hostname = sharedPrefs.getString("bookmark.hostname", "");
        port = sharedPrefs.getInt("bookmark.port", 3389);
        enableGatewaySettings = sharedPrefs.getBoolean("bookmark.enable_gateway_settings", false);
        gatewaySettings.setHostname(sharedPrefs.getString("bookmark.gateway_hostname", ""));
        gatewaySettings.setPort(sharedPrefs.getInt("bookmark.gateway_port", 443));
        gatewaySettings.setUsername(sharedPrefs.getString("bookmark.gateway_username", ""));
        gatewaySettings.setPassword(sharedPrefs.getString("bookmark.gateway_password", ""));
        gatewaySettings.setDomain(sharedPrefs.getString("bookmark.gateway_domain", ""));
    }

    // 网关设置类
    public static class GatewaySettings implements Parcelable {
        private String hostname;
        private int port;
        private String username;
        private String password;
        private String domain;

        public GatewaySettings() {
            hostname = "";
            port = 443;
            username = "";
            password = "";
            domain = "";
        }

        public GatewaySettings(Parcel parcel) {
            hostname = parcel.readString();
            port = parcel.readInt();
            username = parcel.readString();
            password = parcel.readString();
            domain = parcel.readString();
        }

        public static final Parcelable.Creator<GatewaySettings> CREATOR =
                new Parcelable.Creator<GatewaySettings>() {
                    public GatewaySettings createFromParcel(Parcel in) {
                        return new GatewaySettings(in);
                    }

                    @Override
                    public GatewaySettings[] newArray(int size) {
                        return new GatewaySettings[size];
                    }
                };

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
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

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(hostname);
            out.writeInt(port);
            out.writeString(username);
            out.writeString(password);
            out.writeString(domain);
        }
    }
}
