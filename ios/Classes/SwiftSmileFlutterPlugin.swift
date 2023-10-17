import Flutter
import UIKit
import Smile_Identity_SDK

#if arch(arm64) || arch(x86_64)
public class SwiftSmileFlutterPlugin: NSObject, FlutterPlugin,SIDCaptureManagerDelegate,SIDConsentManagerDelegate,BVNConsentResultDelegate {

    
    
    
    
    
    let SID_RESULT_CODE = "SID_RESULT_CODE"
    let SID_RESULT_MESSAGE = "SID_RESULT_MESSAGE"
    let SID_RESULT_TAG = "SID_RESULT_TAG"
    var currentResult : FlutterResult?
    var currentTag:String?
    let mainIdInfo = ["first_name", "last_name", "middle_name", "country", "id_type", "id_number", "email","allow_re_enroll","use_enrolled_image"]
    let mainPartnerParams = ["user_id", "job_id"]
    
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "smile_flutter", binaryMessenger: registrar.messenger())
        let instance = SwiftSmileFlutterPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        var contextTag = ""
        var cameraConfigs: Dictionary<String, Any>?
        if let args = call.arguments as? Dictionary<String, Any>{
            contextTag = args["tag"] as? String ?? ""
            cameraConfigs = args["config"] as? Dictionary<String, Any> ?? nil
        }
        
        switch call.method {
        case "getPlatformVersion":
            result("iOS " + UIDevice.current.systemVersion)
            return
        case "captureSelfie":
            smileCapture(tag: contextTag, captureType: CaptureType.SELFIE,configMap: cameraConfigs, result: result)
            return
        case "captureIDCard":
            smileCapture(tag: contextTag, captureType: CaptureType.ID_CAPTURE,configMap: cameraConfigs, result: result)
            return
        case "captureSelfieAndIDCard":
            smileCapture(tag: contextTag, captureType: CaptureType.SELFIE_AND_ID_CAPTURE,configMap: cameraConfigs, result: result)
            return
        case "showConsentScreen":
            var partnerLogo = ""
            var partnerName = ""
            var privacyPolicyUrl = ""
            var appBundleId = ""
            if let args = call.arguments as? Dictionary<String, Any>{
                partnerLogo = args["partnerLogo"] as? String ??  ""
                partnerName = args["partnerName"] as? String ?? ""
                privacyPolicyUrl = args["privacyPolicyUrl"] as? String ?? ""
                appBundleId = args["appBundleId"] as? String ?? ""
            }
            showConsentScreen(tag: contextTag, partnerLogo: partnerLogo, appBundleId: appBundleId, partnerName: partnerName, privacyPolicyUrl: privacyPolicyUrl,result: result)
            return
        case "showBVNConsent":
            var partnerLogo = ""
            var partnerName = ""
            var privacyPolicyUrl = ""
            var appBundleId = ""
            var isProduction = false
            if let args = call.arguments as? Dictionary<String, Any>{
                partnerLogo = args["partnerLogo"] as? String ??  ""
                partnerName = args["partnerName"] as? String ?? ""
                privacyPolicyUrl = args["privacyPolicyUrl"] as? String ?? ""
                appBundleId = args["appBundleId"] as? String ?? ""
                isProduction = args["isProduction"] as? Bool ?? false
            }
            showBVNConsentScreen(tag: contextTag, partnerLogo: partnerLogo, appBundleId: appBundleId, partnerName: partnerName, privacyPolicyUrl: privacyPolicyUrl, isProduction: isProduction,result: result)
            return
        case "getImagesForTag":
            getImagesForTag(tag: contextTag, result: result)
            return
        case "clearTag":
            clearTag(tag: contextTag, result: result)
            return
        case "getCurrentTags":
            getCurrentTags(result: result)
            return
        case "submitJob":
            var jobType : Int?
            var isProduction : Bool?
            var callBackUrl : String?
            var partnerParams :  Dictionary<String, String>?
            var idInfo : Dictionary<String, String>?
            var geoInfo : Dictionary<String, String>?
            if let args = call.arguments as? Dictionary<String, Any>{
                jobType = args["jobType"] as? Int ?? -1
                isProduction = args["isProduction"] as? Bool ?? false
                callBackUrl = args["callBackUrl"] as? String ?? ""
                partnerParams = args["partnerParams"] as? Dictionary<String, String> ?? nil
                idInfo = args["idInfo"] as? Dictionary<String, String> ?? nil
                geoInfo = args["geoInfo"] as? Dictionary<String, String> ?? nil
            }
            submitJob(tag: contextTag, jobType: jobType , isProduction: isProduction ?? false,callBackUrl:callBackUrl, partnerParams: partnerParams, idInfo: idInfo, geoInfo: geoInfo, result:result)
            return
        default:
            result(FlutterError.init(code: "unimplemented", message: nil, details: nil))
        }
    }
    
    
    public func onDone(tag: String, allowed: Bool) {
        if let result = self.currentResult {
            let response = [self.SID_RESULT_TAG: tag, self.SID_RESULT_CODE: allowed ? 1 : 0] as [String : Any]
            result(response)
        }
        deassignTempVar()
    }
    
    public func onSuccess(tag: String, selfiePreview: UIImage?, idFrontPreview: UIImage?, idBackPreview: UIImage?) {
        if let result = self.currentResult {
            let response = [self.SID_RESULT_TAG: tag, self.SID_RESULT_CODE: -1] as [String : Any]
            result(response)
        }
    }

    private func normalizeBVNErrorCodes(bvnConsentError: Smile_Identity_SDK.BVNConsentError)->Int{
        switch(bvnConsentError){
        case .CONFIG_ERROR_PARTNER_NAME_MISSING:
            return 777
        case .CONFIG_ERROR_PARTNER_LOGO_MISSING:
            return 778
        case .CONFIG_ERROR_CANNOT_LAUNCH:
            return 779
        case .USER_INPUT_DECLINED:
            return 780
        case .NETWORK_ERROR:
            return 781
        case .AUTH_SMILE_ERROR:
            return 782
        case .INIT_CONSENT_ERROR:
            return 783
        case .CONTACT_METHOD_ERROR:
            return 784
        case .CONTACT_METHODS_INVALID_ERROR:
            return 785
        case .CONFIRM_CONSENT_ERROR:
            return 786
        default:
            return 779
        }
    }

    public func onError(tag: String, bvnConsentError: Smile_Identity_SDK.BVNConsentError) {
        if let result = self.currentResult {
            var response : [String : Any] = ["TAG": tag,"ERROR_VALUE":normalizeBVNErrorCodes(bvnConsentError: bvnConsentError),"SID_RESULT_CODE":0]
            result(response)
        }

    }

    public func onSuccess(tag: String, bvnNumber: String, sessionId: String) {
        if let result = self.currentResult {
            var response : [String : Any] = ["TAG": tag,"IS_OTP_CONFIRMED":true,"SID_RESULT_CODE":1,"USER_BVN_NUMBER":bvnNumber,"BVN_SESSION_ID":sessionId]
            result(response)
        }
    }

    public func onError(tag: String, sidError: SIDError) {
        if let result = self.currentResult {
            let response = [self.SID_RESULT_TAG: tag, self.SID_RESULT_CODE: 0] as [String : Any]
            result(response)
        }
    }
    
    func getCurrentTags(result: @escaping FlutterResult) {
        result(["tags":SIFileManager().getIdleTags()])
    }
    
    func getImagesForTag(tag: String?,result: @escaping FlutterResult) {
        var resultList = [String]()
        let selfiesList = SIFileManager().getSelfies(userTag:tag!)
        let idCardList = SIFileManager().getIDCardImages(userTag: tag!)
        resultList.append(contentsOf: selfiesList)
        resultList.append(contentsOf: idCardList)
        result(["images":resultList])
    }
    
    func clearTag(tag: String?,result: @escaping FlutterResult) {
        SIDInfosManager.clearData(userTag: tag!)
        result(["SID_RESULT_CODE":1])
    }
    
    func getRetryOnFailurePolicy() -> RetryOnFailurePolicy {
        let options = RetryOnFailurePolicy();
        options.setMaxRetryTimeoutSec(maxRetryTimeoutSec:15 )
        return options;
    }
    
    func showConsentScreen(tag:String,partnerLogo:String,appBundleId:String,partnerName:String,
                           privacyPolicyUrl:String, result: @escaping FlutterResult){
        assignTempVars(tag: tag, result: result)
        let builder = SIDConsentManager.Builder(tag:tag,partnerLogo:partnerLogo,bundleId: appBundleId,partnerName: partnerName,privacyPolicyUrl: privacyPolicyUrl)
        builder.setDelegate(delegate: self).build().start()
    }
    
    func showBVNConsentScreen(tag:String,partnerLogo:String,appBundleId:String,partnerName:String,
                           privacyPolicyUrl:String,isProduction:Bool, result: @escaping FlutterResult){
        assignTempVars(tag: tag, result: result)
        SIDConsentManager.Builder(tag: tag,partnerLogo: partnerLogo,bundleId: appBundleId,partnerName: partnerName,privacyPolicyUrl: privacyPolicyUrl,environment: isProduction,bvnDelegate: self).setCountry(country: "NG").setIdType(idType: "BVN_MFA").build().start()
    }

    func submitJob(tag: String, jobType: Int?, isProduction: Bool?,callBackUrl:String?, partnerParams: Dictionary<String, String>?,  idInfo: Dictionary<String, String>?,  geoInfo: Dictionary<String, Any>?, result: @escaping FlutterResult) {
        let sidNetworkRequest = SIDNetworkRequest()
        sidNetworkRequest.setDelegate(delegate: SubmitJobListener(result: result))
        sidNetworkRequest.initialize()
        let sidNetData = SIDNetData(environment: isProduction ?? false ? SIDNetData.Environment.PROD : SIDNetData.Environment.TEST);
        if let callBack = callBackUrl{
            sidNetData.setCallBackUrl(callbackUrl: callBack)
        }
        var isAllowReEnroll = false
        var useEnrolledImage = false
        
        let sidConfig = SIDConfig()
        sidConfig.setSidNetworkRequest( sidNetworkRequest : sidNetworkRequest )
        sidConfig.setSidNetData( sidNetData : sidNetData )
        sidConfig.setRetryOnFailurePolicy( retryOnFailurePolicy: getRetryOnFailurePolicy() )
        if let idInfo = idInfo{
            let sidIdInfo = SIDUserIdInfo()
            for (key, value) in idInfo {
                switch key {
                case "country":
                    sidIdInfo.setCountry(country: value )
                case "id_type":
                    sidIdInfo.setIdType(idType: value )
                case "id_number":
                    sidIdInfo.setIdNumber(idNumber: value )
                case "first_name":
                    sidIdInfo.setFirstName(firstName: value )
                case "middle_name":
                    sidIdInfo.setMiddleName(middleName: value )
                case "last_name":
                    sidIdInfo.setLastName(lastName: value )
                case "email":
                    sidIdInfo.setEmail(email: value )
                case "allow_re_enroll":
                    isAllowReEnroll = (value as NSString).boolValue
                case "use_enrolled_image":
                    useEnrolledImage = (value as NSString).boolValue
                default:
                    sidIdInfo.additionalValue(name: key , value: value as! String)
                }
            }
            sidConfig.setUserIdInfo(userIdInfo: sidIdInfo)
        }
        
        let sidPartnerParams = PartnerParams()
        if let partnerParams = partnerParams{
            for (key, value) in partnerParams {
                switch key {
                case "job_id":
                    sidPartnerParams.setJobId(jobId: value )
                case "user_id":
                    sidPartnerParams.setUserId(userId:  value )
                default:
                    sidPartnerParams.setAdditionalValue(key: key , val: value as! String)
                }
            }
        }
        sidPartnerParams.setJobType(jobType: jobType! )
        sidConfig.setPartnerParams( partnerParams : sidPartnerParams )
        
        if let geoInfo = geoInfo {
            
            let sidGeoInfo = GeoInfos(latitude: geoInfo["latitude"] as! Double, longitude: geoInfo["longitude"] as! Double, altitude: geoInfo["altitude"] as! Double, accuracy: geoInfo[ "accuracy"] as! Double, lastUpdate: geoInfo["lastUpdate"] as! String)
        }
        let isEnrollMode = jobType == 1 || jobType == 4
        sidConfig.setIsEnrollMode(isEnrollMode: jobType == 1 || jobType == 4)
        if(isEnrollMode){
            let hasIdCard = SIDInfosManager.hasIdCard(userTag: tag)
            sidConfig.setUseIdCard(useIdCard: hasIdCard)
        }
        sidConfig.setUseEnrolledImage(useEnrolledImage: (useEnrolledImage))
        sidConfig.setAllowNewEnroll(allowNewEnroll: isAllowReEnroll)
        sidConfig.build(userTag: tag)
        do {
            try sidConfig.getSidNetworkRequest().submit(sidConfig: sidConfig)
        } catch {
            result(FlutterError.init(code: "Oops something went wrong!", message: nil, details: nil))
        }
    }
    
    func smileCapture(tag: String, captureType: CaptureType,configMap:Dictionary<String,Any>?, result: @escaping FlutterResult){
        assignTempVars(tag: tag, result:result)
        DispatchQueue.main.async {
            var builder = SIDCaptureManager.Builder(delegate:self, captureType: captureType)
            
            if !tag.isEmpty {
                builder = builder.setTag(tag: tag)
            }
            
            if (captureType == CaptureType.SELFIE_AND_ID_CAPTURE || captureType == CaptureType.ID_CAPTURE) {
                let sidIdCaptureConfig = SIDIDCaptureConfig.Builder().setIdCaptureType(idCaptureType: IDCaptureType.Front).build()
                builder = builder.setSidIdCaptureConfig(sidIdCaptureConfig: sidIdCaptureConfig!)
            }
            
            var config = SIDSelfieCaptureConfig.Builder()
            var idCaptureConfig = SIDIDCaptureConfig.Builder()
            
            if let configMap = configMap {
                for (key, value) in configMap {
                    switch key as! String {
                    case "screen_title_style":
                        config = config.setTitleStyle(titleStyle: value as! NSDictionary)
                        //                        case "is_full_screen":
                        //                            config.setFullScreen(isFullScreen: value as! Bool)
                        
                    case "capture_title_text":
                        config = config.setCaptureTitle(captureTitle: value as! String)
                    case "overlay_color":
                        config = config.setOverlayColor(overlayColor: self.hexStringToUIColor(hex: value as! String))
                    case "overlay_alpha":
                        config = config.setOverlayAlpha(alpha: value as! Int)
                    case "overlay_thickness":
                        config = config.setOverlayThickness(thickness: value as! Int)
                    case "overlay_dotted":
                        config = config.setOverlayDotted(dotted: value as! Bool)
                    case "overlay_width":
                        config = config.setOverlayWidth(overlayWidth: value as! Int)
                    case "overlay_height":
                        config = config.setOverlayHeight(overlayHeight: value as! Int)
                        
                    case "capturing_progress_color":
                        config = config.setCapturingProgressColor(capturingProgressColor: self.hexStringToUIColor(hex: value as! String))
                        
                    case "prompt_default_text":
                        config = config.setPromptDefault(promptDefault: value as! String)
                    case "prompt_blurry_text":
                        config = config.setPromptBlurry(promptBlurry: value as! String)
                    case "prompt_capturing_text":
                        config = config.setPromptCapturing(promptCapturing: value as! String)
                        
                    case "prompt_compatibility_mode_text":
                        config = config.setPromptCompatibilityMode(promptCompatibilityMode: value as! String)
                    case "prompt_do_smile_text":
                        config = config.setPromptDoSmile(promptDoSmile: value as! String)
                    case "prompt_face_not_found_text":
                        config = config.setPromptFaceNotFound(promptFaceNotFound: value as! String)
                    case "prompt_face_too_close_text":
                        config = config.setPromptFaceTooClose(promptFaceTooClose: value as! String)
                    case "prompt_idle_text":
                        config = config.setPromptIdle(promptIdle: value as! String)
                    case "prompt_move_closer_text":
                        config = config.setPromptMoveCloser(promptMoveCloser: value as! String)
                    case "prompt_too_dark_text":
                        config = config.setPromptTooDark(promptTooDark: value as! String)
                        
                    case "capture_prompt_style":
                        config = config.setPromptStyle(promptStyle: value as! NSDictionary)
                        
                    case "capture_tip_text":
                        config = config.setCaptureTip(captureTip: value as! String)
                    case "capture_tip_style":
                        config = config.setTipStyle(tipStyle: value as! NSDictionary)
                        
                    case "review_title_text":
                        config = config.setReviewTitle(reviewTitle: value as! String)
                        
                    case "review_prompt_text":
                        config = config.setReviewMainText(reviewMainText: value as! String)
                    case "review_prompt_style":
                        config = config.setReviewPromptStyle(promptStyle: value as! NSDictionary)
                        
                    case "review_confirm_text":
                        config = config.setReviewConfirmButton(reviewConfirmButton: value as! String)
                        
                    case "review_confirm_color":
                        config = config.setReviewConfirmButtonColor(reviewConfirmButtonColor: self.hexStringToUIColor(hex: value as! String))
                        
                    case "review_confirm_style":
                        config = config.setReviewConfirmButtonStyle(reviewConfirmButtonStyle: value as! NSDictionary)
                        
                    case "review_retake_text":
                        config = config.setReviewRetakeButton(reviewRetakeButton: value as! String)
                        
                    case "review_retake_color":
                        config = config.setReviewCancelButtonColor(reviewCancelButtonColor: self.hexStringToUIColor(hex: value as! String))
                        
                    case "review_retake_style":
                        config = config.setReviewCancelButtonStyle(reviewCancelButtonStyle: value as! NSDictionary)
                    case "is_white_labelled":
                        config = config.setIsWhiteLabeled(isWhiteLabeled: value as! String == "true")
                        idCaptureConfig = idCaptureConfig.setIsWhiteLabeled(isWhiteLabeled: value as! String == "true")
                    case "id_capture_side" :
                        switch(value as! String) {
                        case "0" : idCaptureConfig.setIdCaptureType(idCaptureType: IDCaptureType.Front)
                        case "1": idCaptureConfig.setIdCaptureType(idCaptureType:IDCaptureType.Front_And_Back)
                        case "1": idCaptureConfig.setIdCaptureType(idCaptureType:IDCaptureType.Back)
                        default:
                            idCaptureConfig.setIdCaptureType(idCaptureType: IDCaptureType.Front)
                        }
                    case "id_capture_orientation":
                        switch (value as! String) {
                        case "0" : idCaptureConfig.setIDCaptureOrientation(idCaptureOrientation: .device)
                        case "1" : idCaptureConfig.setIDCaptureOrientation(idCaptureOrientation: .landscape)
                        case "2" : idCaptureConfig.setIDCaptureOrientation(idCaptureOrientation: .potrait)
                        default:
                            idCaptureConfig.setIDCaptureOrientation(idCaptureOrientation: .landscape)
                        }
                    default:
                        config = config.setPromptCompatibilityMode(promptCompatibilityMode: "")
                    }
                }
            }
            
            builder = builder.setSidSelfieConfig(sidSelfieConfig: config.build()).setSidIdCaptureConfig(sidIdCaptureConfig: idCaptureConfig.build()!)
            builder.build().start()
        }
    }
    
    func hexStringToUIColor (hex:String) -> UIColor {
            var cString:String = hex.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()

            if (cString.hasPrefix("#")) {
                cString.remove(at: cString.startIndex)
            }

            if ((cString.count) != 6) {
                return UIColor.gray
            }

            var rgbValue:UInt64 = 0
            Scanner(string: cString).scanHexInt64(&rgbValue)

            return UIColor(
                red: CGFloat((rgbValue & 0xFF0000) >> 16) / 255.0,
                green: CGFloat((rgbValue & 0x00FF00) >> 8) / 255.0,
                blue: CGFloat(rgbValue & 0x0000FF) / 255.0,
                alpha: CGFloat(1.0)
            )
        }
    
    func assignTempVars(tag: String,result: @escaping FlutterResult){
        currentResult = result
        self.currentTag = tag
    }
    
    func deassignTempVar(){
        currentResult = nil
        currentTag = nil
    }
}

class SubmitJobListener: SIDNetworkRequestDelegate{
    
    func onDocumentVerified(sidResponse: SIDResponse) {
        if let response = sidResponse.getStatusResponse(){
            let response = convertToDictionary(text: response.getRawJsonString())
            self.result(response)
            return
        }
        result(FlutterError.init(code: "onAuthenticated Failed", message: nil, details: nil))
    }
    
    let result:FlutterResult
    init(result:@escaping FlutterResult) {
        self.result = result
    }
    func onStartJobStatus() {
        
    }
    
    func onEndJobStatus() {
        
    }
    
    func onUpdateJobProgress(progress: Int) {
        //            let params = ["status": "\(progress)"]
        //            SIDEventEmitter.sharedInstance.dispatch(name: "UploadListener", body: params)
    }
    
    func onUpdateJobStatus(msg: String) {
        
    }
    
    func onAuthenticated(sidResponse: SIDResponse) {
        if let response = sidResponse.getStatusResponse(){
            let response = convertToDictionary(text: response.getRawJsonString())
            self.result(response)
            return
        }
        result(FlutterError.init(code: "onAuthenticated Failed", message: nil, details: nil))
    }
    
    func onEnrolled(sidResponse: SIDResponse) {
        if let response = sidResponse.getStatusResponse(){
            let toReturn = convertToDictionary(text: response.getRawJsonString())
            if(toReturn != nil){
                self.result(toReturn)
                return
            }
            
        }
        result(FlutterError.init(code: "onEnrolled Failed", message: nil, details: nil))
    }
    
    func onComplete() {
        //        let params = ["status": "done"]
        //        SIDEventEmitter.sharedInstance.dispatch(name: "CompleteListener", body: params)
        
    }
    
    func onError(sidError: SIDError) {
        result(FlutterError.init(code:  "onError", message: sidError.message, details: nil))
    }
    
    func onIdValidated(idValidationResponse: IDValidationResponse) {
        let response = convertToDictionary(text: idValidationResponse.getRawJsonString())
        if(response != nil){
            self.result(response)
            return
        }
        result(FlutterError.init(code: "onIdValidated Failed", message: nil, details: nil))
    }
    
    func convertToDictionary(text: String) -> [String: Any]? {
        if let data = text.data(using: .utf8) {
            do {
                return try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any]
            } catch {
                result(FlutterError.init(code: "Oops something went wrong", message: nil, details: nil))
            }
        }
        return nil
    }
}
#else
public class SwiftSmileFlutterPlugin: NSObject, FlutterPlugin {
    public static func register(with registrar: FlutterPluginRegistrar) {
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        
    }
}
#endif
