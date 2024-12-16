package abdelali.projet;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import java.time.Instant;

public class InfluxDBWriter {
    private static final String token = "gk67woj2X16V8qeDN40uj8S9p9zM4IF421F_-g1Y7kXTAzcSn5Uu6Yjp1WjWxEQYkftkSil0V2oS2jSfG-H_tw==";
    private static final String bucket = "fraud-detection-bucket";
    private static final String org = "fraud-detection-org";
    private static final InfluxDBClient client = InfluxDBClientFactory.create("http://localhost:8086", token.toCharArray());

    public static void writeTransaction(String userId, double amount, String timestamp) {
        Point point = Point.measurement("suspicious_transactions")
                .addTag("userId", userId)
                .addField("amount", amount)
                .time(Instant.parse(timestamp), WritePrecision.MS);
        client.getWriteApiBlocking().writePoint(bucket, org, point);
    }
}
