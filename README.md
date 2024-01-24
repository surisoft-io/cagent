
# cagent

Create a Cluster Role for cagent to be allowed to list services on a given cluster
```yaml
kind: ClusterRole
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  namespace: default
  name: service-reader
rules:
- apiGroups: [""] # "" indicates the core API group
  resources: ["services"]
  verbs: ["get", "watch", "list"]
```

Create the role:
```bash
$ kubectl apply -f cluster-role.yaml
```

Create a service account:
```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: capi
```

Apply:
```bash
$ kubectl apply -f service-account.yaml
```

Bind the Cluster role with the service account.
```bash
$ kubectl create clusterrolebinding service-reader-pod \
  --clusterrole=service-reader  \
  --serviceaccount=default:capi
```

