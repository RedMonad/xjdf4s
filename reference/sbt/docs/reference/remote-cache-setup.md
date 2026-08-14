Remote cache setup
==================

This page covers remote caching setup. See [Caching](../concepts/caching.html) for general explanation of the caching system.

gRPC remote cache
-----------------

While there might be multiple remote cache store implemention in the future, sbt 2.0 ships with a gRPC client that is compatible with the Bazel remote cache backends. To configure sbt 2.x, add the following to `project/plugins.sbt`

```scala
addRemoteCachePlugin
```

There are many Bazel remote cache backends, both open source and commercial solutions. While this page documents is not an exhaustive list of all Bazel remote cache implementations, hopefully it shows how sbt 2.x can be set up for wide array of them.

### Authentication

There are a few flavors of [gRPC authentication](https://grpc.io/docs/guides/auth/), and Bazel remote cache backends use various kind of them:

1. Unauthenticated. Useful for testing.
2. Default TLS/SSL.
3. TLS/SSL with custom server certificate.
4. TTL/SSL with custom server and client certificate, mTLS.
5. Default TLS/SSL with API token header.

### bazel-remote without authentication

You can grab the code from [buchgr/bazel-remote][bazel-remote] and run it on a laptop using Bazel:

```bash
bazel run :bazel-remote  -- --max_size 5 --dir $HOME/work/bazel-remote/temp \
  --http_address localhost:8000 \
  --grpc_address localhost:2024
```

To configure sbt 2.x, add the following to `project/plugins.sbt`

```scala
addRemoteCachePlugin
```

and append the following to `build.sbt`:

```scala
Global / remoteCache := Some(uri("grpc://localhost:2024"))
```

### bazel-remote with mTLS

In a real environment, mTLS can ensure that the transport is encrypted and mutually authenticated. bazel-remote can be started with something like the follows:

```bash
bazel run :bazel-remote  -- --max_size 5 --dir $HOME/work/bazel-remote/temp \
  --http_address localhost:8000 \
  --grpc_address localhost:2024 \
  --tls_ca_file /tmp/sslcert/ca.crt \
  --tls_cert_file /tmp/sslcert/server.crt \
  --tls_key_file /tmp/sslcert/server.pem
```

sbt 2.x setting would look like this in this scenario:

```scala
Global / remoteCache := Some(uri("grpcs://localhost:2024"))
Global / remoteCacheTlsCertificate := Some(file("/tmp/sslcert/ca.crt"))
Global / remoteCacheTlsClientCertificate := Some(file("/tmp/sslcert/client.crt"))
Global / remoteCacheTlsClientKey := Some(file("/tmp/sslcert/client.pem"))
```

Note the `grpcs://`, as opposed to `grpc://`.

### EngFlow

[EngFlow GmbH][engflow] is a build solution company founded in 2020 by core members of Bazel team, providing build analytics and remote execution backend for Bazel, which includes remote cache.

After signing up for trial on <https://my.engflow.com/>, the page instructs you to start a trial cluster using a docker. If you followed the instruction, this should start a remote cache service on port 8080. The sbt 2.x configuration would look like this for the trial cluster:

```scala
Global / remoteCache := Some(uri("grpc://localhost:8080"))
```

### BuildBuddy

[BuildBuddy][buildbuddy] is a build solution company founded by ex-Google engineers, providing build analytics and remote execution backend for Bazel. It's also available open source as [buildbuddy-io/buildbuddy][buildbuddy-oss].

After signing up, BuildBuddy Personal plan lets you use BuildBuddy across the Internet.

1. From <https://app.buildbuddy.io/>, go to Settings, and change the Organization URL to `<something>.buildbuddy.io`.
2. Next, go to Quickstart and take note of the URLs and `--remote_headers`.
3. Create a file called `$HOME/.sbt/buildbuddy_credential.txt` and put in the API key:

```
x-buildbuddy-api-key=*******
```

The sbt 2.x configuration would look like this:

```scala
Global / remoteCache := Some(uri("grpcs://something.buildbuddy.io"))
Global / remoteCacheHeaders += IO.read(BuildPaths.defaultGlobalBase / "buildbuddy_credential.txt").trim
```

### NativeLink

[NativeLink][nativelink] is an open-source Bazel remote execution backend implementated in Rust with emphasis on performance. As of June 2024, there's NativeLink Cloud in beta.

1. From <https://app.nativelink.com/>, go to Quickstart and take note of the URLs and `--remote_header`.
2. Create a file called `$HOME/.sbt/nativelink_credential.txt` and put in the API key:

```
x-nativelink-api-key=*******
```

The sbt 2.x configuration would look like this:

```scala
Global / remoteCache := Some(uri("grpcs://something.build-faster.nativelink.net"))
Global / remoteCacheHeaders += IO.read(BuildPaths.defaultGlobalBase / "nativelink_credential.txt").trim
```


  [remote_execution.proto]: https://github.com/bazelbuild/remote-apis/blob/main/build/bazel/remote/execution/v2/remote_execution.proto
  [bazel-remote]: https://github.com/buchgr/bazel-remote
  [engflow]: https://www.engflow.com/
  [buildbuddy]: https://www.buildbuddy.io/
  [buildbuddy-oss]: https://github.com/buildbuddy-io/buildbuddy
  [nativelink]: https://docs.nativelink.dev/
  [develocity]: https://gradle.com/gradle-enterprise-solutions/bazel-build-system/
  [buildfarm]: https://github.com/bazelbuild/bazel-buildfarm
  [shaded-remoteapis-java]: https://repo1.maven.org/maven2/com/eed3si9n/remoteapis/shaded/shaded-remoteapis-java/2.3.0-M1-52317e00d8d4c37fa778c628485d220fb68a8d08/
  [buildbarn]: https://github.com/buildbarn
