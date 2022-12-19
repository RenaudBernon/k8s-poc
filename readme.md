# Kubernetes Scaler

## Build & Deploy on MiniKube

### Prerequisites:

- Minikube installed and running
- Docker installed and running

### Prepare your environment:

1. Link your docker environment to minikube
   ```bash
   eval $(minikube -p minikube docker-env)
   ```

2. Builder the application
   ```bash
   mvn clean install -DskipTests
   ```

3. Build the docker image (`--platform linux/x86_64` is only necessary on m1 macbooks)
   ```bash
   docker build --platform linux/x86_64 . --tag=k8s-poc:latest -t k8s-poc
   ```

### Deploying to Minikube

First create the namespace

```bash
kubectl apply -f kubernetes/namespace.yml
```

Then create the other kubernetes resources

```bash
kubectl apply -f kubernetes
```  

This will create following resources in the namespace:

- A Deployment with 3 replicated pods
- A NodePort service which matches the created pods
- A Role "deployment-scaler" which is allowed to list and update deployments in the namespace
- A RoleBinding which links the Role to the "poc-sa" ServiceAccount
- A ServiceAccount which will be used by all pods in the Namespace

Last but not least, get the [NodePort access](https://minikube.sigs.k8s.io/docs/handbook/accessing/#nodeport-access) in
minikube.

```bash
minikube service k8s-poc --url -n poc
```
