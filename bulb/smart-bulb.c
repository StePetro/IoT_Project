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
#include "os/dev/button-hal.h"

#define MAX_IP_LEN 39
#define MAX_PAYLOAD_LEN 60

// "status" variable
#include "global-variables.h"

#include "coap-blocking-api.h"
#include "coap-engine.h"
#define SERVER_EP "coap://[fd00::1]:5683"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "Bulb"
#define LOG_LEVEL LOG_LEVEL_INFO

/*---------------------------------------------------------------------------*/

/* Declare and autostart process */
PROCESS(smart_bulb, "Smart Bulb");
AUTOSTART_PROCESSES(&smart_bulb);

/*---------------------------------------------------------------------------*/

static bool registred = false;

void client_chunk_handler(coap_message_t *response) {

  const uint8_t *chunk;

  if (response == NULL) {
    puts("Request timed out");
    return;
  }

  coap_get_payload(response, &chunk);
  registred = true;

}

/*---------------------------------------------------------------------------*/

/* Resources */
extern coap_resource_t res_presence;
extern coap_resource_t res_switch;

/*---------------------------------------------------------------------------*/

static struct etimer connectivity_timer;
static bool connected = false;

/* Checks if node is connected to BR */
static void check_connectivity(){

  if(!NETSTACK_ROUTING.node_is_reachable()){

    LOG_INFO("BR not reachable...\n");
    etimer_reset(&connectivity_timer);

  }else{

    LOG_INFO("Smart bulb connected...\n");
    leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
    connected = true;

  }

}

/*---------------------------------------------------------------------------*/

/* Process */
PROCESS_THREAD(smart_bulb, ev, data) {

  PROCESS_BEGIN();

  /* Registration */
  static coap_endpoint_t server_ep;
  static coap_message_t request[1];
  coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);


  /* Resources Activation */
  coap_activate_resource(&res_presence, "luminosity");
  coap_activate_resource(&res_switch, "switch");

  LOG_INFO("Smart bulb started...\n");

  /* Check connectivity every 5 second */
  leds_set(LEDS_NUM_TO_MASK(LEDS_YELLOW));
  etimer_set(&connectivity_timer, CLOCK_SECOND*5);

  while(!connected){
    PROCESS_WAIT_UNTIL(etimer_expired(&connectivity_timer));
    check_connectivity();
  }

  /* Manage registration */
  char service_url[] = "/register";
  char type[] = "BULB";
  char ip[MAX_IP_LEN];
  char payload[MAX_PAYLOAD_LEN];
  uiplib_ipaddr_snprint(ip,MAX_IP_LEN, &uip_ds6_if.addr_list[1].ipaddr);
  snprintf(payload,MAX_PAYLOAD_LEN,"%s@%s",type,ip);

  while(!registred){

    coap_init_message(request, COAP_TYPE_CON, COAP_PUT, 0);
    coap_set_header_uri_path(request, service_url);
    coap_set_payload(request,payload, strlen(payload));
    COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);

  }

  LOG_INFO("Smart bulb registred and ready\n");

  /* Manage button click */
  while (1) {

    PROCESS_YIELD();

    if(ev==button_hal_release_event){

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