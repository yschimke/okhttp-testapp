# okhttp-testapp
OkHttp Test App

## Test Locally

```
$ ./gradlew installDebug
```

Edit [TestSetup](https://github.com/yschimke/okhttp-testapp/blob/master/app/src/main/kotlin/com/squareup/okhttptestapp/TestSetup.kt) to configure the client and create a test request

logs via 

```
$ adb logcat com.squareup.okhttptestapp
```
