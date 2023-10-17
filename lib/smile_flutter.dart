import 'dart:async';
import 'dart:collection';

import 'package:flutter/services.dart';

class SmileFlutter {
  static const MethodChannel _channel = const MethodChannel('smile_flutter');
  final _uploadListenerEventChannel =
      const EventChannel('smile_id/upload_listener');
  final _completeListenerEventChannel =
      const EventChannel('smile_id/complete_listener');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<Map<dynamic, dynamic>?> captureSelfie(String tag,
      [Map<String, String>? config, autoHandlePermissions = true]) async {
    try {
      var result = await _channel.invokeMethod('captureSelfie', {
        "tag": tag,
        "autoHandlePermissions": autoHandlePermissions,
        "config": config
      });
      return result;
    } catch (e) {
      rethrow;
    }
  }

  static Future<Map<dynamic, dynamic>?> captureIDCard(String tag,
      [Map<String, String>? config, autoHandlePermissions = true]) async {
    try {
      var result = await _channel.invokeMethod('captureIDCard', {
        "tag": tag,
        "autoHandlePermissions": autoHandlePermissions,
        "config": config
      });
      return result;
    } catch (e) {
      rethrow;
    }
  }

  static Future<Map<dynamic, dynamic>?> captureSelfieAndIDCard(String tag, [Map<String, String>? config, autoHandlePermissions = true]) async {
    try {
      var result = await _channel.invokeMethod('captureSelfieAndIDCard', {
        "tag": tag,
        "config": config,
        "autoHandlePermissions": autoHandlePermissions
      });
      return result;
    } catch (e) {
      rethrow;
    }
  }

  static Future<Map<dynamic, dynamic>?> getCurrentTags() async {
    try {
      var result = await _channel.invokeMethod('getCurrentTags');
      return result;
    } catch (e) {
      rethrow;
    }
  }

  static Future<Map<dynamic, dynamic>?> getImagesForTag(String tag) async {
    try {
      var result = await _channel.invokeMethod('getImagesForTag', {"tag": tag});
      return result;
    } catch (e) {
      rethrow;
    }
  }

  static Future<Map<dynamic, dynamic>?> clearTag(String tag) async {
    try {
      var result = await _channel.invokeMethod('clearTag', {"tag": tag});
      return result;
    } catch (e) {
      rethrow;
    }
  }

  static Future<Map<dynamic, dynamic>?> submitJob(
      String tag,
      int jobType,
      bool isProduction,
      String callBackUrl,
      Map<String, String>? partnerParams,
      Map<String, String>? idInfo,
      Map<String, String>? geoInfo) async {
    try {
      var result = await _channel.invokeMethod('submitJob', {
        "tag": tag,
        "jobType": jobType,
        "isProduction": isProduction,
        "callBackUrl": callBackUrl,
        "partnerParams": partnerParams,
        "idInfo": idInfo,
        "geoInfo": geoInfo
      });
      return result;
    } catch (e) {
      rethrow;
    }
  }

  static Future<Map<dynamic, dynamic>?> showConsent(
      String tag,
      String partnerLogo,
      String appBundleId,
      String partnerName,
      String privacyPolicyUrl) async {
    try {
      var result = await _channel.invokeMethod('showConsentScreen', {
        "tag": tag,
        "partnerLogo": partnerLogo,
        "appBundleId": appBundleId,
        "partnerName": partnerName,
        "privacyPolicyUrl": privacyPolicyUrl
      });
      return result;
    } catch (e) {
      rethrow;
    }
  }

  static Future<Map<dynamic, dynamic>?> showBVNConsent(
      String tag,
      String partnerLogo,
      String appBundleId,
      String partnerName,
      String privacyPolicyUrl,
      bool isProduction) async {
    try {
      var result = await _channel.invokeMethod('showBVNConsent', {
        "tag": tag,
        "partnerLogo": partnerLogo,
        "appBundleId": appBundleId,
        "partnerName": partnerName,
        "privacyPolicyUrl": privacyPolicyUrl,
        "isProduction": isProduction,
      });
      return result;
    } catch (e) {
      rethrow;
    }
  }
}
