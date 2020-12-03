#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "coap-engine.h"
#include "contiki.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

extern coap_resource_t res_presence_detected;

PROCESS(presence_sensor, "Presence Sensor");
AUTOSTART_PROCESSES(&presence_sensor);

PROCESS_THREAD(presence_sensor, ev, data) {
  PROCESS_BEGIN();

  PROCESS_PAUSE();

  LOG_INFO("Starting Presence Sensor\n");

  coap_activate_resource(&res_presence_detected, "presence_detected");

  while (1) {
    PROCESS_WAIT_EVENT();
  }

  PROCESS_END();
}
