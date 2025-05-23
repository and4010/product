package com.example.product.api

import android.util.Log
import androidx.annotation.MainThread
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * 管理API呼叫
 */
object ApiManager {

    private const val HEADER_APP_VERSION = "AppVersion"
    private const val HEADER_AUTHORIZATION = "Authorization"
    private const val TIMEOUT_SEC = 10L

    private val TAG by lazy { this::class.java.simpleName }

    /**
     * [pendingMap]的鎖，確保呼叫表的內容正確
     */
    private val mutex = Mutex()

    /**
     * API呼叫表
     */
    private val pendingMap = hashMapOf<String, Job>()

    /**
     * [okHttpClient]的讀寫鎖，確保在設定timeout時不會影響正在呼叫的API
     */
    private val readWriteLock = ReentrantReadWriteLock(true)
    private var okHttpClient = OkHttpClient.Builder().apply {
        connectTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
        readTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
        writeTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
    }.build()

    private val headers = Headers.Builder()
    private var tokenInvalidCallback: (() -> Unit)? = null

    init {

    }

    /**
     * 設定Authorization
     */
    fun setAuth(auth: String) {
        Log.d(TAG, "API AUTH: $auth")

        if (headers[HEADER_AUTHORIZATION] != "Bearer $auth") {
            if (auth.isEmpty()) {
                headers[HEADER_AUTHORIZATION] = ""
            }
            else {
                headers[HEADER_AUTHORIZATION] = "Bearer $auth"
            }
        }
    }


    fun removeHeader(key: String) {
        headers.removeAll(key)
    }
    /**
     * 設定Timeout
     */
    fun setTimeout(timeoutSec: Long) {
        readWriteLock.write {
            okHttpClient = OkHttpClient.Builder().apply {
                connectTimeout(timeoutSec, TimeUnit.SECONDS)
                readTimeout(timeoutSec, TimeUnit.SECONDS)
                writeTimeout(timeoutSec, TimeUnit.SECONDS)
            }.build()
        }
    }

    /**
     * 設定Token失效回調
     */
    fun setTokenInvalidCallback(@MainThread callback: () -> Unit) {
        tokenInvalidCallback = callback
    }

    /**
     * 呼叫API
     * @param startDelay 呼叫API前的延遲時間
     * @param isDebug 是否為測試API (不直接呼叫)
     * @param debugResult 自定義測試API的呼叫結果 (預設: 成功)，此結果僅在[isDebug]為true時有效
     */
    suspend fun <T : BaseApi.ApiResponse> BaseApi.call(
        startDelay: Long = 300,
        isDebug: Boolean = false,
        debugResult: CallBackResult = CallBackResult.Successful(),
        callback: Callback<T>) {
        check(getContentType() == BaseApi.ContentType.EMPTY) { "Only empty content type can be called." }

        val apiName = this.javaClass.simpleName

        try {
            val builder = Request.Builder()
                .headers(headers.build())
                .url(getApiPath())
                .post(byteArrayOf().toRequestBody(null))

            ApiHandler(this, builder.build(), startDelay, isDebug, debugResult, callback).execute()
        }
        catch (e: CancellationException) {
            Log.w(TAG, "API $apiName call cancel")
        }
        catch (e: Exception) {
            Log.e(TAG, "API $apiName call Exception: ${e.message}")
        }
    }

    /**
     * 呼叫API
     * @param request API參數
     * @param startDelay 呼叫API前的延遲時間
     * @param isDebug 是否為測試API (不直接呼叫)
     * @param debugResult 自訂測試API的呼叫結果 (預設: 成功)，此結果僅在[isDebug]為true時有效
     */
    suspend fun <T : BaseApi.ApiResponse> BaseApi.call(
        request: BaseApi.ApiRequest,
        startDelay: Long = 300,
        isDebug: Boolean = false,
        debugResult: CallBackResult = CallBackResult.Successful(),
        callback: Callback<T>) {
        val apiName = this.javaClass.simpleName

        try {
            var builder = Request.Builder().headers(headers.build())

            when (getContentType()) {
                BaseApi.ContentType.JSON -> {
                    val jsonString: String = request.toJson()
                    val body = jsonString.toRequestBody("application/json;charset=utf-8".toMediaTypeOrNull())
                    Log.d(TAG, "API $apiName BODY: $jsonString")
                    builder = builder.url(getApiPath()).post(body)
                }

                BaseApi.ContentType.FROM -> {}

                else                     -> {
                    builder = builder.url(getApiPath()).post(byteArrayOf().toRequestBody(null))
                }
            }

            ApiHandler(this, builder.build(), startDelay, isDebug, debugResult, callback).execute()
        }
        catch (e: CancellationException) {
            Log.w(TAG, "API $apiName call cancel")
        }
        catch (e: Exception) {
            Log.e(TAG, "API $apiName call Exception: ${e.message}")
        }
    }

    fun Request.printInfo(tag: String, name: String) =
        Log.d(tag,
            "$name REQUEST\nurl: [${url}]\nappVersion: [${headers[HEADER_APP_VERSION]}]\ncontent-type: [${body?.contentType()}]")

    fun BaseApi.ApiResponse.toJson(): String = Gson().toJson(this)

    /**
     * 處理API呼叫
     * @param request API參數
     * @param startDelay 呼叫API前的延遲時間
     * @param isDebug 是否為測試API (不直接呼叫)
     * @param debugResult 自訂測試API的呼叫結果 (預設: 成功)，此結果僅在[isDebug]為true時有效
     */
    private class ApiHandler<T : BaseApi.ApiResponse>(
        private val api: BaseApi,
        private val request: Request,
        private val startDelay: Long,
        private val isDebug: Boolean,
        private val debugResult: CallBackResult,
        private val callback: Callback<T>) {
        private val tag by lazy { "$TAG.${this::class.java.simpleName}" }

        /**
         * 執行API呼叫
         */
        suspend fun execute() = withContext(Dispatchers.IO) {
            val apiName = api.javaClass.simpleName

            request.printInfo(tag, "API $apiName")

            // 開始執行
            val job = launch {
                try {
                    delay(startDelay)

                    readWriteLock.read { okHttpClient.newCall(request).execute() }
                        .use { response ->
                            if (response.isSuccessful) {
                                Log.d(tag, "API $apiName RESPONSE SUCCESS: $response")
                                val bodyString = response.body?.string()
                                Log.d(tag, "API $apiName RESPONSE bodyString: $bodyString")
                                val result =
                                    Gson().fromJson<T>(bodyString, api.getResponseModel().java)
                                        .apply {
                                            rawBodyString = bodyString
                                        }
                                callback.isSuccessful(result)
                            } else {
                                Log.e(tag, "API $apiName RESPONSE FAIL: $response")
                                callback.isFail(null)
                            }
                        }
                } catch (e: CancellationException) {
                    Log.w(tag, "API $apiName RESPONSE Cancel")
                } catch (e: IOException) {
                    Log.e(tag, "API $apiName RESPONSE IOException: ${e.message}")
                    callback.isNetworkError()
                } catch (e: Exception) {
                    Log.e(tag, "API $apiName RESPONSE Exception: ${e.message}")
                    callback.isError(e.message ?: "")
                }
            }

            if (pendingMap.contains(apiName)) {
                // 若當前API已被呼叫，則取消前一次呼叫後再進行呼叫
                pendingMap[apiName]?.cancel()
            }

            mutex.withLock {
                pendingMap[apiName] = job
            }

            // 等待job完成
            job.join()

            if (job.isCancelled) {
                // 若當前呼叫已被取消，則等到另一個呼叫完畢後再返回
                while (pendingMap.contains(apiName)) {
                    delay(1000)
                }
            } else {
                // 呼叫完畢，從列表移除當前API，並重設Authorization和Timeout
                mutex.withLock {
                    pendingMap.remove(apiName)
                    if (okHttpClient.connectTimeoutMillis != TIMEOUT_SEC.toInt()) {
                        setTimeout(TIMEOUT_SEC)
                    }
                }
            }
        }
    }

    /**
     * 自訂API回調結果
     */
    sealed class CallBackResult {
        data class Successful(val resultMsg: String = "{\"result\":\"0000\",\"message\":\"success\"}") : CallBackResult()
        data class Fail(val code: String = "0000", val message: String = "") : CallBackResult() {
            fun getResultMsg() = "{\"result\":\"$code\",\"message\":\"$message\"}"
        }

        data object NetworkError : CallBackResult()
        data object Error : CallBackResult()
    }

    /**
     * API回調
     */
    interface Callback<T : BaseApi.ApiResponse> {
        /**
         * API呼叫成功
         */
        suspend fun isSuccessful(response: T)

        /**
         * API呼叫失敗
         */
        suspend fun isFail(response: T?)

        /**
         * 網路錯誤
         */
        suspend fun isNetworkError()

        /**
         * 其他錯誤
         */
        suspend fun isError(errorMsg: String)
    }
}