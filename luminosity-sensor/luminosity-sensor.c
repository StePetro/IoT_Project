#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

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

#include "coap-blocking-api.h"
#include "coap-engine.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "Luminosity Sensor"
#define LOG_LEVEL LOG_LEVEL_INFO

/* Global Variables */

// textual max ip len in bytes including ":"
// xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx
#define MAX_IP_LEN 39

// to fit ip + device type
#define MAX_PAYLOAD_LEN 60

// retry to connect to BR every 5 sec
#define CONN_TRY_INTERVAL 5

// Luminosity variables
#include "global-variables.h"

// server ip hardcoded
#define SERVER_EP "coap://[fd00::1]:5683"

/*---------------------------------------------------------------------------*/

/* Declare and autostart process */
PROCESS(luminosity_sensor, "Luminosity Sensor");
AUTOSTART_PROCESSES(&luminosity_sensor);

/*---------------------------------------------------------------------------*/

// not yet registred
static bool registred = false;

/* Handles registration response */
void client_chunk_handler(coap_message_t *response) {

  const uint8_t *chunk;

  // fail only if don't receive a response 
  if (response == NULL) {
    puts("Request timed out");
    return;
  }

  // ack received 
  coap_get_payload(response, &chunk);
  registred = true;

}

/*---------------------------------------------------------------------------*/

/* Resources */
extern coap_resource_t res_luminosity;
int external_luminosity;
int actual_luminosity;
int bulbs_luminosity;

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
  etimer_set(&connectivity_timer, CLOCK_SECOND*CONN_TRY_INTERVAL);

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
  snprintf(payload,MAX_PAYLOAD_LEN,"%s@%s",type,ip);

  while(!registred){

    coap_init_message(request, COAP_TYPE_CON, COAP_PUT, 0);
    coap_set_header_uri_path(request, service_url);
    coap_set_payload(request,payload, strlen(payload));
    COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);

  }

  LOG_INFO("Luminosity sensor registred and ready\n");

  static struct etimer random_timer;
  static bool increase = true;

  /* Manage random luminosity sensing */
  while (1) {

    // simulate random sensing
    etimer_set(&random_timer, rand()%(CLOCK_SECOND*60) + 10*CLOCK_SECOND);

    PROCESS_WAIT_UNTIL(etimer_expired(&random_timer));

    /* Gradually increase and decrease external luminosity */
    if(increase){
      int tmp_lum = floor(external_luminosity + rand()%10 + 1);
      if(tmp_lum>MAX_LUMINOSITY){
        external_luminosity = MAX_LUMINOSITY;
        increase = false;
      }else{
        external_luminosity = tmp_lum;
      }
    }else{
      int tmp_lum = floor(external_luminosity - rand()%10 - 1);
      if(tmp_lum<0){
        external_luminosity = 0;
        increase = true;
      }else{
        external_luminosity = tmp_lum;
      }
    }

    actual_luminosity = external_luminosity + bulbs_luminosity;

    LOG_INFO("Luminosity value changed at: %d\n", actual_luminosity);
    LOG_INFO("Estimated external luminosity is: %d\n", external_luminosity);
    LOG_INFO("Estimated bulbs luminosity is: %d\n", bulbs_luminosity);

    // Trigger resource change notification
    res_luminosity.trigger();

  }

  PROCESS_END();

}