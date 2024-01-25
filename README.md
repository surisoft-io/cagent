
# cagent

CAPI Agent will listen for deployments on your cluster, and registers them on a given Consul instance.

To enable the agent you just need to install the helm chart on your K8s cluster:
```bash
$ helm install cagent ./cagent
```
The chart contains already everything you need to successfully run the agent:
* Service account
* Cluster Role with permissions to read Services, Deployments and Ingress information
* The binding of the Role to the Service Account.

You will need to provide a Consul instance accessible by the agent. Here are the environment values you can define yourself.
* namespace: Where the agent will run. (by default the agent will read all namespaces)
* consulHost: The consul (http://ip:8500)
* consulToken: If ACL's are enabled.
* labelsToFilter: cagent will only listen for services within this list of labels.

Example cagent of values:

```yaml
cagent:
  namespace: default
  consulHost: http://10.0.0.0:8500
  consulToken: 
  labelsToFilter: pet-service-group
  serviceAccountName: capi
```

Example of your deployment labels:
```yaml
- apiVersion: apps/v1
  kind: Deployment
  metadata:
    labels:
      service: sample-service
      pet-service-group: sample-service
    name: sample-service
```


