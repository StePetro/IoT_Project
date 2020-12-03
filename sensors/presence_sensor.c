#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "coap-engine.h"
#include "contiki.h"
#include "sys/etimer.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

extern coap_resource_t res_presence_detected;

static struct etimer e_timer;

PROCESS(presence_sensor, "Presence Detected");
AUTOSTART_PROCESSES(&presence_sensor);

PROCESS_THREAD(presence_sensor, ev, data) {
  PROCESS_BEGIN();

  PROCESS_PAUSE();

  LOG_INFO("Starting Presence Sensor\n");

  coap_activate_resource(&res_presence_detected, "presence_detected");

  etimer_set(&e_timer, CLOCK_SECOND * 4);

  printf("Loop\n");

  while (1) {
    PROCESS_WAIT_EVENT();

    if (ev == PROCESS_EVENT_TIMER && data == &e_timer) {
      printf("Presence Changed\n");

      res_presence_detected.trigger();

      etimer_set(&e_timer, CLOCK_SECOND * 4);
    }
  }

  PROCESS_END();
}
