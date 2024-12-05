package io.approov.shapes;

import android.util.Log;

import okhttp3.*;
import okhttp3.EventListener;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MetricsEventListener extends EventListener {
    private long dnsStartTime;
    private long dnsEndTime;
    private long connectStartTime;
    private long connectEndTime;
    private int responseCode = -1; // Default: unknown response code
    private String domainName;
    private String connectedIpAddress; // Store the actual connected IP address

    @Override
    public void dnsStart(Call call, String domainName) {
        dnsStartTime = System.nanoTime();
        this.domainName = domainName;
    }

    @Override
    public void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList) {
        dnsEndTime = System.nanoTime();
    }

    @Override
    public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
        connectStartTime = System.nanoTime();
        // Capture the connected IP address
        this.connectedIpAddress = inetSocketAddress.getAddress().getHostAddress();
    }

    @Override
    public void connectEnd(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, Protocol protocol) {
        connectEndTime = System.nanoTime();
        // Ensure the connected IP address is updated in case of retries
        this.connectedIpAddress = inetSocketAddress.getAddress().getHostAddress();
    }

    @Override
    public void responseHeadersEnd(Call call, Response response) {
        responseCode = response.code(); // Capture the HTTP response code
    }

    public String getMetricsSummary() {
        // Format as "dns: xx ms, con: yy ms, status: zz, ip: xx.xx.xx.xx"
        long dnsDuration = dnsEndTime > 0 ? TimeUnit.NANOSECONDS.toMillis(dnsEndTime - dnsStartTime) : 0;
        long connectDuration = connectEndTime > 0 ? TimeUnit.NANOSECONDS.toMillis(connectEndTime - connectStartTime) : 0;
        String statusCodeString = (responseCode > 0) ? String.valueOf(responseCode) : "N/A";
        String resolvedIp = (connectedIpAddress != null) ? connectedIpAddress : "N/A";

        Log.d(MainActivity.TAG, domainName + " DNS Duration: " + dnsDuration + "ms, Connect Duration: " +
                connectDuration + "ms, Status Code: " + statusCodeString + ", Connected IP: " + resolvedIp);

        return "dns: " + dnsDuration + " ms, con: " + connectDuration + " ms, status: " +
                statusCodeString + ", ip: " + resolvedIp;
    }
}

