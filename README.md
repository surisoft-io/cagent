
# cagent

CAPI Agent will listen for Ingresses and Services on your cluster, and registers them on a given Consul instance.

To enable the agent you just need to install the helm chart on your K8s cluster:
```bash
$ helm install cagent ./cagent
```
The chart contains already everything you need to successfully run the agent:
* Service account
* Cluster Role with permissions to read Services, Deployments and Ingress information
* The binding of the Role to the Service Account.

You will need to provide a Consul instance accessible by the agent. Here are the environment values you can define yourself.
* namespace: Where the agent will run. (CAGENT will only discover intresses and services within the same namespace)
* consulHost: The consul (http://ip:8500)
* consulToken: If ACL's are enabled.

Example of an ingress discoverable by this agent:
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  namespace: ##same namespace as cagent
  annotations:
    capi.meta.group: dev
    capi.meta.ingress: ingress.local:8080
    capi.meta.secured: 'true'
    capi.meta.scheme: https
    capi.meta.instance: internal
  name: sample-service
spec:
  rules:
  - host: ingress.local
    http:
      paths:
      - path: / 
        pathType: Prefix
        backend:
          service:
            name:  sample-service
            port: 
              number: 8082
```

Example cagent of values:

```yaml
cagent:
  namespace: default
  consulHost: http://10.0.0.0:8500
  consulToken: 
  serviceAccountName: capi
```



