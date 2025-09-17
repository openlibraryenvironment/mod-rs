You'll need to have the `kubectl` context set to some sort of Kubernetes
cluster, I prefer KinD and k3d as they eschew VMs. To create a cluster that
forwards the necessary ports with k3d use something like:

```
k3d cluster create rsdev -p "9130:30130@server:0" -p "54321:30543@server:0" -p"29092:30092@server:0" -p"2181:32181@server:0"
```

Then you can spin up the necessary containers via the manifests (choosing FOLIO version as appropriate):

```
kubectl apply -R -f manifests
kubectl apply -R -f folio-modules-poppy
```

You'll need to wait until all the deployments are available, which you can check via:

```
kubectl get deployment -n reshare
```

While you're waiting you can get some port forwarding running to make Okapi and
Postgres available where the ReShare scripts/configs expect it (on localhost
and ports 9130 and 54321 respectively). Your k8s environment may have some way
to persist this or set it while creating the cluster (eg. you won't need this
step if you've used the k3d command above) or you can have a few terminals
running these:

```
kubectl port-forward -n reshare svc/postgres 54321:5432
kubectl port-forward -n reshare svc/okapi 9130:9130
kubectl port-forward -n reshare svc/reshare-kafka-cardinal 29092:29092
```

Once everything is up and forwarded you can create a tenant and enable the
modules we have manifests for:

```
ls folio-modules-poppy | ./enable-modules.sh
```

To create a superuser for now we can use an existing Perl script. NB. it needs
the JSON and UUID::Tiny modules which, on a Debian based Linux system, are also
available via system packges `libjson-perl` and `libuuid-tiny-perl`. It can be
found here:
https://raw.githubusercontent.com/folio-org/folio-install/master/runbooks/single-server/scripts/bootstrap-superuser.pl

```
perl ./bootstrap-superuser.pl
```

(You might consider changing the username and password in the above script.)

Finally, you'll need to update
`service/src/main/okapi/DeploymentDescriptor-template.json` in both
mod-directory and mod-rs to point at your workstation via `host.k3d.internal`
(or however you reach it via your k8s environment) rather than `10.0.2.2`. You
may also need to make that change in mod-rs'
`service/grails-app/conf/application.yml` as it explicitly lists that ip there.

At that point you should be able to use `register_and_enable.sh` and other
ReShare scripts to configure the dev environment per usual.
