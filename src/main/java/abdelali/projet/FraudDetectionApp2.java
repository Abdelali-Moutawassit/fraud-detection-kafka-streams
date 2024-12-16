package abdelali.projet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

public class FraudDetectionApp2 {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "fraud-detection-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> transactions = builder.stream("transactions-input");

        KStream<String, String> suspiciousTransactions = transactions.filter((key, value) -> {
            try {
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(value, JsonObject.class);

                if (!jsonObject.has("userId") || !jsonObject.has("amount") || !jsonObject.has("timestamp")) {
                    System.err.println("Message malformé ignoré : " + value);
                    return false;
                }

                return jsonObject.get("amount").getAsDouble() > 10000;
            } catch (Exception e) {
                return false;
            }
        });
        suspiciousTransactions.foreach((key, value) -> {
            try {
                Gson gson = new Gson();
                JsonObject json = gson.fromJson(value, JsonObject.class);

                String userId = json.get("userId").getAsString();
                double amount = json.get("amount").getAsDouble();
                String timestamp = json.get("timestamp").getAsString();

                InfluxDBWriter.writeTransaction(userId, amount, timestamp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        suspiciousTransactions.to("fraud-alerts", Produced.with(Serdes.String(), Serdes.String()));

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
    }
}

