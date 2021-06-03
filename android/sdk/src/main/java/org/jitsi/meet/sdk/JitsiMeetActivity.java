/*
 * Copyright @ 2019-present 8x8, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jitsi.meet.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.modules.core.PermissionListener;

import org.jitsi.meet.sdk.animation.JitsiTextAndAnimationView;
import org.jitsi.meet.sdk.log.JitsiMeetLogger;

import java.util.Map;


/**
 * A base activity for SDK users to embed. It uses {@link JitsiMeetFragment} to do the heavy
 * lifting and wires the remaining Activity lifecycle methods so it works out of the box.
 */
public class JitsiMeetActivity extends FragmentActivity
    implements JitsiMeetActivityInterface, JitsiMeetViewListener {

    protected static final String TAG = JitsiMeetActivity.class.getSimpleName();

    private static final String ACTION_JITSI_MEET_CONFERENCE = "org.jitsi.meet.CONFERENCE";
    private static final String JITSI_MEET_CONFERENCE_OPTIONS = "JitsiMeetConferenceOptions";
    private static final String JITSI_MEET_CONFERENCE_MESSAGE = "JitsiMeetConferenceMessage";
    private BroadcastReceiver mCallHangup = new mCallHangup();
    private AppCompatTextView timer;
    private JitsiTextAndAnimationView connecting;
    private RelativeLayout connectLayout;
    private RelativeLayout callingView;
    private long startTime = -1L;
    private String message;
    // Helpers for starting the activity
    //

    public static void launch(Context context, JitsiMeetConferenceOptions options, String message) {
        Intent intent = new Intent(context, JitsiMeetActivity.class);
        intent.setAction(ACTION_JITSI_MEET_CONFERENCE);
        intent.putExtra(JITSI_MEET_CONFERENCE_OPTIONS, options);
        intent.putExtra(JITSI_MEET_CONFERENCE_MESSAGE, message);
        context.startActivity(intent);
    }

    public static void launch(Context context, String url, String message) {
        JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder().setRoom(url).build();
        launch(context, options, message);
    }

//    public static void leave() {
//        onDestroy();
//    }

    // Overrides
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_jitsi_meet);
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        connectLayout = findViewById(R.id.connectLayout);
        callingView = findViewById(R.id.callingView);
        timer = findViewById(R.id.timer);
        connecting = findViewById(R.id.connecting);
        connectLayout.setVisibility(View.VISIBLE);
        callingView.setVisibility(View.INVISIBLE);

        if (!extraInitialize()) {
            if(getIntent().hasExtra(JITSI_MEET_CONFERENCE_MESSAGE)) {
                message = getIntent().getStringExtra(JITSI_MEET_CONFERENCE_MESSAGE);
                connecting.setText(message);
            }
            initialize();
        }
    }

    @Override
    public void onDestroy() {
        // Here we are trying to handle the following corner case: an application using the SDK
        // is using this Activity for displaying meetings, but there is another "main" Activity
        // with other content. If this Activity is "swiped out" from the recent list we will get
        // Activity#onDestroy() called without warning. At this point we can try to leave the
        // current meeting, but when our view is detached from React the JS <-> Native bridge won't
        // be operational so the external API won't be able to notify the native side that the
        // conference terminated. Thus, try our best to clean up.


        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCallHangup);

        leave();
        if (AudioModeModule.useConnectionService()) {
            ConnectionService.abortConnections();
        }
//        JitsiMeetOngoingConferenceService.abort(this);
        connecting.stopAnimation();
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mCallHangup,
            new IntentFilter("CALL_HANGUP"));

    }

    public class mCallHangup extends BroadcastReceiver {
        private static final String TAG = "MyBroadcastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("CALL_HANGUP from Fugu", "CALL_HANGUP");
            leave();
        }
    }

    @Override
    public void finish() {
        leave();

        super.finish();
    }

    // Helper methods
    //

    protected JitsiMeetView getJitsiView() {
        JitsiMeetFragment fragment
            = (JitsiMeetFragment) getSupportFragmentManager().findFragmentById(R.id.jitsiFragment);
        return fragment.getJitsiView();
    }

    public void join(@Nullable String url) {
        JitsiMeetConferenceOptions options
            = new JitsiMeetConferenceOptions.Builder()
            .setRoom(url)
            .build();
        join(options);
    }

    public void join(JitsiMeetConferenceOptions options) {
        getJitsiView().join(options);
    }

    public void leave() {
        getJitsiView().leave();
    }

    private @Nullable
    JitsiMeetConferenceOptions getConferenceOptions(Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            if (uri != null) {
                return new JitsiMeetConferenceOptions.Builder().setRoom(uri.toString()).build();
            }
        } else if (ACTION_JITSI_MEET_CONFERENCE.equals(action)) {
            return intent.getParcelableExtra(JITSI_MEET_CONFERENCE_OPTIONS);
        }

        return null;
    }

    /**
     * Helper function called during activity initialization. If {@code true} is returned, the
     * initialization is delayed and the {@link JitsiMeetActivity#initialize()} method is not
     * called. In this case, it's up to the subclass to call the initialize method when ready.
     * <p>
     * This is mainly required so we do some extra initialization in the Jitsi Meet app.
     *
     * @return {@code true} if the initialization will be delayed, {@code false} otherwise.
     */
    protected boolean extraInitialize() {
        return false;
    }

    protected void initialize() {
        // Listen for conference events.
        getJitsiView().setListener(this);

        // Join the room specified by the URL the app was launched with.
        // Joining without the room option displays the welcome page.
        join(getConferenceOptions(getIntent()));
    }

    // Activity lifecycle methods
    //

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        JitsiMeetActivityDelegate.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        JitsiMeetActivityDelegate.onBackPressed();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        JitsiMeetConferenceOptions options;

        if ((options = getConferenceOptions(intent)) != null) {
            join(options);
            return;
        }

        JitsiMeetActivityDelegate.onNewIntent(intent);
    }

    @Override
    protected void onUserLeaveHint() {
        getJitsiView().enterPictureInPicture();
    }

    // JitsiMeetActivityInterface
    //

    @Override
    public void requestPermissions(String[] permissions, int requestCode, PermissionListener listener) {
        JitsiMeetActivityDelegate.requestPermissions(this, permissions, requestCode, listener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // JitsiMeetViewListener
    //

    @Override
    public void onConferenceJoined(Map<String, Object> data) {
        JitsiMeetLogger.i("Conference joined: " + data);
        // Launch the service for the ongoing notification.
//        JitsiMeetOngoingConferenceService.launch(this);
        if (connectLayout.getVisibility() == View.VISIBLE) {
            connectLayout.setVisibility(View.GONE);
            callingView.setVisibility(View.VISIBLE);
            connecting.stopAnimation();
            timer.setVisibility(View.VISIBLE);
            startTimer();
        }
    }

    @Override
    public void onConferenceTerminated(Map<String, Object> data) {
        // Terminate Call
        if (data.get("error") != null && data.get("error").equals("connection.droppedError")) {
            Intent mIntent = new Intent("INTERNET_ISSUE");
            LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
            JitsiMeetLogger.i("Conference terminated: " + data);
        } else {
            Intent mIntent = new Intent("CALL TERMINATED");
            LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
            JitsiMeetLogger.i("Conference terminated: " + data);
            finish();
        }
    }

    @Override
    public void onConferenceWillJoin(Map<String, Object> data) {
        JitsiMeetLogger.i("Conference will join: " + data);
    }

    private void startTimer() {
        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                new CountDownTimer(90000000, 1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                        timer.setVisibility(View.VISIBLE);
                        if (startTime == -1L) {
                            startTime = System.currentTimeMillis();
                        }
                        long currenTime = System.currentTimeMillis();
                        long diff = (currenTime - startTime) / 1000L;
                        long seconds = diff % 60L;
                        long minutes = diff / 60L;
                        long hours = minutes / 60L;
                        String secondstext = "";
                        if (seconds < 10) {
                            secondstext = "0" + seconds;
                        } else {
                            secondstext = seconds + "";
                        }
                        if (hours > 0) {
                            timer.setText(hours + ":+" + minutes + ":" + secondstext);
                        } else {
                            timer.setText(minutes + ":" + secondstext);
                        }
                    }

                    @Override
                    public void onFinish() {

                    }
                }.start();
            }
        }, 300);
    }
}
