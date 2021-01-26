{
  "variables": {
    "ARCH%": "X64"
  },
  "targets": [
    {
      "target_name": "KeyboardKtWindows<(ARCH)",
      "sources": [ "JsKeyboardHandler.cpp" ],
      "cflags": [ "-s -D_WIN32_WINNT=0x600" ],
      "cflags_cc": [ "-std=c++11 -s -D_WIN32_WINNT=0x600" ],

      "include_dirs": [
        "<!(echo $NODE_ADDON_API_HEADERS_DIR)",
        "../../../nativeCommon/windows",
        "../"
      ]
    }
  ]
}
