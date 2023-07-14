//
//  Generated file. Do not edit.
//

// clang-format off

#include "generated_plugin_registrant.h"

#include <pos_printer/pos_printer_plugin.h>

void fl_register_plugins(FlPluginRegistry* registry) {
  g_autoptr(FlPluginRegistrar) pos_printer_registrar =
      fl_plugin_registry_get_registrar_for_plugin(registry, "PosPrinterPlugin");
  pos_printer_plugin_register_with_registrar(pos_printer_registrar);
}
