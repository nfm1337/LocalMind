#include <jni.h>
#include <fstream>
#include <memory>
#include <string>
#include <vector>
#include <android/log.h>
#include "tokenizers_cpp.h"

#define TAG "Tokenizer"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static std::unique_ptr<tokenizers::Tokenizer> g_tokenizer;

static const int32_t BOS_ID = 0;
static const int32_t PAD_ID = 1;
static const int32_t EOS_ID = 2;

static std::string readFile(const char *path) {
    std::ifstream file(path, std::ios::binary);
    return {std::istreambuf_iterator<char>(file), std::istreambuf_iterator<char>()};
}

extern "C" {

JNIEXPORT jboolean JNICALL
Java_il_nfm_localmind_Tokenizer_nativeLoad(JNIEnv *env, jobject, jstring modelPath) {
    const char *path = env->GetStringUTFChars(modelPath, nullptr);
    std::string blob = readFile(path);
    env->ReleaseStringUTFChars(modelPath, path);

    if (blob.empty()) {
        LOGE("Failed to read model file");
        return JNI_FALSE;
    }

    g_tokenizer = tokenizers::Tokenizer::FromBlobJSON(blob);
    return g_tokenizer != nullptr ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jintArray JNICALL
Java_il_nfm_localmind_Tokenizer_nativeEncode(JNIEnv *env, jobject, jstring text, jint maxLen) {
    if (!g_tokenizer) {
        LOGE("Tokenizer not loaded");
        return nullptr;
    }

    const char *str = env->GetStringUTFChars(text, nullptr);
    std::vector<int32_t> ids = g_tokenizer->Encode(str);
    env->ReleaseStringUTFChars(text, str);

    int maxContent = maxLen - 2;
    if ((int)ids.size() > maxContent) {
        ids.resize(maxContent);
    }

    std::vector<int32_t> inputIds;
    inputIds.reserve(maxLen);
    inputIds.push_back(BOS_ID);
    inputIds.insert(inputIds.end(), ids.begin(), ids.end());
    inputIds.push_back(EOS_ID);
    int realLen = (int)inputIds.size();
    while ((int)inputIds.size() < maxLen) {
        inputIds.push_back(PAD_ID);
    }

    std::vector<int32_t> mask(maxLen, 0);
    for (int i = 0; i < realLen; i++) mask[i] = 1;

    jintArray result = env->NewIntArray(maxLen * 2);
    env->SetIntArrayRegion(result, 0, maxLen, inputIds.data());
    env->SetIntArrayRegion(result, maxLen, maxLen, mask.data());
    return result;
}


JNIEXPORT jstring JNICALL
Java_il_nfm_localmind_Tokenizer_nativeDecode(JNIEnv *env, jobject, jintArray ids) {
    if (!g_tokenizer) return env->NewStringUTF("");

    jint len = env->GetArrayLength(ids);
    jint *raw = env->GetIntArrayElements(ids, nullptr);

    std::vector<int32_t> vec(raw, raw + len);
    env->ReleaseIntArrayElements(ids, raw, JNI_ABORT);

    while (!vec.empty() && vec.back() == 0) vec.pop_back();

    std::string text = g_tokenizer->Decode(vec);
    return env->NewStringUTF(text.c_str());
}

}
