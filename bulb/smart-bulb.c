#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "coap-engine.h"
#include "contiki.h"

#include "node-id.h"
#include "net/ipv6/simple-udp.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-ds6.h"
#include "net/ipv6/uip-debug.h"
#include "routing/routing.h"

#include "sys/etimer.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "Bulb"
#define LOG_LEVEL LOG_LEVEL_INFO

/*---------------------------------------------------------------------------*/

/* Declare and autostart process */
PROCESS(smart_bulb, "Coap Server");
AUTOSTART_PROCESSES(&smart_bulb);

/* Resources */
extern coap_resource_t res_luminosity;

/*---------------------------------------------------------------------------*/

/* Process */
PROCESS_THREAD(smart_bulb, ev, data) {

  PROCESS_BEGIN();

  LOG_INFO("Smart bulb started...\n");

  /* Check connettivity every 5 second */
  static struct etimer et;
  etimer_set(&et, CLOCK_SECOND*5);

  while (!NETSTACK_ROUTING.node_is_reachable()) {

    PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&et));
    LOG_INFO("BR not reachable...\n");
    etimer_reset(&et);

  }

  LOG_INFO("Connected...\n");

  /* Resources Activation */
  coap_activate_resource(&res_luminosity, "luminosity");

  LOG_INFO("Smart bulb ready\n");

  while (1) {
    PROCESS_WAIT_EVENT();
  }

  PROCESS_END();

}