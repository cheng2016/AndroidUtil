package io.github.cheng2016.androidutil.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.cheng2016.androidutil.core.AndroidUtilLogger
import io.github.cheng2016.androidutil.core.DigestUtils
import io.github.cheng2016.androidutil.core.Preferences
import io.github.cheng2016.androidutil.network.AndroidHttpClient
import io.github.cheng2016.androidutil.network.HttpResult
import io.github.cheng2016.androidutil.permissions.PermissionGroups
import io.github.cheng2016.androidutil.permissions.PermissionRequester
import io.github.cheng2016.androidutil.sample.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionRequester: PermissionRequester
    private val httpClient = AndroidHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AndroidUtilLogger.init(
            applicationContext,
            AndroidUtilLogger.Config(
                minLevel = AndroidUtilLogger.Level.DEBUG,
                fileEnabled = true,
            ),
        )
        permissionRequester = PermissionRequester.register(this)

        binding.writeLog.setOnClickListener {
            AndroidUtilLogger.i("Sample", "Private log written from the sample app")
            AndroidUtilLogger.flush()
            binding.result.text = AndroidUtilLogger.logDirectory(this).absolutePath
        }
        binding.requestPermission.setOnClickListener { requestNotifications() }
        binding.runRequest.setOnClickListener { runRequest() }
        binding.runPost.setOnClickListener { runPostRequest() }
        binding.runUtilities.setOnClickListener { runCoreUtilities() }
    }

    private fun requestNotifications() {
        val permissions = PermissionGroups.notifications()
        if (permissions.isEmpty()) {
            binding.result.setText(R.string.permission_not_required)
            return
        }
        permissionRequester.request(*permissions) { result ->
            binding.result.text =
                getString(
                    R.string.permission_result,
                    result.granted,
                    result.denied,
                    result.permanentlyDenied,
                )
        }
    }

    private fun runRequest() {
        val url = "https://example.com"
        binding.result.text = getString(R.string.requesting_url, url)
        lifecycleScope.launch {
            binding.result.text = describe(httpClient.get(url))
        }
    }

    private fun runPostRequest() {
        val url = "https://httpbin.org/post"
        binding.result.text = getString(R.string.requesting_url, url)
        lifecycleScope.launch {
            binding.result.text =
                describe(httpClient.postJson(url, """{"source":"AndroidUtil"}"""))
        }
    }

    private fun describe(result: HttpResult): String =
        when (result) {
            is HttpResult.Success ->
                getString(R.string.http_result, result.code, result.body.take(120))
            is HttpResult.HttpFailure ->
                getString(R.string.http_result, result.code, result.body.take(120))
            is HttpResult.NetworkFailure ->
                getString(R.string.network_error, result.cause.message.orEmpty())
            HttpResult.Cancelled -> getString(R.string.request_cancelled)
        }

    private fun runCoreUtilities() {
        val preferences = Preferences.of(this)
        val count = preferences.getInt("sample_runs") + 1
        preferences.putInt("sample_runs", count)
        val digest = DigestUtils.sha256Hex("AndroidUtil")
        binding.result.text = getString(R.string.core_result, count, digest)
    }

    override fun onDestroy() {
        if (isFinishing) {
            AndroidUtilLogger.close()
        }
        super.onDestroy()
    }
}
