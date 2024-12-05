package io.approov.shapes;
import okhttp3.Dns;
import org.xbill.DNS.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class CustomDns implements Dns {
    private static final String DNS_SERVER = "1.1.1.1"; // Predefined DNS server

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        try {
            // Create a DNS lookup for the specified hostname
            Lookup lookup = new Lookup(hostname, Type.A);
            SimpleResolver resolver = new SimpleResolver(DNS_SERVER);
            lookup.setResolver(resolver);

            Record[] records = lookup.run();

            if (lookup.getResult() == Lookup.SUCCESSFUL && records != null) {
                List<InetAddress> result = new ArrayList<>();
                for (Record record : records) {
                    if (record instanceof ARecord) {
                        ARecord aRecord = (ARecord) record;
                        result.add(aRecord.getAddress());
                    }
                }
                return result;
            } else {
                throw new UnknownHostException("Unable to resolve hostname: " + hostname);
            }
        } catch (TextParseException e) {
            throw new UnknownHostException("Invalid hostname: " + hostname);
        } catch (Exception e) {
            throw new UnknownHostException("DNS lookup failed: " + e.getMessage());
        }
    }
}
