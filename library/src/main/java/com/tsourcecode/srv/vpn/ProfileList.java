package com.tsourcecode.srv.vpn;

import java.util.ArrayList;

import de.blinkt.openvpn.api.APIVpnProfile;

public class ProfileList<T extends APIVpnProfile> extends ArrayList<T> {
    public T findByName(String name){
        for (T profile : this){
            if (profile.mName.equals(name)){
                return profile;
            }
        }
        return null;
    }

    public T findByUUID(String uuid){
        for (T profile : this){
            if (profile.mUUID.equals(uuid)){
                return profile;
            }
        }
        return null;
    }
}
