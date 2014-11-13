package com.tsourcecode.srv.vpn;

import android.content.Intent;

public interface ActivityCallback {
    void onActivityResult(int requestCode, int resultCode, Intent data);
}
