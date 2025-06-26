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

### Initial data

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

