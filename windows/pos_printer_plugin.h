#ifndef FLUTTER_PLUGIN_POS_PRINTER_PLUGIN_H_
#define FLUTTER_PLUGIN_POS_PRINTER_PLUGIN_H_

#include <flutter/method_channel.h>
#include <flutter/plugin_registrar_windows.h>

#include <memory>

namespace pos_printer {

class PosPrinterPlugin : public flutter::Plugin {
 public:
  static void RegisterWithRegistrar(flutter::PluginRegistrarWindows *registrar);

  PosPrinterPlugin();

  virtual ~PosPrinterPlugin();

  // Disallow copy and assign.
  PosPrinterPlugin(const PosPrinterPlugin&) = delete;
  PosPrinterPlugin& operator=(const PosPrinterPlugin&) = delete;

  // Called when a method is called on this plugin's channel from Dart.
  void HandleMethodCall(
      const flutter::MethodCall<flutter::EncodableValue> &method_call,
      std::unique_ptr<flutter::MethodResult<flutter::EncodableValue>> result);
};

}  // namespace pos_printer

#endif  // FLUTTER_PLUGIN_POS_PRINTER_PLUGIN_H_
