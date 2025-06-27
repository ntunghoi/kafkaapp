# Build docker image

```bash
./gradlew bootBuildImage

docker run -p 8085:8085 kafkaapp:1.0-SNAPSHOT

# load docker image into Kubernetes context
minikube image load kuberneteslab:0.0.1-SNAPSHOT
```