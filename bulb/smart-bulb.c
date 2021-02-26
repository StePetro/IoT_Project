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

#include "sys/ctimer.h"
#include "os/dev/leds.h"
#include "os/dev/button-hal.h"

// "status" variable
#include "global-variables.h"

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

static struct ctimer connectivity_timer;
static bool ready = false;

/* Checks if node is connected to BR */
static void check_connectivity(void*ptr){

  if(!NETSTACK_ROUTING.node_is_reachable()){

    leds_set(LEDS_NUM_TO_MASK(LEDS_YELLOW));
    LOG_INFO("BR not reachable...\n");
    ctimer_reset(&connectivity_timer);

  }else{

    LOG_INFO("Smart bulb ready\n");
    leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
    ready = true;

  }

}

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
  ctimer_set(&connectivity_timer, CLOCK_SECOND*5, check_connectivity, NULL);

  while (1) {

    PROCESS_YIELD();

    if(ev==button_hal_release_event && ready){

      LOG_INFO("Button click...\n");

      if(status == 0){

        LOG_INFO("Switch set to ON\n");
        leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));

      }else{

        LOG_INFO("Switch set to OFF\n");
        leds_set(LEDS_NUM_TO_MASK(LEDS_RED));

      }

      status = !status;

    }

  }

  PROCESS_END();

}