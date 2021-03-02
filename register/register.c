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

// "new_registration" variable
#include "global-variables.h"

/* Log configuration */
#include "sys/log.h" 
#define LOG_MODULE "Register"
#define LOG_LEVEL LOG_LEVEL_INFO

/*---------------------------------------------------------------------------*/

/* Declare and autostart process */
PROCESS(register_process, "Register");
AUTOSTART_PROCESSES(&register_process);

/*---------------------------------------------------------------------------*/

/* Resources */
extern coap_resource_t res_register;
bool new_registration = false;

/*---------------------------------------------------------------------------*/

static struct ctimer connectivity_timer;
static bool connected = false;

/* Checks if node is connected to BR */
static void check_connectivity(void*ptr){

  if(!NETSTACK_ROUTING.node_is_reachable()){

    LOG_INFO("BR not reachable...\n");
    ctimer_reset(&connectivity_timer);

  }else{

    LOG_INFO("Register ready\n");
    connected = true;

  }

}

/*---------------------------------------------------------------------------*/

/* Process */
PROCESS_THREAD(register_process, ev, data) {

  PROCESS_BEGIN();

  /* Resources Activation */
  coap_activate_resource(&res_register, "register");

  LOG_INFO("Register started...\n");

  /* Check connectivity every 5 second */
  ctimer_set(&connectivity_timer, CLOCK_SECOND*5, check_connectivity, NULL);

  /* Check new registration */
  static struct etimer register_check_timer;
  etimer_set(&register_check_timer, CLOCK_SECOND);

  while (1) {

    PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&register_check_timer));

    if(new_registration){
      res_register.trigger();
    }

    etimer_reset(&register_check_timer);

  }

  PROCESS_END();

}