package io.poc.k8spoc.api;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.openapi.models.V1DeploymentSpec;
import io.kubernetes.client.util.Config;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController("/")
public class KubernetesController {
    private static final String NAMESPACE = "poc";
    private static final String DEPLOYMENT_NAME = "k8s-poc";

    private final AppsV1Api appsV1Api;


    public KubernetesController() throws IOException {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        appsV1Api = new AppsV1Api();
    }

    @GetMapping("scale")
    @ResponseBody
    public String scale(@RequestParam(value = "amount") Integer numberOfReplicas) {
        V1Deployment deployment = getDeployment();
        scaleDeployment(deployment, numberOfReplicas);
        return String.format("Scaled to %d pods", numberOfReplicas);
    }

    @GetMapping("scale-down")
    public String scaleDown() {
        V1Deployment deployment = getDeployment();
        Integer amountOfReplicas = deployment.getSpec().getReplicas();
        scaleDeployment(deployment, --amountOfReplicas);
        return String.format("Scaled down to %d replicas", amountOfReplicas);
    }

    @GetMapping("scale-up")
    public String scaleUp() {
        V1Deployment deployment = getDeployment();
        Integer amountOfReplicas = deployment.getSpec().getReplicas();
        scaleDeployment(deployment, ++amountOfReplicas);
        return String.format("Scaled up to %d replicas", amountOfReplicas);
    }

    private V1Deployment getDeployment() {
        V1DeploymentList listNamespacedDeployment;
        try {
            listNamespacedDeployment = appsV1Api.listNamespacedDeployment(NAMESPACE, null, null, null, null, null, null, null, null, null, Boolean.FALSE);
        } catch (ApiException e) {
            throw new RuntimeException("Failed to list deployments in namespace");
        }

        List<V1Deployment> appsV1DeploymentItems = listNamespacedDeployment.getItems();
        return appsV1DeploymentItems.stream()
                .filter((V1Deployment deployment) -> deployment.getMetadata().getName().equals(DEPLOYMENT_NAME))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No deployments in namespace matched name"));
    }

    private void scaleDeployment(V1Deployment deployment, int numberOfReplicas) {
        try {
            V1DeploymentSpec deploymentSpec = deployment.getSpec();
            deploymentSpec.setReplicas(numberOfReplicas);
            V1Deployment updatedDeployment = deployment.spec(deploymentSpec);

            appsV1Api.replaceNamespacedDeployment(
                    deployment.getMetadata().getName(),
                    NAMESPACE,
                    updatedDeployment,
                    null,
                    null,
                    null,
                    null);

        } catch (ApiException e) {
            throw new RuntimeException("Failed to replace deployment");
        }
    }
}
