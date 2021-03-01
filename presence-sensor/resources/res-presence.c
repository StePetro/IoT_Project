#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "coap-engine.h"
#include "os/dev/leds.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "Presence Sensor Resource"
#define LOG_LEVEL LOG_LEVEL_INFO

/*---------------------------------------------------------------------------*/

static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset);

static void res_event_handler(void);

EVENT_RESOURCE(res_presence, "", res_get_handler, NULL, NULL, NULL,
               res_event_handler);

/*---------------------------------------------------------------------------*/

static bool presence = true;
size_t max_char_len = 2;  // T or F + endstring

/*---------------------------------------------------------------------------*/

/* If someone is present in room or not */
static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset) {

  LOG_INFO("Handling get request...\n");

  char msg[max_char_len];

  if (presence == true) {
    snprintf(msg, max_char_len, "%s", "T");
  } else {
    snprintf(msg, max_char_len, "%s", "F");
  }

  size_t len = strlen(msg);
  memcpy(buffer, (const void *)msg, len);

  coap_set_header_content_format(response, TEXT_PLAIN);
  coap_set_header_etag(response, (uint8_t *)&len, 1);
  coap_set_payload(response, buffer, len);

}

/*---------------------------------------------------------------------------*/

/* Changes presence state and notify observers */
static void res_event_handler(void){

	presence = !presence;
  coap_notify_observers(&res_presence);

}