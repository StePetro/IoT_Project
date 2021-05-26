#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "coap-engine.h"
#include "os/dev/leds.h"

/* Luminosity variables */
#include "global-variables.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "Luminosity Sensor Resource"
#define LOG_LEVEL LOG_LEVEL_INFO

/*---------------------------------------------------------------------------*/

static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset);

static void res_put_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset);

static void res_event_handler(void);

EVENT_RESOURCE(res_luminosity, 
               '</lum-sensor/luminosity>;title="Luminosity Sensor Value";
               rt="luminosity";if="lum-sensor"',
               res_get_handler, 
               NULL, 
               res_put_handler, 
               NULL,
               res_event_handler);

/*---------------------------------------------------------------------------*/

int actual_luminosity = 0;
int bulbs_luminosity = 0;
int external_luminosity = 0;
size_t max_char_len = 4;  // 100 + endstring

/*---------------------------------------------------------------------------*/

/* To get actual luminosity in room */
static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset) {

  //LOG_INFO("Handling get request...\n");

  char msg[max_char_len];

  snprintf(msg, max_char_len, "%d", actual_luminosity);

  size_t len = strlen(msg);
  memcpy(buffer, (const void *)msg, len);

  coap_set_header_content_format(response, TEXT_PLAIN);
  coap_set_header_etag(response, (uint8_t *)&len, 1);
  coap_set_payload(response, buffer, len);

} 

/*---------------------------------------------------------------------------*/

/* To set the luminosity value */
static void res_put_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset) {

  LOG_INFO("Handling put request...\n");

  const char *rcvd_msg = NULL;
  char char_lum[max_char_len];
  size_t len = 0;

  len = coap_get_post_variable(request, "bulb", &rcvd_msg);

  if (len > 0 && len <= max_char_len) {

    memcpy(char_lum, rcvd_msg, len);
    int tmp_lum = atoi(char_lum);

    if (tmp_lum < 0 || tmp_lum > MAX_LUMINOSITY) {

      LOG_INFO("Received invalid luminosity value\n");
      coap_set_status_code(response, BAD_REQUEST_4_00);

    } else {

      bulbs_luminosity = tmp_lum;
      actual_luminosity = bulbs_luminosity + external_luminosity;
      LOG_INFO("Estimated bulb luminosity is %u, thus new actual luminosity is: %u\n", bulbs_luminosity, actual_luminosity);
      coap_set_status_code(response, CHANGED_2_04);

      char msg[max_char_len];

      /* Respond with new luminosity value */
      
      snprintf(msg, max_char_len, "%d", bulbs_luminosity);
      size_t len = strlen(msg);
      memcpy(buffer, (const void *)msg, len);

      coap_set_header_content_format(response, TEXT_PLAIN);
      coap_set_header_etag(response, (uint8_t *)&len, 1);
      coap_set_payload(response, buffer, len);

    }

  } else {

    LOG_INFO("Bad Request\n");
    coap_set_status_code(response, BAD_REQUEST_4_00);

  }

}

/*---------------------------------------------------------------------------*/

/* Notify observers */
static void res_event_handler(void){

  coap_notify_observers(&res_luminosity);

}