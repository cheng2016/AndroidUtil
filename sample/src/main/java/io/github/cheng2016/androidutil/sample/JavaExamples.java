package io.github.cheng2016.androidutil.sample;

import android.content.Context;

import io.github.cheng2016.androidutil.core.AndroidUtilLogger;
import io.github.cheng2016.androidutil.core.CrashHandler;
import io.github.cheng2016.androidutil.core.DigestUtils;
import io.github.cheng2016.androidutil.core.Preferences;
import io.github.cheng2016.androidutil.network.AndroidHttpClient;
import io.github.cheng2016.androidutil.network.CancellableCall;
import io.github.cheng2016.androidutil.permissions.PermissionCallback;
import io.github.cheng2016.androidutil.permissions.PermissionRequester;

/** Compile-checked Java interop examples referenced by the documentation. */
public final class JavaExamples {
    private JavaExamples() {
    }

    public static String useCore(Context context) {
        AndroidUtilLogger.i("JavaSample", "Java interop works");
        CrashHandler.install(context);
        Preferences preferences = Preferences.of(context);
        preferences.putBoolean("java_sample_seen", true);
        return DigestUtils.sha256Hex("AndroidUtil");
    }

    public static CancellableCall get(
            AndroidHttpClient client,
            String url,
            io.github.cheng2016.androidutil.network.HttpCallback callback
    ) {
        return client.getAsync(url, callback);
    }

    public static void request(
            PermissionRequester requester,
            String[] permissions,
            PermissionCallback callback
    ) {
        requester.request(permissions, callback);
    }
}
