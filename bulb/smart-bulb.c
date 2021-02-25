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
#include "os/dev/leds.h"

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
extern coap_resource_t res_switch;

/*---------------------------------------------------------------------------*/

/* Process */
PROCESS_THREAD(smart_bulb, ev, data) {

  PROCESS_BEGIN();

  /* Resources Activation */
  coap_activate_resource(&res_luminosity, "luminosity");
  coap_activate_resource(&res_switch, "switch");

  LOG_INFO("Smart bulb started...\n");

  /* Check connectivity every 5 second */
  leds_set(LEDS_NUM_TO_MASK(LEDS_YELLOW));
  static struct etimer connectivity_timer;
  etimer_set(&connectivity_timer, CLOCK_SECOND*5);
  static bool ready = false;

  while (1) {

    PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&connectivity_timer));

    if(!NETSTACK_ROUTING.node_is_reachable()){

      leds_set(LEDS_NUM_TO_MASK(LEDS_YELLOW));
      LOG_INFO("BR not reachable...\n");
      ready = false;

    }else{

      if(!ready){
        LOG_INFO("Smart bulb ready\n");
        leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
        ready = true;
      }

    }

    etimer_reset(&connectivity_timer);

  }

  PROCESS_END();

}