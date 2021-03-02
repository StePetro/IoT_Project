#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "lib/random.h"

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

#define MAX_IP_LEN 39
#define MAX_PAYLOAD_LEN 60

#include "coap-blocking-api.h"
#include "coap-engine.h"
#define SERVER_EP "coap://[fd00::202:2:2:2]:5683"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "Luminosity Sensor"
#define LOG_LEVEL LOG_LEVEL_INFO

/*---------------------------------------------------------------------------*/

/* Declare and autostart process */
PROCESS(luminosity_sensor, "Presence Sensor");
AUTOSTART_PROCESSES(&luminosity_sensor);

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
extern coap_resource_t res_luminosity;

/*---------------------------------------------------------------------------*/

static struct etimer connectivity_timer;
static bool connected = false;

/* Checks if node is connected to BR */
static void check_connectivity(){

  if(!NETSTACK_ROUTING.node_is_reachable()){

    LOG_INFO("BR not reachable...\n");
    etimer_reset(&connectivity_timer);

  }else{

    LOG_INFO("Luminosity Sensor connected...\n");
    leds_toggle(LEDS_NUM_TO_MASK(LEDS_YELLOW));
    connected = true;

  }

}

/*---------------------------------------------------------------------------*/

/* Process */
PROCESS_THREAD(luminosity_sensor, ev, data) {

  PROCESS_BEGIN();

  /* Registration */
  static coap_endpoint_t server_ep;
  static coap_message_t request[1];
  coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);


  /* Resources Activation */
  coap_activate_resource(&res_luminosity, "luminosity");

  LOG_INFO("Luminosity Sensor started...\n");

  /* Check connectivity every 5 second */
  leds_set(LEDS_NUM_TO_MASK(LEDS_YELLOW));
  etimer_set(&connectivity_timer, CLOCK_SECOND*5);

  while(!connected){
    PROCESS_WAIT_UNTIL(etimer_expired(&connectivity_timer));
    check_connectivity();
  }

  /* Manage registration */
  char service_url[] = "/register";
  char type[] = "LUM_SENS";
  char ip[MAX_IP_LEN];
  char payload[MAX_PAYLOAD_LEN];
  uiplib_ipaddr_snprint(ip,MAX_IP_LEN, &uip_ds6_if.addr_list[1].ipaddr);
  snprintf(payload,MAX_PAYLOAD_LEN,"type=%s&ip=%s",type,ip);

  while(!registred){

    coap_init_message(request, COAP_TYPE_CON, COAP_PUT, 0);
    coap_set_header_uri_path(request, service_url);
    coap_set_payload(request,payload, strlen(payload));
    COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);

  }

  LOG_INFO("Luminosity sensor registred and ready\n");

  static struct etimer random_timer;

  /* Manage random luminosity sensing */
  while (1) {

    etimer_set(&random_timer, random_rand() % (CLOCK_SECOND*20) + 2*CLOCK_SECOND);

    PROCESS_WAIT_UNTIL(etimer_expired(&random_timer));

    res_luminosity.trigger();

    leds_toggle(LEDS_NUM_TO_MASK(LEDS_RED));

  }

  PROCESS_END();

}