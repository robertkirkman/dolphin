// Copyright 2018 Dolphin Emulator Project
// SPDX-License-Identifier: GPL-2.0-or-later

#include "jni/AndroidCommon/AndroidCommon.h"

#include <algorithm>
#include <ios>
#include <string>
#include <string_view>
#include <vector>

#include <jni.h>

#include "Common/Assert.h"
#include "Common/StringUtil.h"
#include "jni/AndroidCommon/IDCache.h"

std::string GetJString(JNIEnv* env, jstring jstr)
{
  const jchar* jchars = env->GetStringChars(jstr, nullptr);
  const jsize length = env->GetStringLength(jstr);
  const std::u16string_view string_view(reinterpret_cast<const char16_t*>(jchars), length);
  std::string converted_string = UTF16ToUTF8(string_view);
  env->ReleaseStringChars(jstr, jchars);
  return converted_string;
}

jstring ToJString(JNIEnv* env, const std::string& str)
{
  const std::u16string converted_string = UTF8ToUTF16(str);
  return env->NewString(reinterpret_cast<const jchar*>(converted_string.data()),
                        converted_string.size());
}

std::vector<std::string> JStringArrayToVector(JNIEnv* env, jobjectArray array)
{
  const jsize size = env->GetArrayLength(array);
  std::vector<std::string> result;
  result.reserve(size);

  for (jsize i = 0; i < size; ++i)
  {
    jstring str = reinterpret_cast<jstring>(env->GetObjectArrayElement(array, i));
    result.push_back(GetJString(env, str));
    env->DeleteLocalRef(str);
  }

  return result;
}

jobjectArray VectorToJStringArray(JNIEnv* env, const std::vector<std::string>& vector)
{
  return VectorToJObjectArray(env, vector, IDCache::GetStringClass(), ToJString);
}

bool IsPathAndroidContent(const std::string& uri)
{
  return uri.starts_with("content://");
}

std::string OpenModeToAndroid(std::string mode)
{
  // The 'b' specifier is not supported by Android. Since we're on POSIX, it's fine to just skip it.
  mode.erase(std::remove(mode.begin(), mode.end(), 'b'));

  if (mode == "r+")
    mode = "rw";
  else if (mode == "w+")
    mode = "rwt";
  else if (mode == "a+")
    mode = "rwa";
  else if (mode == "a")
    mode = "wa";

  return mode;
}

std::string OpenModeToAndroid(std::ios_base::openmode mode)
{
  std::string result;

  if (mode & std::ios_base::in)
    result += 'r';

  if (mode & (std::ios_base::out | std::ios_base::app))
    result += 'w';

  if (mode & std::ios_base::app)
    result += 'a';

  constexpr std::ios_base::openmode t = std::ios_base::in | std::ios_base::trunc;
  if ((mode & t) == t)
    result += 't';

  // The 'b' specifier is not supported by Android. Since we're on POSIX, it's fine to just skip it.

  return result;
}

int OpenAndroidContent(const std::string& uri, const std::string& mode)
{
  JNIEnv* env = IDCache::GetEnvForThread();

  jstring j_uri = ToJString(env, uri);
  jstring j_mode = ToJString(env, mode);

  jint result = env->CallStaticIntMethod(IDCache::GetContentHandlerClass(),
                                         IDCache::GetContentHandlerOpenFd(), j_uri, j_mode);

  env->DeleteLocalRef(j_uri);
  env->DeleteLocalRef(j_mode);

  return result;
}

bool DeleteAndroidContent(const std::string& uri)
{
  JNIEnv* env = IDCache::GetEnvForThread();

  jstring j_uri = ToJString(env, uri);

  jboolean result = env->CallStaticBooleanMethod(IDCache::GetContentHandlerClass(),
                                                 IDCache::GetContentHandlerDelete(), j_uri);

  env->DeleteLocalRef(j_uri);

  return static_cast<bool>(result);
}

jlong GetAndroidContentSizeAndIsDirectory(const std::string& uri)
{
  JNIEnv* env = IDCache::GetEnvForThread();

  jstring j_uri = ToJString(env, uri);

  jlong result = env->CallStaticLongMethod(
      IDCache::GetContentHandlerClass(), IDCache::GetContentHandlerGetSizeAndIsDirectory(), j_uri);

  env->DeleteLocalRef(j_uri);

  return result;
}

std::string GetAndroidContentDisplayName(const std::string& uri)
{
  JNIEnv* env = IDCache::GetEnvForThread();

  jstring j_uri = ToJString(env, uri);

  jstring j_result = reinterpret_cast<jstring>(env->CallStaticObjectMethod(
      IDCache::GetContentHandlerClass(), IDCache::GetContentHandlerGetDisplayName(), j_uri));

  env->DeleteLocalRef(j_uri);

  if (!j_result)
    return "";

  std::string result = GetJString(env, j_result);

  env->DeleteLocalRef(j_result);

  return result;
}

std::vector<std::string> GetAndroidContentChildNames(const std::string& uri)
{
  JNIEnv* env = IDCache::GetEnvForThread();

  jstring j_uri = ToJString(env, uri);

  jobjectArray j_result = reinterpret_cast<jobjectArray>(env->CallStaticObjectMethod(
      IDCache::GetContentHandlerClass(), IDCache::GetContentHandlerGetChildNames(), j_uri, false));

  std::vector<std::string> result = JStringArrayToVector(env, j_result);

  env->DeleteLocalRef(j_uri);
  env->DeleteLocalRef(j_result);

  return result;
}

std::vector<std::string> DoFileSearchAndroidContent(const std::string& directory,
                                                    const std::vector<std::string>& extensions,
                                                    bool recursive)
{
  JNIEnv* env = IDCache::GetEnvForThread();

  jstring j_directory = ToJString(env, directory);
  jobjectArray j_extensions = VectorToJStringArray(env, extensions);

  jobjectArray j_result = reinterpret_cast<jobjectArray>(env->CallStaticObjectMethod(
      IDCache::GetContentHandlerClass(), IDCache::GetContentHandlerDoFileSearch(), j_directory,
      j_extensions, recursive));

  std::vector<std::string> result = JStringArrayToVector(env, j_result);

  env->DeleteLocalRef(j_directory);
  env->DeleteLocalRef(j_extensions);
  env->DeleteLocalRef(j_result);

  return result;
}

int GetNetworkIpAddress()
{
  JNIEnv* env = IDCache::GetEnvForThread();
  return env->CallStaticIntMethod(IDCache::GetNetworkHelperClass(),
                                  IDCache::GetNetworkHelperGetNetworkIpAddress());
}

int GetNetworkPrefixLength()
{
  JNIEnv* env = IDCache::GetEnvForThread();
  return env->CallStaticIntMethod(IDCache::GetNetworkHelperClass(),
                                  IDCache::GetNetworkHelperGetNetworkPrefixLength());
}

int GetNetworkGateway()
{
  JNIEnv* env = IDCache::GetEnvForThread();
  return env->CallStaticIntMethod(IDCache::GetNetworkHelperClass(),
                                  IDCache::GetNetworkHelperGetNetworkGateway());
}
