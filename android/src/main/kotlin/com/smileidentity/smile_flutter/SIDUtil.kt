package com.rnsmileid

import android.content.Context
import android.graphics.Color
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap


class SIDUtil {
    companion object {
        @Throws(JSONException::class)
        fun convertJsonToMap(jsonObject: JSONObject): HashMap<Any, Any>? {
            val map: HashMap<Any, Any> = HashMap()
            val iterator: Iterator<String> = jsonObject.keys()
            while (iterator.hasNext()) {
                val key = iterator.next()
                val value: Any = jsonObject.get(key)
                if (value is JSONObject) {
                    convertJsonToMap(value as JSONObject)?.let { map.put(key, it) }
                } else if (value is JSONArray) {
                    convertJsonToArray(value as JSONArray)?.let { map.put(key, it) }
                } else if (value is Boolean) {
                    map.put(key, value)
                } else if (value is Int) {
                    map.put(key, value)
                } else if (value is Double) {
                    map.put(key, value)
                } else if (value is String) {
                    map.put(key, value)
                } else {
                    map.put(key, value.toString())
                }
            }
            return map
        }

        @Throws(JSONException::class)
        fun convertJsonToArray(jsonArray: JSONArray): List<Any>? {
            val array = mutableListOf<Any>()

            for (i in 0 until jsonArray.length()) {
                val value: Any = jsonArray.get(i)
                if (value is JSONObject) {
                    convertJsonToMap(value as JSONObject)?.let { array.add(it) }
                } else if (value is JSONArray) {
                    convertJsonToArray(value as JSONArray)?.let { array.add(it) }
                } else if (value is Boolean) {
                    array.add(value)
                } else if (value is Int) {
                    array.add(value)
                } else if (value is Double) {
                    array.add(value)
                } else if (value is String) {
                    array.add(value)
                } else {
                    array.add(value.toString())
                }
            }
            return array
        }

//        @Throws(JSONException::class)
//        fun convertMapToJson(readableMap: HashMap<Any, Any>?): JSONObject? {
//            val `object` = JSONObject()
//            val iterator = readableMap!!.keySetIterator()
//            while (iterator.hasNextKey()) {
//                val key = iterator.nextKey()
//                when (readableMap.getType(key)) {
//                    ReadableType.Null -> `object`.put(key, JSONObject.NULL)
//                    ReadableType.Boolean -> `object`.put(key, readableMap.getBoolean(key))
//                    ReadableType.Number -> `object`.put(key, readableMap.getDouble(key))
//                    ReadableType.String -> `object`.put(key, readableMap.getString(key))
//                    ReadableType.Map -> `object`.put(key, convertMapToJson(readableMap.getMap(key)))
//                    ReadableType.Array -> `object`.put(
//                        key,
//                        convertArrayToJson(readableMap.getArray(key))
//                    )
//                }
//            }
//            return `object`
//        }
//
//        @Throws(JSONException::class)
//        fun convertArrayToJson(readableArray: ReadableArray?): JSONArray? {
//            val array = JSONArray()
//            for (i in 0 until readableArray!!.size()) {
//                when (readableArray.getType(i)) {
//                    ReadableType.Null -> {
//                    }
//                    ReadableType.Boolean -> array.put(readableArray.getBoolean(i))
//                    ReadableType.Number -> array.put(readableArray.getDouble(i))
//                    ReadableType.String -> array.put(readableArray.getString(i))
//                    ReadableType.Map -> array.put(convertMapToJson(readableArray.getMap(i)))
//                    ReadableType.Array -> array.put(convertArrayToJson(readableArray.getArray(i)))
//                }
//            }
//            return array
//        }

         fun getColorFromResId(context: Context,resId: String): String =
            try {
                color2Rgb(color2RgbInt(context,getFromResourceId(context,resId, "color")))
            } catch (e: Exception) {
                resId
            }

        private fun color2Rgb(color: Int): String = "#" + Integer.toHexString(color)

        private fun color2RgbInt(context: Context,color: Int): Int {
            val red = Color.red(context.resources.getColor(color))
            val green = Color.green(context.resources.getColor(color))
            val blue = Color.blue(context.resources.getColor(color))
            return Color.rgb(red, green, blue)
        }

        fun getStringFromResId(context: Context, resId: String): String {
            return try {
                context.resources.getString(
                    getFromResourceId(context, resId, "string")
                )
            } catch (e: Exception) {
                resId
            }
        }

        fun getFromResourceId(context: Context, resId: String, resType: String): Int =
            context.resources.getIdentifier(
                resId, resType,
                context.packageName
            )

        fun flattenMap(styleMap: HashMap<String, Any>): String =
            JSONObject(styleMap as Map<*, *>?).toString()
    }
}
