#include "include/pos_printer/pos_printer_plugin_c_api.h"

#include <flutter/plugin_registrar_windows.h>

#include "pos_printer_plugin.h"

void PosPrinterPluginCApiRegisterWithRegistrar(
    FlutterDesktopPluginRegistrarRef registrar) {
  pos_printer::PosPrinterPlugin::RegisterWithRegistrar(
      flutter::PluginRegistrarManager::GetInstance()
          ->GetRegistrar<flutter::PluginRegistrarWindows>(registrar));
}
