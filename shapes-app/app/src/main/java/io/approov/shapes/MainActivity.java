//
// MIT License
//
// Copyright (c) 2016-present, Critical Blue Ltd.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
// (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
// publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
// subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
// ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
// THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package io.approov.shapes;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

enum TEST_TYPE {
    UNKNOWN,
    NETWORK_DNS,
    CUSTOM_DNS
}

public class MainActivity extends Activity {
    public static final String TAG = "NETWORK_TAG";
    private Activity activity;
    private View statusView = null;
    private ImageView statusImageView = null;
    private TextView statusTextView = null;
    private Button helloCheckButton = null;
    private Button shapesCheckButton = null;
    private ArrayList<String> mHosts = new ArrayList<String>();
    private TextView[] hostTextViews = new TextView[8];
    private TextView[] hostDataTextViews = new TextView[8];
    private TextView[] hostDataTextViews2 = new TextView[8];
    private TEST_TYPE lastTest = TEST_TYPE.UNKNOWN;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        mHosts.add("https://attest.approovr.io/health");
        mHosts.add("https://attest.approovr.com/health");
        mHosts.add("https://liffey-ew1-ew1.approovr.io/health");
        mHosts.add("https://liffey-ew1-uw1.approovr.io/health");
        mHosts.add("https://liffey-ew1-apse1.approovr.io/health");
        mHosts.add("https://liffey-ew1-sae1.approovr.io/health");
        mHosts.add("https://google.com");
        mHosts.add("https://www.cloudflare.com/trademark/");
        // Initialize TextView references for hostnames
        hostTextViews[0] = findViewById(R.id.hostname1);
        hostTextViews[1] = findViewById(R.id.hostname2);
        hostTextViews[2] = findViewById(R.id.hostname3);
        hostTextViews[3] = findViewById(R.id.hostname4);
        hostTextViews[4] = findViewById(R.id.hostname5);
        hostTextViews[5] = findViewById(R.id.hostname6);
        hostTextViews[6] = findViewById(R.id.hostname7);
        hostTextViews[7] = findViewById(R.id.hostname8);

        // Initialize TextView references for metrics
        hostDataTextViews[0] = findViewById(R.id.hostname1Data);
        hostDataTextViews[1] = findViewById(R.id.hostname2Data);
        hostDataTextViews[2] = findViewById(R.id.hostname3Data);
        hostDataTextViews[3] = findViewById(R.id.hostname4Data);
        hostDataTextViews[4] = findViewById(R.id.hostname5Data);
        hostDataTextViews[5] = findViewById(R.id.hostname6Data);
        hostDataTextViews[6] = findViewById(R.id.hostname7Data);
        hostDataTextViews[7] = findViewById(R.id.hostname8Data);

        // Initialize TextView two references for metrics
        hostDataTextViews2[0] = findViewById(R.id.hostname1Data2);
        hostDataTextViews2[1] = findViewById(R.id.hostname2Data2);
        hostDataTextViews2[2] = findViewById(R.id.hostname3Data2);
        hostDataTextViews2[3] = findViewById(R.id.hostname4Data2);
        hostDataTextViews2[4] = findViewById(R.id.hostname5Data2);
        hostDataTextViews2[5] = findViewById(R.id.hostname6Data2);
        hostDataTextViews2[6] = findViewById(R.id.hostname7Data2);
        hostDataTextViews2[7] = findViewById(R.id.hostname8Data2);

        // Populate the hostname TextViews
        populateHostnames();
        clearMetrics(); // Clear metrics on startup
        // find controls

        helloCheckButton = findViewById(R.id.btnConnectionCheck);
        shapesCheckButton = findViewById(R.id.btnShapesCheck);

        // handle hello connection check
        helloCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if previous test used this configuration
                if ((lastTest == TEST_TYPE.CUSTOM_DNS) || (lastTest == TEST_TYPE.UNKNOWN)) {
                    // Clear the metrics
                    for (int i = 0; i < hostDataTextViews.length; i++) {
                        hostDataTextViews[i].setText("");
                        hostDataTextViews2[i].setText("");
                    }
                }
                checkHosts();
                lastTest = TEST_TYPE.NETWORK_DNS;
            }
        });


        // handle getting shapes
        shapesCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if previous test used this configuration
                if ((lastTest == TEST_TYPE.NETWORK_DNS) || (lastTest == TEST_TYPE.UNKNOWN)) {
                    // Clear the metrics
                    for (int i =0; i < hostDataTextViews.length; i++) {
                        hostDataTextViews[i].setText("");
                        hostDataTextViews2[i].setText("");
                    }
                }
                checkHostsUsingCustomDNS();
                lastTest = TEST_TYPE.CUSTOM_DNS;
            }
        });
    }

    private void populateHostnames() {
        for (int i = 0; i < mHosts.size(); i++) {
            hostTextViews[i].setText(mHosts.get(i));
        }
    }

    private void clearMetrics() {
        for (TextView metricsView : hostDataTextViews) {
            metricsView.setText(""); // Clear all metrics
        }
        for (TextView metricsView : hostDataTextViews2) {
            metricsView.setText("");
        }
    }
    void checkHosts() {
        for (int i = 0; i < mHosts.size(); i++) {
            String host = mHosts.get(i);
            final int index = i;

            // Update hostname TextView
            hostTextViews[index].setText(host);

            // Make network call and collect metrics
            new Thread(() -> {
                MetricsEventListener listener = new MetricsEventListener();
                OkHttpClient client = new OkHttpClient.Builder()
                        .eventListener(listener)
                        .build();

                Request request = new Request.Builder().url(host).build();

                try (Response response = client.newCall(request).execute()) {
                    listener.responseHeadersEnd(null, response); // Ensure response metrics are captured
                } catch (Exception e) {
                    Log.d(TAG, "Network call failed: " + e.getMessage());
                } finally {
                    String metrics = listener.getMetricsSummary();

                    // Update metrics TextView on the main thread
                    runOnUiThread(() -> {
                        // Add info to the first field unless it is already not empty
                        if (hostDataTextViews[index].getText().toString().isEmpty()) {
                            hostDataTextViews[index].setText(metrics);
                        } else {
                            hostDataTextViews2[index].setText(metrics);
                        }
                    });

                }
            }).start();
        }
    }

    void checkHostsUsingCustomDNS() {
        for (int i = 0; i < mHosts.size(); i++) {
            String host = mHosts.get(i);
            final int index = i;

            // Update hostname TextView
            hostTextViews[index].setText(host);

            // Make network call and collect metrics
            new Thread(() -> {
                MetricsEventListener listener = new MetricsEventListener();
                OkHttpClient client = new OkHttpClient.Builder()
                        .dns(new CustomDns()) // Use the custom DNS implementation
                        .eventListener(listener)
                        .build();

                Request request = new Request.Builder().url(host).build();

                try (Response response = client.newCall(request).execute()) {
                    listener.responseHeadersEnd(null, response); // Ensure response metrics are captured
                } catch (Exception e) {
                    Log.d(TAG, "Network call failed: " + e.getMessage());
                } finally {
                    String metrics = listener.getMetricsSummary();

                    // Update metrics TextView on the main thread
                    runOnUiThread(() -> {
                        // Add info to the first field unless it is already not empty
                        if (hostDataTextViews[index].getText().toString().isEmpty()) {
                            hostDataTextViews[index].setText(metrics);
                        } else {
                            hostDataTextViews2[index].setText(metrics);
                        }
                    });
                }
            }).start();
        }
    }

}


