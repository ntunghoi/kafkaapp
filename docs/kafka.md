# Documentation about Kafka components

## Kafka Setup

## Kafka Environment

In the first version, a local Kafka environment using Docker was setup and used.    

The YAML file [docker/docker-compose.yaml](../docker/docker-compose.yaml) defines the following components

- Kafka brokers (kafka-broker1, kafka-broker2, kafka-broker3)
- Schema registry
- KSQL DB server
- KSQL client

However, due to limited resources of my machine, some of the components failed to start properly. Then a remote environment running in Confluent cloud is used.

All the Kafka configuration settings are defined in application.yaml

- spring.kafka
- ksqldb-server

### Topics

The following two topics are defined in Kafka. Some other topics and tables in KSQL database were created based on these two topics.

- account_transactions
- user_accounts

Run the following commands to create required topics and update the retention period to infinity so that data will not be purged.

```bash
# Create topics
kafka-topics --create --topic account_transactions --bootstrap-server pkc-4nxnd.asia-east2.gcp.confluent.cloud:9092 --partitions 6 --replication-factor 1
kafka-topics --create --topic user_accounts --bootstrap-server pkc-4nxnd.asia-east2.gcp.confluent.cloud:9092 --partitions 6 --replication-factor 1

# Set the retention period to infinite
kafka-configs --bootstrap-server pkc-4nxnd.asia-east2.gcp.confluent.cloud:9092 --alter --add-config retention.ms=100 --topic test-topic
kafka-configs --bootstrap-server pkc-4nxnd.asia-east2.gcp.confluent.cloud:9092 --alter --delete-config retention.ms --topic test-topic 

# list the available topics to make sure topics are created
kafka-topics --list --bootstrap-server pkc-4nxnd.asia-east2.gcp.confluent.cloud:9092
```

#### Initial data

Fake data is generated using the script [generate-account-transactions.py](../scripts/generate-accuont-transactions.py) and specify the output filename (e.g. samples/init-data.csv) as a command line argument. If no argument is provided, a data file wil ge generated in the current directory. 

A configuration file [load-config.json](../load-data.json) defines the following fields
- dataFilePath - should be the command line argument of generate-account-transactions.py
- userAccountsTopicName - Name of the topic for user accounts data
- accountTransactionsTopicName - Name of topic for account transactions
- defaultUserAccountCreationDate - Timestamp of the user account record in the topic. This must be before the timestamp of the account transaction data.

```bash
python generate-account-transactions.py samples/init-data.csv

# Run the following command to load data from file samples/init-data.csv into the corresponding topics
./gradlew bootRunCli -Pargs="-i load-data.json" --stacktrace
```

### ksqlDB

Create the following streams and tables

```ksql
SET 'auto.offset.reset' = 'earliest';

CREATE OR REPLACE TABLE user_accounts_table (
    account_number VARCHAR PRIMARY KEY,
    user_id INT
) WITH (
    KAFKA_TOPIC = 'user_accounts',
    VALUE_FORMAT = 'JSON'
);

CREATE OR REPLACE STREAM account_transactions_stream (
    id VARCHAR,
    amount_with_currency VARCHAR,
    account_number VARCHAR, 
    value_date BIGINT,
    value_timestamp BIGINT,
    description VARCHAR
 ) WITH (
    KAFKA_TOPIC = 'account_transactions',
    PARTITIONS = 6,
    REPLICAS = 3,
    TIMESTAMP = 'value_timestamp',
    -- timestamp_format='yyyy-MM-dd HH:mm:ss.SSSSSS' -- not necessary if already in milliseconds
    VALUE_FORMAT = 'JSON'
 );
 
CREATE OR REPLACE STREAM user_account_transactions_stream WITH (PARTITIONS=6) 
AS SELECT
    t.id AS transaction_id,
    ua.user_id AS user_id,
    t.account_number AS account_number,
    t.amount_with_currency AS amount_with_currency,
    t.value_date AS value_date,
    t.value_timestamp AS value_timestamp,
    t.description AS description
FROM
    account_transactions_stream t
LEFT JOIN
    user_accounts_table ua ON t.account_number = ua.account_number
WHERE
    ua.user_id IS NOT NULL
PARTITION BY ua.user_id; 

CREATE TABLE transactions_by_user_value_date WITH (
    KAFKA_TOPIC = 'transactions_by_user_value_date',
    VALUE_FORMAT = 'JSON',
    KEY_FORMAT = 'AVRO'
) AS SELECT
    user_id,
    value_date,
    COLLECT_LIST(
        STRUCT(
            transaction_id := transaction_id,
            account_number := account_number,
            amount_with_currency := amount_with_currency,
            value_date := value_date,
            value_timestamp := value_timestamp,
            description := description
        )
    ) AS daily_transactions
FROM
    user_account_transactions_stream
GROUP BY
    user_id, value_date;
```