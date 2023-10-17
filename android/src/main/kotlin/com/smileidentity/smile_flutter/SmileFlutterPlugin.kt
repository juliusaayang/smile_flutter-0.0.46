package com.smileidentity.smile_flutter

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleEventObserver
import com.rnsmileid.SIDUtil
import com.smileid.smileidui.CaptureType
import com.smileid.smileidui.IntentHelper.*
import com.smileid.smileidui.SIDCaptureManager
import com.smileid.smileidui.SIDIDCaptureConfig
import com.smileid.smileidui.SIDSelfieCaptureConfig
import com.smileidentity.libsmileid.SIDInfosManager
import com.smileidentity.libsmileid.core.*
import com.smileidentity.libsmileid.core.consent.ConsentActivity
import com.smileidentity.libsmileid.core.consent.util.BVNConsentError
import com.smileidentity.libsmileid.core.consent.util.SIDConsentConfig
import com.smileidentity.libsmileid.model.GeoInfos
import com.smileidentity.libsmileid.model.SIDMetadata
import com.smileidentity.libsmileid.model.SIDNetData
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.lifecycle.HiddenLifecycleReference
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener
import org.json.JSONObject

/** SmileFlutterPlugin */
class SmileFlutterPlugin : FlutterPlugin, MethodCallHandler, ActivityAware,
    PluginRegistry.ActivityResultListener, RequestPermissionsResultListener {

    companion object {
        const val SCREEN_TITLE_STYLE = "screen_title_style"
        const val IS_FULLSCREEN = "is_fullscreen"

        const val CAPTURE_TITLE_TEXT = "capture_title_text"

        const val OVERLAY_COLOR = "overlay_color"
        const val OVERLAY_ALPHA = "overlay_alpha"
        const val OVERLAY_THICKNESS = "overlay_thickness"
        const val OVERLAY_DOTTED = "overlay_dotted"
        const val OVERLAY_WIDTH = "overlay_width"
        const val OVERLAY_HEIGHT = "overlay_height"
        const val IS_WHITE_LABELLED = "is_white_labelled"


        const val CAPTURING_PROGRESS_COLOR = "capturing_progress_color"
        const val CAPTURED_PROGRESS_COLOR = "captured_progress_color"

        const val PROMPT_DEFAULT_TEXT = "prompt_default_text"
        const val PROMPT_BLURRY_TEXT = "prompt_blurry_text"
        const val PROMPT_CAPTURING_TEXT = "prompt_capturing_text"
        const val PROMPT_DO_SMILE_TEXT = "prompt_do_smile_text"
        const val PROMPT_COMPATIBILITY_MODE_TEXT =
            "prompt_compatibility_mode_text"
        const val PROMPT_FACE_NOT_FOUND_TEXT = "prompt_face_not_found_text"
        const val PROMPT_FACE_TOO_CLOSE_TEXT = "prompt_face_too_close_text"
        const val PROMPT_IDLE_TEXT = "prompt_idle_text"
        const val PROMPT_MOVE_CLOSER_TEXT = "prompt_move_closer_text"
        const val PROMPT_TOO_DARK_TEXT = "prompt_too_dark_text"

        const val CAPTURE_PROMPT_STYLE = "capture_prompt_style"

        const val CAPTURE_TIP_TEXT = "capture_tip_text"
        const val CAPTURE_TIP_STYLE = "capture_tip_style"

        const val REVIEW_TITLE_TEXT = "review_title_text"

        const val REVIEW_PROMPT_TEXT = "review_prompt_text"
        const val REVIEW_PROMPT_STYLE = "review_prompt_style"

        const val REVIEW_TIP_TEXT = "review_tip_text"
        const val REVIEW_TIP_STYLE = "review_tip_style"

        const val REVIEW_CONFIRM_TEXT = "review_confirm_text"
        const val REVIEW_CONFIRM_COLOR = "review_confirm_color"
        const val REVIEW_CONFIRM_STYLE = "review_confirm_style"

        const val REVIEW_RETAKE_TEXT = "review_retake_text"
        const val REVIEW_RETAKE_COLOR = "review_retake_color"
        const val REVIEW_RETAKE_STYLE = "review_retake_style"
        const val ID_CAPTURE_SIDE = "id_capture_side"
        const val ID_CAPTURE_ORIENTATION = "id_capture_orientation"

        const val ID_CAPTURED_BLURRY_MESSAGE = "id_captured_blurry_message"
        const val ID_CAPTURED_DARK_MESSAGE = "id_captured_dark_message"

        const val CARD_TYPE = "card_type"
        const val PROMPT_ID_CAPTURE_BLURRY = "id_prompt_blurry"
        const val PROMPT_ID_DETECTING_FACE = "id_prompt_detecting_face"
        const val PROMPT_ID_FACE_DETECTED = "id_prompt_face_detected"
        const val PROMPT_ID_FIT_ID = "id_prompt_fit_id"
        const val PROMPT_ID_FLASH_MISSING = "id_prompt_flash_missing"
        const val PROMPT_ID_INSUFFICIENT_LIGHT = "id_prompt_insufficient_light"
        const val PROMPT_ID_LOADING = "id_prompt_loading"
        const val PROMPT_ID_NO_FACE_DETECTED = "id_prompt_no_face_detected"
        const val PROMPT_ID_BACK_ID = "id_prompt_back_id"
        const val PROMPT_ID_CAPTURED_BLURRY = "id_prompt_captured_blurry"
        const val PROMPT_ID_CAPTURED_DARK = "id_prompt_captured_dark"
    }

    private val uploadListenerEventChannel = "smile_id/upload_listener"
    private val completeListenerEventChannel = "smile_id/complete_listener"

    private val PERMISSION_ALL = 1

    private val SMILE_ID_CARD_REQUEST_CODE = 777
    private val PERMISSION_SELFIE_REQUEST_CODE = 778
    private val PERMISSION_SELFIE_IDCARD_REQUEST_CODE = 779
    private val PERMISSION_IDCARD_REQUEST_CODE = 780
    private val USER_CONSENT_REQUEST_CODE = 781
    private val USER_BVN_CONSENT_REQUEST_CODE = 782
    val SID_RESULT_CODE = "SID_RESULT_CODE"
    val SID_RESULT_MESSAGE = "SID_RESULT_MESSAGE"
    val SID_RESULT_TAG = "SID_RESULT_TAG"

    val mainIdInfo =
        listOf(
            "first_name",
            "last_name",
            "middle_name",
            "country",
            "id_type",
            "id_number",
            "email",
            "allow_re_enroll",
            "use_enrolled_image"
        )
    val mainPartnerParams = listOf("user_id", "job_id")
    val permissionError = "permissionError"
    val submitError = "submitError"

    val permissionErrorMessage =
        "Does not have required permissions to run this method"

    private var PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA
    )

    private lateinit var activity: Activity
    private lateinit var activityBinding: ActivityPluginBinding
    private var currentConfigMap: HashMap<String, String>? = null


    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var currentResult: Result
    private var currentTag: String? = ""

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {

        channel =
            MethodChannel(flutterPluginBinding.binaryMessenger, "smile_flutter")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(
        @NonNull call: MethodCall,
        @NonNull result: Result
    ) {
        call.argument<String>("tag").also { currentTag = it }
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
                return
            }
            "captureSelfie" -> {
                currentResult = result
                currentConfigMap =
                    call.argument<HashMap<String, String>>("config")
                call.argument<Boolean>("autoHandlePermissions").also {
                    if (ActivityCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        if (it == false) {
                            result.error(
                                permissionError,
                                "Camera permissions not granted",
                                null
                            )
                            return
                        } else {
                            ActivityCompat.requestPermissions(
                                activity,
                                arrayOf(
                                    Manifest.permission.CAMERA
                                ),
                                PERMISSION_SELFIE_REQUEST_CODE
                            )
                            return
                        }
                    }
                    smileCapture(
                        call.argument<String>("tag"),
                        CaptureType.SELFIE,
                        call.argument<HashMap<String, String>>("config")
                    )
                }
            }
            "captureIDCard" -> {
                currentResult = result
                currentConfigMap =
                    call.argument<HashMap<String, String>>("config")
                call.argument<Boolean>("autoHandlePermissions").also {
                    if (ActivityCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        if (it == false) {
                            result.error(
                                permissionError,
                                "Camera permissions not granted",
                                null
                            )
                            return
                        } else {
                            ActivityCompat.requestPermissions(
                                activity,
                                arrayOf(
                                    Manifest.permission.CAMERA
                                ),
                                PERMISSION_IDCARD_REQUEST_CODE
                            )
                            return
                        }
                    }
                    smileCapture(
                        call.argument<String>("tag"),
                        CaptureType.ID_CAPTURE,
                        call.argument<HashMap<String, String>>("config")
                    )
                }
            }
            "captureSelfieAndIDCard" -> {
                currentResult = result
                currentConfigMap =
                    call.argument<HashMap<String, String>>("config")
                call.argument<Boolean>("autoHandlePermissions").also {
                    if (ActivityCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        if (it == false) {
                            result.error(
                                permissionError,
                                "Camera permissions not granted",
                                null
                            )
                            return
                        } else {
                            ActivityCompat.requestPermissions(
                                activity,
                                arrayOf(
                                    Manifest.permission.CAMERA
                                ),
                                PERMISSION_SELFIE_IDCARD_REQUEST_CODE
                            )
                            return
                        }
                    }
                    smileCapture(
                        call.argument<String>("tag"),
                        CaptureType.SELFIE_AND_ID_CAPTURE,
                        call.argument<HashMap<String, String>>("config")
                    )
                }
            }
            "showConsentScreen" -> {
                currentResult = result
                showConsent(
                    call.argument<String>("tag"),
                    call.argument<String>("partnerLogo"),
                    call.argument<String>("appBundleId"),
                    call.argument<String>("partnerName"),
                    call.argument<String>("privacyPolicyUrl"),
                    result
                )
            }
            "showBVNConsent" -> {
                currentResult = result
                showBVNConsent(
                    call.argument<String>("tag"),
                    call.argument<String>("partnerLogo"),
                    call.argument<String>("appBundleId"),
                    call.argument<String>("partnerName"),
                    call.argument<String>("privacyPolicyUrl"),
                    call.argument<Boolean>("isProduction"),
                    result
                )
            }
            "submitJob" -> {
                submitJob(
                    call.argument<String>("tag"),
                    call.argument<Int>("jobType"),
                    call.argument<Boolean>("isProduction"),
                    call.argument<String>("callBackUrl"),
                    call.argument<HashMap<String, String>>("partnerParams"),
                    call.argument<HashMap<String, String>>("idInfo"),
                    call.argument<HashMap<String, String>>("geoInfo"),
                    result
                )
                return
            }
            "getImagesForTag" -> {
                getImagesForTag(
                    call.argument<String>("tag"),
                    result
                )
                return
            }
            "clearTag" -> {
                clearTag(
                    call.argument<String>("tag"),
                    result
                )
                return
            }
            "getCurrentTags" -> {
                getCurrentTags(
                    result
                )
                return
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }


    //smile id

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activityBinding = binding
        activity = binding.activity
        binding.addRequestPermissionsResultListener(this);
        binding.addActivityResultListener(this);
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activityBinding.removeActivityResultListener(this)
        activityBinding.removeRequestPermissionsResultListener(this)
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onDetachedFromActivity() {
        activityBinding.removeRequestPermissionsResultListener(this);
        activityBinding.removeActivityResultListener(this);
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Boolean {
        val map: HashMap<String, Any> = HashMap()
        if ((requestCode == 1000) || (requestCode == 1001) || (requestCode ==
                    1002) || (requestCode == 1003)
        ) {
            when (requestCode) {
                1000 -> {
                    data?.extras?.getInt(SELFIE_REQUEST_RESULT)
                        ?.let { x -> map.put(SID_RESULT_CODE, x) }
                }
                1001 -> {
                    data?.extras?.getInt(ID_REQUEST_RESULT)
                        ?.let { x -> map.put(SID_RESULT_CODE, x) }
                }
                1002 -> {
                    data?.extras?.getInt(SELFIE_REQUEST_RESULT)
                        ?.let { x -> map.put(SID_RESULT_CODE, x) }
                }
                1003 -> {
                    data?.extras?.getInt(SELFIE_REQUEST_RESULT)
                        ?.let { x -> map.put(SID_RESULT_CODE, x) }
                }
            }
            data?.extras?.getString(SMILE_REQUEST_RESULT_TAG)
                ?.let { y -> map.put(SID_RESULT_TAG, y) }

            try {
                currentResult.success(map)
            } catch (e: Exception) {
                Log.e("SID", "Error whilst delivering Smile results")
            }
            return true
        }
        if (requestCode == USER_CONSENT_REQUEST_CODE) {
            val map: HashMap<String, Any> = HashMap()
            val tag = data?.extras?.getString(SMILE_REQUEST_RESULT_TAG)
            tag?.let {
                map.put(SID_RESULT_TAG, it)
            }
            if (resultCode == Activity.RESULT_OK) {
                map.put(SID_RESULT_CODE, 1)
            } else {
                map.put(SID_RESULT_CODE, 0)
            }
            currentResult.success(map)
        }
        if(requestCode == USER_BVN_CONSENT_REQUEST_CODE){
            val map: HashMap<Any?, Any?> = HashMap()
            data?.extras?.let {
                for (key in it.keySet()) {
                    if(it.get(key) is BVNConsentError){
                        map.put(key,  (it.get(key) as BVNConsentError).errorCode)
                    }else{
                        map.put(key, it.get(key))
                    }
                }
            }
            if (resultCode == Activity.RESULT_OK) {
                map.put(SID_RESULT_CODE, 1)
            } else {
                map.put(SID_RESULT_CODE, 0)
            }

            // PARTNER_LOGO is a bitmap, which Flutter doesn't know how to serialize
            currentResult.success(map.filter { it.key != "PARTNER_LOGO" })
        }
        return false
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        if (currentTag == null) {
            return true
        }

        when (requestCode) {
            PERMISSION_SELFIE_REQUEST_CODE -> {
                smileCapture(currentTag, CaptureType.SELFIE, currentConfigMap)
            }
            PERMISSION_SELFIE_IDCARD_REQUEST_CODE -> {
                smileCapture(
                    currentTag,
                    CaptureType.SELFIE_AND_ID_CAPTURE,
                    currentConfigMap
                )
            }
            PERMISSION_IDCARD_REQUEST_CODE -> {
                smileCapture(
                    currentTag,
                    CaptureType.ID_CAPTURE,
                    currentConfigMap
                )
            }
        }

        return true
    }

    fun getCurrentTags(channelResult: Result) {
        var resultList = mutableListOf<String>()
        resultList.addAll(SIFileManager().getIdleTags(activity, true))
        val map: HashMap<String, Any> = HashMap()
        map.put("tags", resultList)
        channelResult.success(map)
    }

    fun getImagesForTag(
        tag: String?,
        channelResult: Result
    ) {
        var resultList = mutableListOf<String>()
        val selfiesList = SIFileManager().getSelfies(tag, activity)
        val idCardList = SIFileManager().getIdCards(tag, activity)
        for (selfie in selfiesList) {
            resultList.add(selfie.path)
        }
        for (idCard in idCardList) {
            resultList.add(idCard.path)
        }
        val map: HashMap<String, Any> = HashMap()
        map.put("images", resultList)
        channelResult.success(map)
    }

    fun clearTag(
        tag: String?,
        channelResult: Result
    ) {
        SIDInfosManager.getInstance(activity).clearData(tag!!)
        val map: HashMap<String, Any> = HashMap()
        map.put(SID_RESULT_CODE, 1)
        channelResult.success(map)
    }

    fun showConsent(
        tag: String?,
        partnerLogo: String?,
        appBundleId: String?,
        partnerName: String?,
        privacyPolicyUrl: String?,
        channelResult: Result
    ) {

        val bitmap = BitmapFactory.decodeResource(
            activity.getResources(),
            activity.getResources().getIdentifier(partnerLogo, "drawable", appBundleId)
        )

        //To be replaced by a partner-set values as returned by the backend
        val intent = Intent(activity, ConsentActivity::class.java)
        intent.putExtra(ConsentActivity.TAG, tag)
//        intent.putExtra(ConsentActivity.PARTNER_LOGO, bitmap)
        intent.putExtra(ConsentActivity.PARTNER_NAME, partnerName)
        intent.putExtra(ConsentActivity.PRIVACY_LINK, privacyPolicyUrl)
        activity.startActivityForResult(intent, USER_CONSENT_REQUEST_CODE)
    }


    fun showBVNConsent(
        tag: String?,
        partnerLogo: String?,
        appBundleId: String?,
        partnerName: String?,
        privacyPolicyUrl: String?,
        isProduction: Boolean?,
        channelResult: Result
    ) {
        var isEnvProd = false
        isProduction?.let {
            isEnvProd = it
        }
        var sidConsentConfig = SIDConsentConfig(activity, USER_BVN_CONSENT_REQUEST_CODE)
        sidConsentConfig.show(
            tag, partnerName,
            activity.getResources().getIdentifier(partnerLogo, "drawable", activity.packageName),
            isEnvProd, privacyPolicyUrl, "NG", "BVN_MFA"
        )
    }

    fun smileCapture(
        tag: String?,
        captureType: CaptureType,
        configMap: HashMap<String, String>?
    ) {
        val config = SIDSelfieCaptureConfig.Builder()
        val idCaptureConfig = SIDIDCaptureConfig.Builder()

        if (configMap != null) {
            val iterator = configMap.iterator()

            while (iterator.hasNext()) {
                val entry = iterator.next()

                when (entry.key) {
                    SCREEN_TITLE_STYLE -> config.setTitleStyle(
                        SIDUtil.flattenMap(
                            entry.value as HashMap<String, Any>
                        )
                    )
                    IS_FULLSCREEN -> config.setCaptureFullScreen(entry.value as Boolean)

                    CAPTURE_TITLE_TEXT -> config.setCaptureTitle(
                        SIDUtil.getStringFromResId(
                            activity,
                            entry.value as String
                        )
                    )

                    OVERLAY_COLOR -> config.setOverlayColor(
                        SIDUtil.getColorFromResId(
                            activity,
                            entry.value as String
                        )
                    )
                    OVERLAY_ALPHA -> config.setOverlayAlpha((entry.value as Double).toInt())
                    OVERLAY_THICKNESS -> config.setOverlayThickness((entry.value as Double).toInt())
                    OVERLAY_DOTTED -> config.setOverlayDotted(entry.value as Boolean)
                    OVERLAY_WIDTH -> config.setOverlayWidth((entry.value as Double).toInt())
                    OVERLAY_HEIGHT -> config.setOverlayHeight((entry.value as Double).toInt())
                    IS_WHITE_LABELLED -> {
                        config.setIsWhiteLabeled(
                            entry
                                .value.toBoolean())
                        idCaptureConfig.setIsWhiteLabeled(
                            entry
                                .value.toBoolean())
                    }
                    CAPTURING_PROGRESS_COLOR -> config.setCapturingProgressColor(
                        SIDUtil.getColorFromResId(
                            activity,
                            entry.value as String
                        )
                    )
                    CAPTURED_PROGRESS_COLOR -> config.setCapturedProgressColor(
                        SIDUtil.getColorFromResId(
                            activity,
                            entry.value as String
                        )
                    )

                    //All the Capture texts
                    PROMPT_DEFAULT_TEXT -> config.setPromptDefault(
                        SIDUtil.getStringFromResId(
                            activity,
                            entry.value as String
                        )
                    )
                    PROMPT_BLURRY_TEXT -> config.setPromptBlurry(
                        SIDUtil.getStringFromResId(
                            activity,
                            entry.value as String
                        )
                    )
                    PROMPT_CAPTURING_TEXT -> config.setPromptCapturing(
                        SIDUtil.getStringFromResId(
                            activity,
                            entry.value as String
                        )
                    )

                    PROMPT_COMPATIBILITY_MODE_TEXT -> config.setPromptCompatibilityMode(
                        SIDUtil.getStringFromResId(
                            activity,
                            entry.value as String
                        )
                    )
                    PROMPT_DO_SMILE_TEXT -> config.setPromptDoSmile(
                        SIDUtil.getStringFromResId(
                            activity,
                            entry.value as String
                        )
                    )
                    PROMPT_FACE_NOT_FOUND_TEXT -> config.setPromptFaceNotFound(
                        SIDUtil.getStringFromResId(
                            activity,
                            entry.value as String
                        )
                    )
                    PROMPT_FACE_TOO_CLOSE_TEXT -> config.setPromptFaceTooClose(
                        SIDUtil.getStringFromResId(
                            activity,
                            entry.value as String
                        )
                    )
                    PROMPT_IDLE_TEXT -> config.setPromptIdle(
                        SIDUtil.getStringFromResId(
                            activity,
                            entry.value as String
                        )
                    )
                    PROMPT_MOVE_CLOSER_TEXT -> config.setPromptMoveCloser(
                        SIDUtil.getStringFromResId(
                            activity,
                            entry.value as String
                        )
                    )
                    PROMPT_TOO_DARK_TEXT -> config.setPromptTooDark(
                        SIDUtil.getStringFromResId(
                            activity,
                            entry.value as String
                        )
                    )

                    CAPTURE_PROMPT_STYLE -> config.setPromptStyle(
                        SIDUtil.flattenMap(
                            entry.value as HashMap<String, Any>
                        )
                    )

                    //Tip text and styling
                    CAPTURE_TIP_TEXT -> config.setCaptureTip(
                        SIDUtil.getStringFromResId(
                            activity,
                            entry.value as String
                        )
                    )
                    CAPTURE_TIP_STYLE -> config.setTipStyle(
                        SIDUtil.flattenMap(
                            entry.value as HashMap<String, Any>
                        )
                    )

                    REVIEW_TITLE_TEXT -> config.setReviewTitle(
                        SIDUtil.getStringFromResId(
                            activity,
                            entry.value as String
                        )
                    )

                    REVIEW_PROMPT_TEXT -> config.setReviewPrompt(
                        SIDUtil.getStringFromResId(
                            activity,
                            entry.value as String
                        )
                    )
                    REVIEW_PROMPT_STYLE -> config.setReviewPromptStyle(
                        SIDUtil.flattenMap(
                            entry.value as HashMap<String, Any>
                        )
                    )

                    REVIEW_TIP_TEXT -> config.setReviewTip(
                        SIDUtil.getStringFromResId(
                            activity,
                            entry.value as String
                        )
                    )
                    REVIEW_TIP_STYLE -> config.setReviewTipStyle(
                        SIDUtil.flattenMap(
                            entry.value as HashMap<String, Any>
                        )
                    )

                    REVIEW_CONFIRM_TEXT -> config.setReviewConfirmButton(
                        SIDUtil.getStringFromResId(
                            activity,
                            entry.value as String
                        )
                    )
                    REVIEW_CONFIRM_COLOR -> config.setReviewConfirmButtonColor(
                        SIDUtil.getColorFromResId(
                            activity,
                            entry.value as String
                        )
                    )
                    REVIEW_CONFIRM_STYLE -> config.setReviewConfirmButtonStyle(
                        SIDUtil.flattenMap(
                            entry.value as HashMap<String, Any>
                        )
                    )

                    REVIEW_RETAKE_TEXT -> config.setReviewRetakeButton(
                        SIDUtil.getStringFromResId(
                            activity,
                            entry.value as String
                        )
                    )
                    REVIEW_RETAKE_COLOR -> config.setReviewRetakeButtonColor(
                        SIDUtil.getColorFromResId(
                            activity,
                            entry.value as String
                        )
                    )
                    REVIEW_RETAKE_STYLE -> config.setReviewRetakeButtonStyle(
                        SIDUtil.flattenMap(
                            entry.value as HashMap<String, Any>
                        )
                    )
                    ID_CAPTURE_SIDE -> {
                        when (entry.value) {
                            "0" -> idCaptureConfig.setCaptureSide(
                                SIDIDCaptureConfig.CaptureSide.Front
                            )
                            "1" -> idCaptureConfig.setCaptureSide(
                                SIDIDCaptureConfig.CaptureSide.FrontAndBack
                            )
                            "2" -> idCaptureConfig.setCaptureSide(
                                SIDIDCaptureConfig.CaptureSide.Back
                            )
                        }

                    }
                    ID_CAPTURED_BLURRY_MESSAGE -> {
                        idCaptureConfig.setPromptCapturedBlurry(entry.value as String)
                    }
                    ID_CAPTURED_DARK_MESSAGE -> {
                        idCaptureConfig.setPromptCapturedDark(entry.value as String)
                    }
                    ID_CAPTURE_ORIENTATION -> {
                        when (entry.value) {
                            "0" -> idCaptureConfig.setCaptureOrientation(
                                SmartCardView.IDCaptureOrientation.DEVICE
                            )
                            "1" -> idCaptureConfig.setCaptureOrientation(
                                SmartCardView.IDCaptureOrientation.LANDSCAPE
                            )
                            "2" -> idCaptureConfig.setCaptureOrientation(
                                SmartCardView.IDCaptureOrientation.PORTRAIT
                            )
                        }

                    }
                    CARD_TYPE -> {
                        when (entry.value as String) {
                            "card" -> idCaptureConfig.setIdType(IdType.Idcard)
                            "passport" -> idCaptureConfig.setIdType(IdType.Passport)
                            "other" -> idCaptureConfig.setIdType(IdType.Other)
                        }
                    }
                    PROMPT_ID_CAPTURE_BLURRY -> idCaptureConfig.setPromptBlurry(
                        entry.value as String
                    )
                    PROMPT_ID_DETECTING_FACE -> idCaptureConfig.setPromptDetectingFace(
                        entry.value as String
                    )
                    PROMPT_ID_FACE_DETECTED -> idCaptureConfig.setPromptFaceDetected(
                        entry.value as String
                    )
                    PROMPT_ID_FIT_ID -> idCaptureConfig.setPromptFitID(entry.value as String)
                    PROMPT_ID_FLASH_MISSING -> idCaptureConfig.setPromptFlashMissing(
                        entry.value as String
                    )
                    PROMPT_ID_INSUFFICIENT_LIGHT -> idCaptureConfig.setPromptInsufficientLight(
                        entry.value as String
                    )
                    PROMPT_ID_LOADING -> idCaptureConfig.setPromptLoading(entry.value as String)
                    PROMPT_ID_NO_FACE_DETECTED -> idCaptureConfig.setPromptNoFaceDetected(
                        entry.value as String
                    )
                    PROMPT_ID_BACK_ID -> idCaptureConfig.setPromptBackOfIdCapture(
                        entry.value as String
                    )
                    PROMPT_ID_CAPTURED_BLURRY -> idCaptureConfig.setPromptCapturedBlurry(
                        entry.value as String
                    )
                    PROMPT_ID_CAPTURED_DARK -> idCaptureConfig.setPromptCapturedDark(
                        entry.value as String
                    )
                }
            }
        }
        val builder = SIDCaptureManager.Builder(
            activity,
            captureType,
            1000 + captureType.ordinal
        )

        if (!TextUtils.isEmpty(tag)) {
            builder.setTag(tag)
        }

        builder.setSidSelfieConfig(config.build())
        builder.setSidIdCaptureConfig(idCaptureConfig.build())
        builder.build().start()
    }

    fun submitJob(
        tag: String?,
        jobType: Int?,
        isProduction: Boolean?,
        callBackUrl: String?,
        partnerParams: HashMap<String, String>?,
        @Nullable idInfo: HashMap<String, String>?,
        @Nullable geoInfo: HashMap<String, String>?,
        channelResult: Result
    ) {
        var isAllowReEnroll = false
        var useEnrolledImage = false
        if (TextUtils.isEmpty(tag)) {
            channelResult.error(submitError, "tag is not set", null)
            return
        }
        if (jobType == null) {
            channelResult.error(submitError, "job type is not set", null)
            return
        }

        if (isProduction == null) {
            channelResult.error(submitError, "environment is not set", null)
            return
        }

//        var uploadChannelEvent = EventChannel(flutterEngine.dartExecutor.binaryMessenger,uploadListenerEventChannel)


        val metadata = SIDMetadata()
        val sidNetworkRequest = SIDNetworkRequest(activity)
        sidNetworkRequest.setOnCompleteListener {
//            val params = Arguments.createMap()
//            params.putString("status", "done")
//            sendEvent(reactApplicationContext, "CompleteListener", params);
        }
        sidNetworkRequest.set0nErrorListener {
            channelResult.error(tag ?: "default", "set0nErrorListener", it)
        }
        sidNetworkRequest.setOnUpdateListener {
//            val params = Arguments.createMap()
//            params.putString("status", it.toString())
//            sendEvent(reactApplicationContext, "UploadListener", params);
        }
        sidNetworkRequest.setOnEnrolledListener {
            if (it != null && it.statusResponse != null) {
                val result =
                    SIDUtil.convertJsonToMap(JSONObject(it.statusResponse.rawJsonString))
                channelResult.success(result)
            }
        }
        sidNetworkRequest.setOnIDValidationListener{
            if (it != null) {
                val result =
                    SIDUtil.convertJsonToMap(JSONObject(it.rawJsonString))
                channelResult.success(result)
            } else {
                channelResult.error(tag!!, "set0nErrorListener", it)
            }
        }
        sidNetworkRequest.setOnAuthenticatedListener {
            if (it != null && it.statusResponse != null) {
                val result =
                    SIDUtil.convertJsonToMap(JSONObject(it.statusResponse.rawJsonString))
                channelResult.success(result)
            }
        }

        sidNetworkRequest.setOnDocVerificationListener {
            if (it != null && it.statusResponse != null) {
                val result =
                    SIDUtil.convertJsonToMap(JSONObject(it.statusResponse.rawJsonString))
                channelResult.success(result)
            }
        }

        if (idInfo != null && !idInfo.isEmpty()) {
            val sidUserIdInfo = metadata.sidUserIdInfo
            for ((key, value) in idInfo) {
                if (!mainIdInfo.any { mainKey -> mainKey == key }) {
                    sidUserIdInfo.additionalValue(key, value)
                } else {
                    when (key) {
                        "country" -> sidUserIdInfo.countryCode = value
                        "id_type" -> sidUserIdInfo.idType = value
                        "id_number" -> sidUserIdInfo.idNumber = value
                        "first_name" -> sidUserIdInfo.firstName = value
                        "middle_name" -> sidUserIdInfo.middleName = value
                        "last_name" -> sidUserIdInfo.lastName = value
                        "email" -> sidUserIdInfo.email = value
                        "use_enrolled_image" -> useEnrolledImage =
                            value.toBoolean()
                        "allow_re_enroll" -> isAllowReEnroll = value.toBoolean()
                    }
                }
            }
        }

        if (partnerParams != null && !partnerParams.isEmpty()) {
            val sidPartnerParams = metadata.partnerParams
            for ((key, value) in partnerParams) {
                if (!mainPartnerParams.any { mainKey -> mainKey == key }) {
                    sidPartnerParams.additionalValue(key, value)
                } else {
                    when (key) {
                        "job_id" -> sidPartnerParams.jobId = value
                        "user_id" -> sidPartnerParams.userId = value
                    }
                }
            }
        }

        val geoInfos = GeoInfos()
        if (geoInfo != null && geoInfo.isEmpty()) {
            for ((key, value) in geoInfo) {
                when (key) {
                    "accuracy" -> geoInfos.accuracy = value as Double
                    "altitude" -> geoInfos.altitude = value as Double
                    "latitude" -> geoInfos.latitude = value as Double
                    "longitude" -> geoInfos.longitude = value as Double
                    "lastUpdate" -> geoInfos.lastUpdate = value as String
                    "isGeoPermissionGranted" -> geoInfos.isGeoPermissionGranted =
                        value as Boolean
                }
            }
        }

        val data = SIDNetData(
            activity,
            if (isProduction) SIDNetData.Environment.PROD else SIDNetData.Environment.TEST
        )
        if (!TextUtils.isEmpty(callBackUrl)) {
            data.callBackUrl = callBackUrl
        }
        val config = SIDConfig.Builder(activity)
            .setSmileIdNetData(data)
            .setGeoInformation(geoInfos)
            .setSIDMetadata(metadata)
            .setJobType(jobType)
            .setAllowNewEnroll(isAllowReEnroll)
            .useEnrolledImage(useEnrolledImage)
            .setMode(if (jobType == 1 || jobType == 4 || jobType == 6) SIDConfig.Mode.ENROLL else SIDConfig.Mode.AUTHENTICATION)
            .build(tag)
        activity?.let {
            if (haveNetworkConnection(it)) {
                sidNetworkRequest.submit(config)
            } else {
                channelResult.error(
                    submitError,
                    "Please make sure you are connected",
                    null
                )
            }
        }
    }


    fun checkPermissions(): String {
        val inValidPermission = permissionGranted(PERMISSIONS)
        if (inValidPermission.length == 0) {
            return "";
        } else {
            ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS,
                PERMISSION_ALL
            )
        }
        return inValidPermission;
    }

    protected fun permissionGranted(permissions: Array<String>): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        permission!!
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return permission
                }
            }
        }
        return ""
    }

    fun haveNetworkConnection(context: Context): Boolean {
        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (cm.activeNetworkInfo != null) {
            return cm.activeNetworkInfo!!.isConnected
        } else {
            return false
        }
    }
}
