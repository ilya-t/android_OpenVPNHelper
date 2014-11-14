package com.tsourcecode.srv.vpn;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import de.blinkt.openvpn.api.APIVpnProfile;
import de.blinkt.openvpn.api.IOpenVPNAPIService;
import de.blinkt.openvpn.api.IOpenVPNStatusCallback;

public class VpnHelper implements Handler.Callback{
    private static final int MSG_UPDATE_STATE = 0;
    private static final int MSG_UPDATE_MYIP = 1;
    public static final int START_PROFILE_EMBEDDED = 2;
    public static final int START_PROFILE_BYUUID = 3;
    private static final int ICS_OPENVPN_PERMISSION = 7;

    private boolean isConnected;

    private final Activity activity;
    protected IOpenVPNAPIService mService=null;

    private ActivityCallback activityCallback = new ActivityCallback() {
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode == Activity.RESULT_OK) {
                if(requestCode==START_PROFILE_EMBEDDED)
                    startEmbeddedProfile();
                if(requestCode==START_PROFILE_BYUUID)
                    try {
                        mService.startProfile(mStartUUID);
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                if (requestCode == ICS_OPENVPN_PERMISSION) {
                    listVPNs();
                    try {
                        mService.registerStatusCallback(mCallback);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    };

    private IOpenVPNStatusCallback mCallback = new IOpenVPNStatusCallback.Stub() {
        /**
         * This is called by the remote service regularly to tell us about
         * new values.  Note that IPC calls are dispatched through a thread
         * pool running in each process, so the code executing here will
         * NOT be running in our main thread like most other things -- so,
         * to update the UI, we need to use a Handler to hop over there.
         */

        @Override
        public void newStatus(String uuid, String state, String message, String level)
                throws RemoteException {
            Message msg = Message.obtain(mHandler, MSG_UPDATE_STATE, state + "|" + message);
            msg.sendToTarget();

        }

    };
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.

            mService = IOpenVPNAPIService.Stub.asInterface(service);

            try {
                // Request permission to use the API
                Intent i = mService.prepare(activity.getPackageName());
                if (i!=null) {
                    activity.startActivityForResult(i, ICS_OPENVPN_PERMISSION);
                } else {
                    activityCallback.onActivityResult(ICS_OPENVPN_PERMISSION, Activity.RESULT_OK, null);
                }

            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            isConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            isConnected = false;
        }
    };

    private String mStartUUID=null;

    private Handler mHandler;

    public VpnHelper(Activity activity) {
        this.activity = activity;
        mHandler = new Handler(this);
    }

    public ActivityCallback bindToService(){
        activity.bindService(new Intent(IOpenVPNAPIService.class.getName()),
                serviceConnection, Context.BIND_AUTO_CREATE);
        return this.activityCallback;
    }

    public void unbindService() {
        activity.unbindService(serviceConnection);
    }

    private void startEmbeddedProfile(){
        try {
            InputStream conf = activity.getAssets().open("test.conf");
            InputStreamReader isr = new InputStreamReader(conf);
            BufferedReader br = new BufferedReader(isr);
            String config="";
            String line;
            while(true) {
                line = br.readLine();
                if(line == null)
                    break;
                config += line + "\n";
            }
            br.readLine();

            //			mService.addVPNProfile("test", config);
            mService.startVPN(config);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Deprecated
    protected void listVPNs() {
/*
        try {
            List<APIVpnProfile> list = mService.getProfiles();
            String all="List:";
            for(APIVpnProfile vp:list) {
                all = all + vp.mName + ":" + vp.mUUID + "\n";
            }

            if(list.size()> 0) {
                Button b= mStartVpn;
                b.setOnClickListener(this);
                b.setVisibility(View.VISIBLE);
                b.setText(list.get(0).mName);
                mStartUUID = list.get(0).mUUID;
            }



            mHelloWorld.setText(all);

        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            mHelloWorld.setText(e.getMessage());
        }
*/
    }

    private void prepareStartProfile(int requestCode) throws RemoteException {
        Intent requestpermission = mService.prepareVPNService();
        if(requestpermission == null) {
            activityCallback.onActivityResult(requestCode, Activity.RESULT_OK, null);
        } else {
            // Have to call an external Activity since services cannot used onActivityResult
            activity.startActivityForResult(requestpermission, requestCode);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
//FIXME implement later
/*
        if(msg.what == MSG_UPDATE_STATE) {
            mStatus.setText((CharSequence) msg.obj);
        } else if (msg.what == MSG_UPDATE_MYIP) {

            mMyIp.setText((CharSequence) msg.obj);
        }
*/
        return true;
    }

    public IOpenVPNAPIService getApiInterface() {
        if (mService == null){
            bindToService();
        }
        return mService;
    }


    public boolean isConnected() {
        return isConnected;
    }

    public boolean addProfile(String name, String config) {
        if (getApiInterface() != null){
            try {
                ProfileList<APIVpnProfile> profileList = getProfileList();

                if (profileList.findByName(name) != null){
                    return false;
                }

                return getApiInterface().addVPNProfile(name, config);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public boolean startVPN(String inlineConfig) {
        if (getApiInterface() != null){
            try {
                getApiInterface().startVPN(inlineConfig);
                return true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return false;
    }


    public boolean disconnect() {
        if (getApiInterface() != null){
            try {
                getApiInterface().disconnect();
                return true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public ProfileList<APIVpnProfile> getProfileList() {
        if (getApiInterface() != null){
            try {
                List<APIVpnProfile> list = getApiInterface().getProfiles();
                ProfileList<APIVpnProfile> profileList = new ProfileList<>();
                if (list != null && list.size() > 0)
                profileList.addAll(list);
                return profileList;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean startProfile(String profileName) {
        if (getApiInterface() != null){
            APIVpnProfile profile = getProfileList().findByName(profileName);
            try {
                if (profile != null){
                    getApiInterface().startProfile(profile.mUUID);
                    return true;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
