package com.ntunghoi.kafkaapp.components;

import io.confluent.ksql.api.client.Client;
import io.confluent.ksql.api.client.ClientOptions;
import io.confluent.ksql.api.client.Row;
import io.confluent.ksql.api.client.StreamedQueryResult;
import jakarta.annotation.PostConstruct;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class KsqlClient<T> {
    private static final Map<String, Object> queryProperties = Map.of("auto.offset.reset", "earliest");

    private static final Logger logger = LoggerFactory.getLogger(KsqlClient.class);
    @Value("${ksqlDb-server.host}")
    private String ksqlDbServerHost;
    @Value("${ksqlDb-server.port}")
    private String ksqlDbServerPort;
    @Value("${ksqlDb-server.api.key}")
    private String ksqlDbServerApiKey;
    @Value("${ksqlDb-server.api.secret}")
    private String ksqlDbServerApiSecret;
    private ClientOptions ksqlClientOptions;

    public interface DataLoaderHelper<T> {
        String getQuery();

        boolean parse(Row raw) throws Exception;

        void onError(Throwable throwable);

        List<T> getItems();
    }

    public static class DataLoader<T> implements Subscriber<Row> {
        private int index;
        private Subscription subscription;
        private final CompletableFuture<List<T>> completableFuture;
        private final DataLoaderHelper<T> helper;

        public DataLoader(DataLoaderHelper<T> helper, CompletableFuture<List<T>> completableFuture) {
            this.helper = helper;
            this.completableFuture = completableFuture;
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            logger.info("onSubscribe");
            this.subscription = subscription;
            this.index = 0;
            subscription.request(1);
        }

        @Override
        public void onNext(Row row) {
            boolean isDone = false;
            try {
                isDone = helper.parse(row);
            } catch (Exception exception) {
                helper.onError(new Exception(
                        String.format("Error in processing data in row %d: %s", index, row),
                        exception
                ));
            } finally {
                if(isDone) {
                    subscription.cancel();
                    onComplete();
                } else {
                    index++;
                    subscription.request(1);
                }
            }
        }

        @Override
        public void onComplete() {
            this.completableFuture.complete(helper.getItems());
        }

        @Override
        public void onError(Throwable throwable) {
            this.helper.onError(throwable);
        }
    }

    @PostConstruct
    private void init() {
        ksqlClientOptions = ClientOptions.create()
                .setHost(ksqlDbServerHost)
                .setPort(Integer.parseInt(ksqlDbServerPort))
                .setUseAlpn(true)
                .setUseTls(true)
                .setBasicAuthCredentials(ksqlDbServerApiKey, ksqlDbServerApiSecret);
    }

    public List<T> executeQuery(DataLoaderHelper<T> dataLoaderHelper) throws Exception {
        try (Client ksqlClient = Client.create(ksqlClientOptions)) {
            System.out.println(dataLoaderHelper.getQuery());
            StreamedQueryResult streamedQueryResult = ksqlClient.streamQuery(dataLoaderHelper.getQuery(), queryProperties).get();
            CompletableFuture<List<T>> future = new CompletableFuture<>();
            streamedQueryResult.subscribe(new DataLoader<>(dataLoaderHelper, future));
            List<T> result = future.get(10, TimeUnit.SECONDS);
            logger.info("streamQueryResult.isComplete: {}", streamedQueryResult.isComplete());
            if(!streamedQueryResult.isComplete()) {
                try {
                    CompletableFuture<Void> t = ksqlClient.terminatePushQuery(streamedQueryResult.queryID());
                    t.get();
                } catch(Exception e) {
                    // ignore exception here
                }
            }
            return result;
        } catch (TimeoutException timeoutException) {
            throw new Exception("Timeout in retrieving data from KsqlDB", timeoutException);
        } catch (Exception exception) {
            throw new Exception("Unknown exception in retrieving data", exception);
        }
    }

}
